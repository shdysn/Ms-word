package com.example.data.model

import java.util.UUID

enum class PageOrientation {
    PORTRAIT, LANDSCAPE
}

enum class DocPageSize(val displayName: String, val widthMm: Int, val heightMm: Int) {
    A4("A4 (210 × 297 mm)", 210, 297),
    LETTER("US Letter (8.5 × 11 in)", 216, 279),
    LEGAL("US Legal (8.5 × 14 in)", 216, 356),
    A5("A5 (148 × 210 mm)", 148, 210)
}

enum class PageMargins(val displayName: String, val paddingDp: Int) {
    NORMAL("Normal (24dp)", 24),
    NARROW("Narrow (12dp)", 12),
    MODERATE("Moderate (18dp)", 18),
    WIDE("Wide (32dp)", 32)
}

enum class PageTone(val displayName: String, val hexColor: Long, val isDark: Boolean = false) {
    WHITE("Classic White", 0xFFFFFFFF),
    CREAM("Warm Cream", 0xFFFAF7EE),
    PARCHMENT("Parchment", 0xFFF5EFE6),
    SOFT_BLUE("Office Blue", 0xFFF0F5FA),
    NIGHT("Dark Paper", 0xFF1E1E1E, isDark = true)
}

enum class BlockStyle(val displayName: String, val defaultFontSizePt: Int, val isDefaultBold: Boolean = false) {
    NORMAL("Normal", 11, false),
    TITLE("Title", 26, true),
    SUBTITLE("Subtitle", 16, false),
    HEADING_1("Heading 1", 20, true),
    HEADING_2("Heading 2", 15, true),
    HEADING_3("Heading 3", 13, true),
    QUOTE("Quote", 12, false),
    CODE("Code", 10, false)
}

enum class BlockAlignment {
    LEFT, CENTER, RIGHT, JUSTIFY
}

enum class ListType {
    NONE, BULLET, NUMBERED, ALPHABETIC, ROMAN, CHECKLIST
}

enum class FontFamilyChoice(val displayName: String) {
    CALIBRI("Calibri"),
    TIMES_NEW_ROMAN("Times New Roman"),
    ARIAL("Arial"),
    GEORGIA("Georgia"),
    COURIER_NEW("Courier New"),
    ROBOTO("Roboto")
}

enum class CalloutType(val title: String, val bgHex: Long, val borderHex: Long) {
    INFO("Note", 0xFFEBF3FB, 0xFF0078D4),
    TIP("Tip", 0xFFEAF5EA, 0xFF107C41),
    WARNING("Important", 0xFFFFF4CE, 0xFF797775),
    QUOTE("Insight", 0xFFF3F2F1, 0xFF8A8886)
}

enum class DividerStyle {
    SOLID, DASHED, DOUBLE
}

enum class TableStyle(val headerBgHex: Long, val altRowBgHex: Long) {
    BLUE_OFFICE(0xFF185ABD, 0xFFF0F5FA),
    CHARCOAL(0xFF323130, 0xFFF3F2F1),
    MINIMAL(0xFFFFFFFF, 0xFFFFFFFF),
    STRIPED(0xFF0078D4, 0xFFF9FAFB)
}

sealed class DocumentBlock {
    abstract val id: String

    data class Paragraph(
        override val id: String = UUID.randomUUID().toString(),
        val text: String = "",
        val style: BlockStyle = BlockStyle.NORMAL,
        val alignment: BlockAlignment = BlockAlignment.LEFT,
        val lineSpacingMultiplier: Float = 1.15f,
        val isBold: Boolean = false,
        val isItalic: Boolean = false,
        val isUnderline: Boolean = false,
        val isStrikethrough: Boolean = false,
        val isSubscript: Boolean = false,
        val isSuperscript: Boolean = false,
        val textColorHex: Long = 0xFF201F1E,
        val highlightColorHex: Long = 0x00000000,
        val fontSizePt: Int = 11,
        val fontFamily: FontFamilyChoice = FontFamilyChoice.CALIBRI,
        val indentLevel: Int = 0,
        val listType: ListType = ListType.NONE,
        val isChecked: Boolean = false
    ) : DocumentBlock()

