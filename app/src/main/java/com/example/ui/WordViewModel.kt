package com.example.ui

import android.app.Application
import android.speech.tts.TextToSpeech
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.*
import com.example.data.repository.DocumentRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class WordViewModel(application: Application) : AndroidViewModel(application), TextToSpeech.OnInitListener {

    private val repository: DocumentRepository
    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    val allDocuments: StateFlow<List<WordDocument>>
    
    private val _activeDocument = MutableStateFlow<WordDocument?>(null)
    val activeDocument: StateFlow<WordDocument?> = _activeDocument.asStateFlow()

    private val _selectedBlockIndex = MutableStateFlow<Int>(0)
    val selectedBlockIndex: StateFlow<Int> = _selectedBlockIndex.asStateFlow()

    private val _activeRibbonTab = MutableStateFlow(RibbonTab.HOME)
    val activeRibbonTab: StateFlow<RibbonTab> = _activeRibbonTab.asStateFlow()

    private val _isRibbonExpanded = MutableStateFlow(true)
    val isRibbonExpanded: StateFlow<Boolean> = _isRibbonExpanded.asStateFlow()

    private val _isPrintLayout = MutableStateFlow(true)
    val isPrintLayout: StateFlow<Boolean> = _isPrintLayout.asStateFlow()

    private val _zoomPercent = MutableStateFlow(100)
    val zoomPercent: StateFlow<Int> = _zoomPercent.asStateFlow()

    private val _showRuler = MutableStateFlow(true)
    val showRuler: StateFlow<Boolean> = _showRuler.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    // Find and Replace state
    private val _isFindReplaceVisible = MutableStateFlow(false)
    val isFindReplaceVisible: StateFlow<Boolean> = _isFindReplaceVisible.asStateFlow()
    
    private val _findQuery = MutableStateFlow("")
    val findQuery: StateFlow<String> = _findQuery.asStateFlow()

    private val _replaceQuery = MutableStateFlow("")
    val replaceQuery: StateFlow<String> = _replaceQuery.asStateFlow()

    private val _searchMatchCount = MutableStateFlow(0)
    val searchMatchCount: StateFlow<Int> = _searchMatchCount.asStateFlow()

    // Dialogs state
    private val _isStatsDialogVisible = MutableStateFlow(false)
    val isStatsDialogVisible: StateFlow<Boolean> = _isStatsDialogVisible.asStateFlow()

    private val _isSymbolDialogVisible = MutableStateFlow(false)
    val isSymbolDialogVisible: StateFlow<Boolean> = _isSymbolDialogVisible.asStateFlow()

    private val _isTableDialogVisible = MutableStateFlow(false)
    val isTableDialogVisible: StateFlow<Boolean> = _isTableDialogVisible.asStateFlow()

    private val _isPropertiesDialogVisible = MutableStateFlow(false)
    val isPropertiesDialogVisible: StateFlow<Boolean> = _isPropertiesDialogVisible.asStateFlow()

    private val _isHeaderFooterDialogVisible = MutableStateFlow(false)
    val isHeaderFooterDialogVisible: StateFlow<Boolean> = _isHeaderFooterDialogVisible.asStateFlow()

    // Undo / Redo stacks
    private val undoStack = mutableListOf<List<DocumentBlock>>()
    private val redoStack = mutableListOf<List<DocumentBlock>>()
    private val _canUndo = MutableStateFlow(false)
    val canUndo: StateFlow<Boolean> = _canUndo.asStateFlow()
    private val _canRedo = MutableStateFlow(false)
    val canRedo: StateFlow<Boolean> = _canRedo.asStateFlow()

    private var autoSaveJob: Job? = null

    init {
        val db = AppDatabase.getInstance(application)
        repository = DocumentRepository(db.documentDao())

        allDocuments = repository.allDocuments.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        viewModelScope.launch {
            repository.initializeDefaultDocumentsIfEmpty()
        }

        try {
            tts = TextToSpeech(application, this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.getDefault()
            isTtsReady = true
        }
    }

    override fun onCleared() {
        tts?.stop()
        tts?.shutdown()
        super.onCleared()
    }

    fun openDocument(doc: WordDocument) {
        undoStack.clear()
        redoStack.clear()
        _canUndo.value = false
        _canRedo.value = false
        _activeDocument.value = doc
        _selectedBlockIndex.value = 0
    }

    fun openDocumentById(id: String) {
        viewModelScope.launch {
            val doc = repository.getDocument(id)
            if (doc != null) {
                openDocument(doc)
            }
        }
    }

    fun createNewDocument(templateKey: String) {
        val newDoc = when (templateKey) {
            "resume" -> DocTemplates.getResumeTemplate()
            "proposal" -> DocTemplates.getBusinessProposalTemplate()
            "meeting" -> DocTemplates.getMeetingMinutesTemplate()
            "letter" -> DocTemplates.getFormalLetterTemplate()
            else -> DocTemplates.getBlankDocument()
        }
        viewModelScope.launch {
            repository.saveDocument(newDoc)
            openDocument(newDoc)
        }
    }

    fun closeDocument() {
        // Force save before closing
        _activeDocument.value?.let { doc ->
            viewModelScope.launch {
                repository.saveDocument(doc)
            }
        }
        stopReading()
        _activeDocument.value = null
    }

    private fun pushToUndo(currentBlocks: List<DocumentBlock>) {
        undoStack.add(currentBlocks)
        if (undoStack.size > 50) undoStack.removeAt(0)
        redoStack.clear()
        _canUndo.value = undoStack.isNotEmpty()
        _canRedo.value = false
    }

    fun undo() {
        val currentDoc = _activeDocument.value ?: return
        if (undoStack.isNotEmpty()) {
            val previousBlocks = undoStack.removeAt(undoStack.size - 1)
            redoStack.add(currentDoc.blocks)
            _activeDocument.value = currentDoc.copy(blocks = previousBlocks)
            _canUndo.value = undoStack.isNotEmpty()
            _canRedo.value = true
            triggerAutoSave()
        }
    }

    fun redo() {
        val currentDoc = _activeDocument.value ?: return
        if (redoStack.isNotEmpty()) {
            val nextBlocks = redoStack.removeAt(redoStack.size - 1)
            undoStack.add(currentDoc.blocks)
            _activeDocument.value = currentDoc.copy(blocks = nextBlocks)
            _canUndo.value = true
            _canRedo.value = redoStack.isNotEmpty()
            triggerAutoSave()
        }
    }

    private fun triggerAutoSave() {
        autoSaveJob?.cancel()
        autoSaveJob = viewModelScope.launch {
            delay(600)
            _activeDocument.value?.let { doc ->
                repository.saveDocument(doc)
            }
        }
    }

    fun updateTitle(newTitle: String) {
        val doc = _activeDocument.value ?: return
        _activeDocument.value = doc.copy(title = newTitle)
        triggerAutoSave()
    }

    fun selectBlock(index: Int) {
        val doc = _activeDocument.value ?: return
        if (index in doc.blocks.indices) {
            _selectedBlockIndex.value = index
        }
    }

    fun updateBlock(index: Int, newBlock: DocumentBlock) {
        val doc = _activeDocument.value ?: return
        if (index !in doc.blocks.indices) return

        pushToUndo(doc.blocks)
        val mutable = doc.blocks.toMutableList()
        mutable[index] = newBlock
        _activeDocument.value = doc.copy(blocks = mutable)
        triggerAutoSave()
    }

    fun updateParagraphText(index: Int, newText: String) {
        val doc = _activeDocument.value ?: return
        val current = doc.blocks.getOrNull(index) as? DocumentBlock.Paragraph ?: return
        
        // Fast update without recording every keystroke to undo
        val mutable = doc.blocks.toMutableList()
        mutable[index] = current.copy(text = newText)
        _activeDocument.value = doc.copy(blocks = mutable)
        triggerAutoSave()
    }

    fun addParagraphAfter(afterIndex: Int = -1) {
        val doc = _activeDocument.value ?: return
        pushToUndo(doc.blocks)
        val mutable = doc.blocks.toMutableList()
        val insertIndex = if (afterIndex in doc.blocks.indices) afterIndex + 1 else mutable.size
        val newBlock = DocumentBlock.Paragraph(text = "")
        mutable.add(insertIndex, newBlock)
        _activeDocument.value = doc.copy(blocks = mutable)
        _selectedBlockIndex.value = insertIndex
        triggerAutoSave()
    }

    fun removeBlock(index: Int) {
        val doc = _activeDocument.value ?: return
        if (doc.blocks.size <= 1) return // keep at least 1 block
        pushToUndo(doc.blocks)
        val mutable = doc.blocks.toMutableList()
        mutable.removeAt(index)
        _activeDocument.value = doc.copy(blocks = mutable)
        _selectedBlockIndex.value = (index - 1).coerceAtLeast(0)
        triggerAutoSave()
    }

    fun moveBlock(index: Int, up: Boolean) {
        val doc = _activeDocument.value ?: return
        val target = if (up) index - 1 else index + 1
        if (target !in doc.blocks.indices) return
        pushToUndo(doc.blocks)
        val mutable = doc.blocks.toMutableList()
        Collections.swap(mutable, index, target)
        _activeDocument.value = doc.copy(blocks = mutable)
        _selectedBlockIndex.value = target
        triggerAutoSave()
    }

    // Paragraph Formatting Actions
    fun toggleBold() {
        val index = _selectedBlockIndex.value
        val doc = _activeDocument.value ?: return
        val p = doc.blocks.getOrNull(index) as? DocumentBlock.Paragraph ?: return
        updateBlock(index, p.copy(isBold = !p.isBold))
    }

    fun toggleItalic() {
        val index = _selectedBlockIndex.value
        val doc = _activeDocument.value ?: return
        val p = doc.blocks.getOrNull(index) as? DocumentBlock.Paragraph ?: return
        updateBlock(index, p.copy(isItalic = !p.isItalic))
    }

    fun toggleUnderline() {
        val index = _selectedBlockIndex.value
        val doc = _activeDocument.value ?: return
        val p = doc.blocks.getOrNull(index) as? DocumentBlock.Paragraph ?: return
        updateBlock(index, p.copy(isUnderline = !p.isUnderline))
    }

    fun toggleStrikethrough() {
        val index = _selectedBlockIndex.value
        val doc = _activeDocument.value ?: return
        val p = doc.blocks.getOrNull(index) as? DocumentBlock.Paragraph ?: return
        updateBlock(index, p.copy(isStrikethrough = !p.isStrikethrough))
    }

    fun toggleSubscript() {
        val index = _selectedBlockIndex.value
        val doc = _activeDocument.value ?: return
        val p = doc.blocks.getOrNull(index) as? DocumentBlock.Paragraph ?: return
        updateBlock(index, p.copy(isSubscript = !p.isSubscript, isSuperscript = false))
    }

    fun toggleSuperscript() {
        val index = _selectedBlockIndex.value
        val doc = _activeDocument.value ?: return
        val p = doc.blocks.getOrNull(index) as? DocumentBlock.Paragraph ?: return
        updateBlock(index, p.copy(isSuperscript = !p.isSuperscript, isSubscript = false))
    }

    fun setAlignment(alignment: BlockAlignment) {
        val index = _selectedBlockIndex.value
        val doc = _activeDocument.value ?: return
        val p = doc.blocks.getOrNull(index) as? DocumentBlock.Paragraph ?: return
        updateBlock(index, p.copy(alignment = alignment))
    }

    fun setLineSpacing(spacing: Float) {
        val index = _selectedBlockIndex.value
        val doc = _activeDocument.value ?: return
        val p = doc.blocks.getOrNull(index) as? DocumentBlock.Paragraph ?: return
        updateBlock(index, p.copy(lineSpacingMultiplier = spacing))
    }

    fun setStyle(style: BlockStyle) {
        val index = _selectedBlockIndex.value
        val doc = _activeDocument.value ?: return
        val p = doc.blocks.getOrNull(index) as? DocumentBlock.Paragraph ?: return
        updateBlock(
            index,
            p.copy(
                style = style,
                fontSizePt = style.defaultFontSizePt,
                isBold = style.isDefaultBold || p.isBold
            )
        )
    }

    fun setTextColor(colorHex: Long) {
        val index = _selectedBlockIndex.value
        val doc = _activeDocument.value ?: return
        val p = doc.blocks.getOrNull(index) as? DocumentBlock.Paragraph ?: return
        updateBlock(index, p.copy(textColorHex = colorHex))
    }

    fun setHighlightColor(colorHex: Long) {
        val index = _selectedBlockIndex.value
        val doc = _activeDocument.value ?: return
        val p = doc.blocks.getOrNull(index) as? DocumentBlock.Paragraph ?: return
        updateBlock(index, p.copy(highlightColorHex = colorHex))
    }

    fun setFontSize(sizePt: Int) {
        val index = _selectedBlockIndex.value
        val doc = _activeDocument.value ?: return
        val p = doc.blocks.getOrNull(index) as? DocumentBlock.Paragraph ?: return
        updateBlock(index, p.copy(fontSizePt = sizePt.coerceIn(8, 72)))
    }

    fun changeFontSizeBy(delta: Int) {
        val index = _selectedBlockIndex.value
        val doc = _activeDocument.value ?: return
        val p = doc.blocks.getOrNull(index) as? DocumentBlock.Paragraph ?: return
        setFontSize(p.fontSizePt + delta)
    }

    fun setFontFamily(font: FontFamilyChoice) {
        val index = _selectedBlockIndex.value
        val doc = _activeDocument.value ?: return
        val p = doc.blocks.getOrNull(index) as? DocumentBlock.Paragraph ?: return
        updateBlock(index, p.copy(fontFamily = font))
    }

    fun setListType(listType: ListType) {
        val index = _selectedBlockIndex.value
        val doc = _activeDocument.value ?: return
        val p = doc.blocks.getOrNull(index) as? DocumentBlock.Paragraph ?: return
        val nextType = if (p.listType == listType) ListType.NONE else listType
        updateBlock(index, p.copy(listType = nextType))
    }

    fun toggleChecklist(index: Int) {
        val doc = _activeDocument.value ?: return
        val p = doc.blocks.getOrNull(index) as? DocumentBlock.Paragraph ?: return
        updateBlock(index, p.copy(isChecked = !p.isChecked))
    }

    fun changeIndent(delta: Int) {
        val index = _selectedBlockIndex.value
        val doc = _activeDocument.value ?: return
        val p = doc.blocks.getOrNull(index) as? DocumentBlock.Paragraph ?: return
        val newIndent = (p.indentLevel + delta).coerceIn(0, 5)
        updateBlock(index, p.copy(indentLevel = newIndent))
    }

    fun clearFormatting() {
        val index = _selectedBlockIndex.value
        val doc = _activeDocument.value ?: return
        val p = doc.blocks.getOrNull(index) as? DocumentBlock.Paragraph ?: return
        updateBlock(
            index,
            DocumentBlock.Paragraph(
                id = p.id,
                text = p.text,
                style = BlockStyle.NORMAL,
                alignment = BlockAlignment.LEFT,
                fontSizePt = 11,
                fontFamily = FontFamilyChoice.CALIBRI,
                textColorHex = 0xFF201F1E,
                highlightColorHex = 0x00000000
            )
        )
    }

    // Insert Actions
    fun insertTable(rows: Int, cols: Int, style: TableStyle) {
        val doc = _activeDocument.value ?: return
        val r = rows.coerceIn(1, 10)
        val c = cols.coerceIn(1, 8)
        val tableRows = List(r) { rowIdx ->
            List(c) { colIdx ->
                if (rowIdx == 0) "Header ${colIdx + 1}" else "Item ${rowIdx}-${colIdx + 1}"
            }
        }
        val tableBlock = DocumentBlock.Table(
            rows = tableRows,
            hasHeaderRow = true,
            style = style
        )
        insertBlockAfterCurrent(tableBlock)
    }

    fun updateTableCell(tableIndex: Int, rowIndex: Int, colIndex: Int, text: String) {
        val doc = _activeDocument.value ?: return
        val table = doc.blocks.getOrNull(tableIndex) as? DocumentBlock.Table ?: return
        val mutableRows = table.rows.map { it.toMutableList() }.toMutableList()
        if (rowIndex in mutableRows.indices && colIndex in mutableRows[rowIndex].indices) {
            mutableRows[rowIndex][colIndex] = text
            updateBlock(tableIndex, table.copy(rows = mutableRows))
        }
    }

    fun addTableRow(tableIndex: Int) {
        val doc = _activeDocument.value ?: return
        val table = doc.blocks.getOrNull(tableIndex) as? DocumentBlock.Table ?: return
        val colCount = table.rows.firstOrNull()?.size ?: 2
        val newRow = List(colCount) { "" }
        val mutableRows = table.rows.toMutableList()
        mutableRows.add(newRow)
        updateBlock(tableIndex, table.copy(rows = mutableRows))
    }

    fun deleteTableRow(tableIndex: Int, rowIndex: Int) {
        val doc = _activeDocument.value ?: return
        val table = doc.blocks.getOrNull(tableIndex) as? DocumentBlock.Table ?: return
        if (table.rows.size <= 1) return
        val mutableRows = table.rows.toMutableList()
        if (rowIndex in mutableRows.indices) {
            mutableRows.removeAt(rowIndex)
            updateBlock(tableIndex, table.copy(rows = mutableRows))
        }
    }

    fun addTableColumn(tableIndex: Int) {
        val doc = _activeDocument.value ?: return
        val table = doc.blocks.getOrNull(tableIndex) as? DocumentBlock.Table ?: return
        val mutableRows = table.rows.map { row ->
            row.toMutableList().also { it.add("") }
        }
        updateBlock(tableIndex, table.copy(rows = mutableRows))
    }

    fun deleteTableColumn(tableIndex: Int, colIndex: Int) {
        val doc = _activeDocument.value ?: return
        val table = doc.blocks.getOrNull(tableIndex) as? DocumentBlock.Table ?: return
        if ((table.rows.firstOrNull()?.size ?: 0) <= 1) return
        val mutableRows = table.rows.map { row ->
            row.toMutableList().also { if (colIndex in it.indices) it.removeAt(colIndex) }
        }
        updateBlock(tableIndex, table.copy(rows = mutableRows))
    }

    fun insertCallout(type: CalloutType, text: String) {
        val block = DocumentBlock.Callout(
            type = type,
            text = text,
            title = type.title
        )
        insertBlockAfterCurrent(block)
    }

    fun insertDivider(style: DividerStyle = DividerStyle.SOLID) {
        insertBlockAfterCurrent(DocumentBlock.Divider(style = style))
    }

    fun insertPageBreak() {
        insertBlockAfterCurrent(DocumentBlock.PageBreak())
    }

    fun insertSymbol(symbol: String) {
        val index = _selectedBlockIndex.value
        val doc = _activeDocument.value ?: return
        val p = doc.blocks.getOrNull(index) as? DocumentBlock.Paragraph
        if (p != null) {
            updateParagraphText(index, p.text + symbol)
        } else {
            addParagraphAfter(index)
            val newP = _activeDocument.value?.blocks?.getOrNull(index + 1) as? DocumentBlock.Paragraph
            if (newP != null) {
                updateParagraphText(index + 1, symbol)
            }
        }
    }

    fun insertCurrentDateTime() {
        val format = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()).format(Date())
        insertSymbol(" $format ")
    }

    private fun insertBlockAfterCurrent(block: DocumentBlock) {
        val doc = _activeDocument.value ?: return
        pushToUndo(doc.blocks)
        val currentIndex = _selectedBlockIndex.value
        val mutable = doc.blocks.toMutableList()
        val insertIndex = (currentIndex + 1).coerceAtMost(mutable.size)
        mutable.add(insertIndex, block)
        // Ensure another editable paragraph after special elements
        if (block !is DocumentBlock.Paragraph) {
            mutable.add(insertIndex + 1, DocumentBlock.Paragraph(text = ""))
        }
        _activeDocument.value = doc.copy(blocks = mutable)
        _selectedBlockIndex.value = insertIndex + 1
        triggerAutoSave()
    }

    // Page Layout Operations
    fun setPageOrientation(orientation: PageOrientation) {
        val doc = _activeDocument.value ?: return
        _activeDocument.value = doc.copy(orientation = orientation)
        triggerAutoSave()
    }

    fun setPageSize(size: DocPageSize) {
        val doc = _activeDocument.value ?: return
        _activeDocument.value = doc.copy(pageSize = size)
        triggerAutoSave()
    }

    fun setPageMargins(margins: PageMargins) {
        val doc = _activeDocument.value ?: return
        _activeDocument.value = doc.copy(margins = margins)
        triggerAutoSave()
    }

    fun setPageTone(tone: PageTone) {
        val doc = _activeDocument.value ?: return
        _activeDocument.value = doc.copy(pageTone = tone)
        triggerAutoSave()
    }

    fun setHeaderFooter(header: String, footer: String, showNumbers: Boolean) {
        val doc = _activeDocument.value ?: return
        _activeDocument.value = doc.copy(
            headerText = header,
            footerText = footer,
            showPageNumbers = showNumbers
        )
        triggerAutoSave()
    }

    // Ribbon & View state
    fun setRibbonTab(tab: RibbonTab) {
        _activeRibbonTab.value = tab
        _isRibbonExpanded.value = true
    }

    fun toggleRibbonExpanded() {
        _isRibbonExpanded.value = !_isRibbonExpanded.value
    }

    fun togglePrintLayout() {
        _isPrintLayout.value = !_isPrintLayout.value
    }

    fun setZoom(percent: Int) {
        _zoomPercent.value = percent.coerceIn(50, 200)
    }

    fun toggleRuler() {
        _showRuler.value = !_showRuler.value
    }

    // Dialog state controllers
    fun showStatsDialog(show: Boolean) { _isStatsDialogVisible.value = show }
    fun showSymbolDialog(show: Boolean) { _isSymbolDialogVisible.value = show }
    fun showTableDialog(show: Boolean) { _isTableDialogVisible.value = show }
    fun showPropertiesDialog(show: Boolean) { _isPropertiesDialogVisible.value = show }
    fun showHeaderFooterDialog(show: Boolean) { _isHeaderFooterDialogVisible.value = show }
    fun showFindReplace(show: Boolean) { 
        _isFindReplaceVisible.value = show 
        if (!show) {
            _findQuery.value = ""
            _replaceQuery.value = ""
            _searchMatchCount.value = 0
        }
    }

    // Find and Replace
    fun updateFindQuery(query: String) {
        _findQuery.value = query
        countMatches(query)
    }

    fun updateReplaceQuery(query: String) {
        _replaceQuery.value = query
    }

    private fun countMatches(query: String) {
        val doc = _activeDocument.value ?: return
        if (query.isBlank()) {
            _searchMatchCount.value = 0
            return
        }
        var count = 0
        for (block in doc.blocks) {
            when (block) {
                is DocumentBlock.Paragraph -> {
                    count += countOccurrences(block.text, query)
                }
                is DocumentBlock.Table -> {
                    for (row in block.rows) {
                        for (cell in row) {
                            count += countOccurrences(cell, query)
                        }
                    }
                }
                is DocumentBlock.Callout -> {
                    count += countOccurrences(block.text, query)
                }
                else -> {}
            }
        }
        _searchMatchCount.value = count
    }

    private fun countOccurrences(src: String, pattern: String): Int {
        if (pattern.isEmpty()) return 0
        var count = 0
        var idx = 0
        while (true) {
            idx = src.indexOf(pattern, idx, ignoreCase = true)
            if (idx == -1) break
            count++
            idx += pattern.length
        }
        return count
    }

    fun replaceAllOccurrences() {
        val doc = _activeDocument.value ?: return
        val find = _findQuery.value
        val replace = _replaceQuery.value
        if (find.isBlank()) return

        pushToUndo(doc.blocks)
        val newBlocks = doc.blocks.map { block ->
            when (block) {
                is DocumentBlock.Paragraph -> {
                    block.copy(text = block.text.replace(find, replace, ignoreCase = true))
                }
                is DocumentBlock.Table -> {
                    val newRows = block.rows.map { row ->
                        row.map { cell -> cell.replace(find, replace, ignoreCase = true) }
                    }
                    block.copy(rows = newRows)
                }
                is DocumentBlock.Callout -> {
                    block.copy(text = block.text.replace(find, replace, ignoreCase = true))
                }
                else -> block
            }
        }
        _activeDocument.value = doc.copy(blocks = newBlocks)
        countMatches(find)
        triggerAutoSave()
    }

    // Text to Speech
    fun toggleReadAloud() {
        if (_isSpeaking.value) {
            stopReading()
        } else {
            startReading()
        }
    }

    private fun startReading() {
        val doc = _activeDocument.value ?: return
        if (!isTtsReady || tts == null) return

        val textToRead = buildString {
            append(doc.title).append(". ")
            for (block in doc.blocks) {
                when (block) {
                    is DocumentBlock.Paragraph -> if (block.text.isNotBlank()) append(block.text).append(". ")
                    is DocumentBlock.Callout -> if (block.text.isNotBlank()) append(block.text).append(". ")
                    else -> {}
                }
            }
        }

        if (textToRead.isNotBlank()) {
            tts?.speak(textToRead, TextToSpeech.QUEUE_FLUSH, null, "DOC_READ")
            _isSpeaking.value = true
        }
    }

    fun stopReading() {
        tts?.stop()
        _isSpeaking.value = false
    }

    // Repository operations from Home Screen
    fun deleteDocument(id: String) {
        viewModelScope.launch {
            repository.deleteDocument(id)
        }
    }

    fun togglePin(doc: WordDocument) {
        viewModelScope.launch {
            repository.togglePin(doc.id, doc.isPinned)
        }
    }

    fun duplicateDocument(doc: WordDocument) {
        viewModelScope.launch {
            repository.duplicateDocument(doc)
        }
    }

    // Document Exporters
    fun exportToPlainText(): String {
        val doc = _activeDocument.value ?: return ""
        return buildString {
            appendLine(doc.title.uppercase())
            appendLine("=".repeat(doc.title.length.coerceAtLeast(10)))
            appendLine()
            for (block in doc.blocks) {
                when (block) {
                    is DocumentBlock.Paragraph -> {
                        val prefix = when (block.listType) {
                            ListType.BULLET -> " • "
                            ListType.NUMBERED -> " 1. "
                            ListType.CHECKLIST -> if (block.isChecked) " [x] " else " [ ] "
                            else -> "  ".repeat(block.indentLevel)
                        }
                        appendLine("$prefix${block.text}")
                    }
                    is DocumentBlock.Table -> {
                        appendLine("[Table: ${block.caption.ifBlank { "Data Grid" }}]")
                        for (row in block.rows) {
                            appendLine("| " + row.joinToString(" | ") + " |")
                        }
                        appendLine()
                    }
                    is DocumentBlock.Callout -> {
                        appendLine("[${block.title.uppercase()}]: ${block.text}")
                        appendLine()
                    }
                    is DocumentBlock.Divider -> appendLine("----------------------------------------")
                    is DocumentBlock.PageBreak -> appendLine("=== [ PAGE BREAK ] ===")
                }
            }
        }
    }

    fun exportToMarkdown(): String {
        val doc = _activeDocument.value ?: return ""
        return buildString {
            appendLine("# ${doc.title}")
            appendLine()
            for (block in doc.blocks) {
                when (block) {
                    is DocumentBlock.Paragraph -> {
                        val prefix = when (block.style) {
                            BlockStyle.TITLE -> "# "
                            BlockStyle.HEADING_1 -> "## "
                            BlockStyle.HEADING_2 -> "### "
                            BlockStyle.HEADING_3 -> "#### "
                            BlockStyle.QUOTE -> "> "
                            BlockStyle.CODE -> "    "
                            else -> when (block.listType) {
                                ListType.BULLET -> "* "
                                ListType.NUMBERED -> "1. "
                                ListType.CHECKLIST -> if (block.isChecked) "- [x] " else "- [ ] "
                                else -> ""
                            }
                        }
                        var formattedText = block.text
                        if (block.isBold) formattedText = "**$formattedText**"
                        if (block.isItalic) formattedText = "*$formattedText*"
                        appendLine("$prefix$formattedText")
                        appendLine()
                    }
                    is DocumentBlock.Table -> {
                        if (block.rows.isNotEmpty()) {
                            val header = block.rows.first()
                            appendLine("| " + header.joinToString(" | ") + " |")
                            appendLine("| " + header.map { "---" }.joinToString(" | ") + " |")
                            for (row in block.rows.drop(1)) {
                                appendLine("| " + row.joinToString(" | ") + " |")
                            }
                            appendLine()
                        }
                    }
                    is DocumentBlock.Callout -> {
                        appendLine("> **${block.title}**: ${block.text}")
                        appendLine()
                    }
                    is DocumentBlock.Divider -> {
                        appendLine("---")
                        appendLine()
                    }
                    is DocumentBlock.PageBreak -> {
                        appendLine("<div style=\"page-break-after: always;\"></div>")
                        appendLine()
                    }
                }
            }
        }
    }

    fun exportToHtml(): String {
        val doc = _activeDocument.value ?: return ""
        return buildString {
            appendLine("<!DOCTYPE html>")
            appendLine("<html><head><meta charset=\"utf-8\"><title>${doc.title}</title>")
            appendLine("<style>")
            appendLine("body { font-family: Calibri, Arial, sans-serif; margin: 40px auto; max-width: 800px; line-height: 1.6; color: #333; }")
            appendLine("table { width: 100%; border-collapse: collapse; margin: 16px 0; }")
            appendLine("th, td { border: 1px solid #ccc; padding: 8px 12px; text-align: left; }")
            appendLine("th { background-color: #185ABD; color: white; }")
            appendLine(".callout { padding: 12px; margin: 16px 0; border-left: 4px solid #185ABD; background: #f0f5fa; }")
            appendLine("</style></head><body>")
            appendLine("<h1>${doc.title}</h1>")
            for (block in doc.blocks) {
                when (block) {
                    is DocumentBlock.Paragraph -> {
                        val tag = when (block.style) {
                            BlockStyle.TITLE -> "h1"
                            BlockStyle.HEADING_1 -> "h2"
                            BlockStyle.HEADING_2 -> "h3"
                            BlockStyle.HEADING_3 -> "h4"
                            BlockStyle.QUOTE -> "blockquote"
                            BlockStyle.CODE -> "pre"
                            else -> "p"
                        }
                        val alignStyle = " style=\"text-align:${block.alignment.name.lowercase()};\""
                        var content = block.text
                        if (block.isBold) content = "<strong>$content</strong>"
                        if (block.isItalic) content = "<em>$content</em>"
                        if (block.isUnderline) content = "<u>$content</u>"
                        appendLine("<$tag$alignStyle>$content</$tag>")
                    }
                    is DocumentBlock.Table -> {
                        appendLine("<table>")
                        block.rows.forEachIndexed { rIdx, row ->
                            appendLine("<tr>")
                            val cellTag = if (rIdx == 0 && block.hasHeaderRow) "th" else "td"
                            for (cell in row) {
                                appendLine("<$cellTag>$cell</$cellTag>")
                            }
                            appendLine("</tr>")
                        }
                        appendLine("</table>")
                    }
                    is DocumentBlock.Callout -> {
                        appendLine("<div class=\"callout\"><strong>${block.title}:</strong> ${block.text}</div>")
                    }
                    is DocumentBlock.Divider -> appendLine("<hr/>")
                    is DocumentBlock.PageBreak -> appendLine("<div style=\"page-break-after: always;\"></div>")
                }
            }
            appendLine("</body></html>")
        }
    }
}