    data class Table(
        override val id: String = UUID.randomUUID().toString(),
        val rows: List<List<String>> = listOf(
            listOf("Column 1", "Column 2", "Column 3"),
            listOf("Data A", "Data B", "Data C"),
            listOf("Data D", "Data E", "Data F")
        ),
        val hasHeaderRow: Boolean = true,
        val style: TableStyle = TableStyle.BLUE_OFFICE,
        val caption: String = ""
    ) : DocumentBlock()

    data class Callout(
        override val id: String = UUID.randomUUID().toString(),
        val text: String = "Important note or announcement goes here.",
        val type: CalloutType = CalloutType.INFO,
        val title: String = "Note"
    ) : DocumentBlock()

    data class Divider(
        override val id: String = UUID.randomUUID().toString(),
        val style: DividerStyle = DividerStyle.SOLID
    ) : DocumentBlock()

    data class PageBreak(
        override val id: String = UUID.randomUUID().toString()
    ) : DocumentBlock()
}

data class WordDocument(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "Untitled Document",
    val blocks: List<DocumentBlock> = listOf(DocumentBlock.Paragraph(text = "")),
    val orientation: PageOrientation = PageOrientation.PORTRAIT,
    val pageSize: DocPageSize = DocPageSize.A4,
    val margins: PageMargins = PageMargins.NORMAL,
    val pageTone: PageTone = PageTone.WHITE,
    val headerText: String = "",
    val footerText: String = "",
    val showPageNumbers: Boolean = true,
    val isPinned: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val author: String = "User"
)

data class DocumentStats(
    val words: Int,
    val charactersWithSpaces: Int,
    val charactersWithoutSpaces: Int,
    val paragraphs: Int,
    val lines: Int,
    val readingTimeMinutes: Int,
    val speakingTimeMinutes: Int,
    val estimatedPages: Int
)

fun calculateDocumentStats(document: WordDocument): DocumentStats {
    var words = 0
    var charsWithSpaces = 0
    var charsWithoutSpaces = 0
    var paragraphs = 0
    var lines = 0

    for (block in document.blocks) {
        when (block) {
            is DocumentBlock.Paragraph -> {
                val t = block.text
                if (t.isNotBlank()) {
                    paragraphs++
                    val blockWords = t.trim().split("\\s+".toRegex()).filter { it.isNotEmpty() }
                    words += blockWords.size
                    charsWithSpaces += t.length
                    charsWithoutSpaces += t.count { !it.isWhitespace() }
                    lines += (t.length / 70).coerceAtLeast(1)
                }
            }
            is DocumentBlock.Table -> {
                for (row in block.rows) {
                    for (cell in row) {
                        if (cell.isNotBlank()) {
                            val cellWords = cell.trim().split("\\s+".toRegex()).filter { it.isNotEmpty() }
                            words += cellWords.size
                            charsWithSpaces += cell.length
                            charsWithoutSpaces += cell.count { !it.isWhitespace() }
                        }
                    }
                }
                lines += block.rows.size
            }
            is DocumentBlock.Callout -> {
                val t = block.text
                if (t.isNotBlank()) {
                    paragraphs++
                    val calloutWords = t.trim().split("\\s+".toRegex()).filter { it.isNotEmpty() }
                    words += calloutWords.size
                    charsWithSpaces += t.length
                    charsWithoutSpaces += t.count { !it.isWhitespace() }
                    lines += 2
                }
            }
            is DocumentBlock.Divider -> lines++
            is DocumentBlock.PageBreak -> lines += 5
        }
    }

    val readingTime = ((words / 200.0).coerceAtLeast(0.5)).toInt().coerceAtLeast(if (words > 0) 1 else 0)
    val speakingTime = ((words / 130.0).coerceAtLeast(0.5)).toInt().coerceAtLeast(if (words > 0) 1 else 0)
    val pages = (lines / 35).coerceAtLeast(1)

    return DocumentStats(
        words = words,
        charactersWithSpaces = charsWithSpaces,
        charactersWithoutSpaces = charsWithoutSpaces,
        paragraphs = paragraphs,
        lines = lines.coerceAtLeast(1),
        readingTimeMinutes = readingTime,
        speakingTimeMinutes = speakingTime,
        estimatedPages = pages
    )
}
