package com.example.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.WordViewModel
import com.example.ui.theme.WordBluePrimary

@Composable
fun DocumentCanvas(
    viewModel: WordViewModel,
    doc: WordDocument,
    modifier: Modifier = Modifier
) {
    val selectedIndex by viewModel.selectedBlockIndex.collectAsState()
    val isPrintLayout by viewModel.isPrintLayout.collectAsState()
    val zoom by viewModel.zoomPercent.collectAsState()
    val showRuler by viewModel.showRuler.collectAsState()

    val scaleFactor = zoom / 100f
    val paperColor = Color(doc.pageTone.hexColor)
    val contentPadding = doc.margins.paddingDp.dp

    Column(modifier = modifier.fillMaxSize()) {
        // Document Top Ruler
        if (showRuler) {
            DocumentRuler(scaleFactor = scaleFactor)
        }

        // Main Paper Canvas area
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (doc.pageTone.isDark) MaterialTheme.colorScheme.background
                    else MaterialTheme.colorScheme.background
                ),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        horizontal = if (isPrintLayout) 16.dp else 4.dp,
                        vertical = if (isPrintLayout) 16.dp else 4.dp
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                item {
                    // Document Page Sheet Card
                    Surface(
                        modifier = Modifier
                            .widthIn(max = if (doc.orientation == PageOrientation.LANDSCAPE) 900.dp else 680.dp)
                            .fillMaxWidth()
                            .then(
                                if (isPrintLayout) {
                                    Modifier.shadow(8.dp, RoundedCornerShape(4.dp))
                                } else Modifier
                            ),
                        shape = RoundedCornerShape(if (isPrintLayout) 4.dp else 0.dp),
                        color = paperColor,
                        tonalElevation = if (isPrintLayout) 2.dp else 0.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(contentPadding)
                        ) {
                            // Header Text & Page numbering
                            if (doc.headerText.isNotBlank() || isPrintLayout) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        doc.headerText.ifBlank { " " },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (doc.pageTone.isDark) Color.LightGray else Color.Gray,
                                        fontSize = (9 * scaleFactor).sp
                                    )
                                    if (doc.showPageNumbers) {
                                        Text(
                                            "Page 1",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (doc.pageTone.isDark) Color.LightGray else Color.Gray,
                                            fontSize = (9 * scaleFactor).sp
                                        )
                                    }
                                }
                                HorizontalDivider(
                                    color = if (doc.pageTone.isDark) Color.DarkGray else Color.LightGray.copy(alpha = 0.5f),
                                    thickness = 0.5.dp
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                            }

                            // Render Each Document Block
                            doc.blocks.forEachIndexed { index, block ->
                                val isSelected = index == selectedIndex
                                BlockRenderer(
                                    block = block,
                                    index = index,
                                    isSelected = isSelected,
                                    scaleFactor = scaleFactor,
                                    isDarkPaper = doc.pageTone.isDark,
                                    onSelect = { viewModel.selectBlock(index) },
                                    onTextChange = { newText -> viewModel.updateParagraphText(index, newText) },
                                    onDelete = { viewModel.removeBlock(index) },
                                    onMoveUp = { viewModel.moveBlock(index, true) },
                                    onMoveDown = { viewModel.moveBlock(index, false) },
                                    onAddAfter = { viewModel.addParagraphAfter(index) },
                                    onToggleCheck = { viewModel.toggleChecklist(index) },
                                    onCellChange = { r, c, txt -> viewModel.updateTableCell(index, r, c, txt) },
                                    onAddTableRow = { viewModel.addTableRow(index) },
                                    onDeleteTableRow = { r -> viewModel.deleteTableRow(index, r) },
                                    onAddTableCol = { viewModel.addTableColumn(index) },
                                    onDeleteTableCol = { c -> viewModel.deleteTableColumn(index, c) }
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                            }

                            // Footer Text
                            if (doc.footerText.isNotBlank() || isPrintLayout) {
                                Spacer(modifier = Modifier.height(24.dp))
                                HorizontalDivider(
                                    color = if (doc.pageTone.isDark) Color.DarkGray else Color.LightGray.copy(alpha = 0.5f),
                                    thickness = 0.5.dp
                                )
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        doc.footerText.ifBlank { " " },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (doc.pageTone.isDark) Color.LightGray else Color.Gray,
                                        fontSize = (9 * scaleFactor).sp
                                    )
                                    Text(
                                        "${doc.pageSize.name} • ${doc.orientation.name.lowercase().replaceFirstChar { it.uppercase() }}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (doc.pageTone.isDark) Color.LightGray else Color.Gray,
                                        fontSize = (8 * scaleFactor).sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Quick Floating Bottom Bar for One-Tap Editing
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                shape = CircleShape,
                color = WordBluePrimary,
                shadowElevation = 6.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = { viewModel.toggleBold() },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Text("B", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                    }
                    IconButton(
                        onClick = { viewModel.toggleItalic() },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Text("I", fontStyle = FontStyle.Italic, color = Color.White, fontSize = 16.sp)
                    }
                    IconButton(
                        onClick = { viewModel.toggleUnderline() },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Text("U", color = Color.White, fontSize = 16.sp, textDecoration = TextDecoration.Underline)
                    }
                    VerticalDivider(modifier = Modifier.height(20.dp), color = Color.White.copy(alpha = 0.5f))
                    IconButton(
                        onClick = { viewModel.addParagraphAfter(selectedIndex) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Paragraph", tint = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun DocumentRuler(scaleFactor: Float) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(22.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            for (inch in 1..10) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        "$inch",
                        fontSize = (8 * scaleFactor).sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Box(
                        modifier = Modifier
                            .height(10.dp)
                            .width(1.dp)
                            .background(Color.Gray)
                    )
                }
            }
        }
    }
}

@Composable
private fun BlockRenderer(
    block: DocumentBlock,
    index: Int,
    isSelected: Boolean,
    scaleFactor: Float,
    isDarkPaper: Boolean,
    onSelect: () -> Unit,
    onTextChange: (String) -> Unit,
    onDelete: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onAddAfter: () -> Unit,
    onToggleCheck: () -> Unit,
    onCellChange: (Int, Int, String) -> Unit,
    onAddTableRow: () -> Unit,
    onDeleteTableRow: (Int) -> Unit,
    onAddTableCol: () -> Unit,
    onDeleteTableCol: (Int) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(
                if (isSelected) WordBluePrimary.copy(alpha = 0.06f)
                else Color.Transparent
            )
            .border(
                width = if (isSelected) 1.dp else 0.dp,
                color = if (isSelected) WordBluePrimary.copy(alpha = 0.4f) else Color.Transparent,
                shape = RoundedCornerShape(4.dp)
            )
            .clickable(onClick = onSelect)
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            when (block) {
                is DocumentBlock.Paragraph -> {
                    ParagraphRenderer(
                        paragraph = block,
                        scaleFactor = scaleFactor,
                        isDarkPaper = isDarkPaper,
                        onTextChange = onTextChange,
                        onToggleCheck = onToggleCheck
                    )
                }
                is DocumentBlock.Table -> {
                    TableRenderer(
                        table = block,
                        scaleFactor = scaleFactor,
                        onCellChange = onCellChange,
                        onAddRow = onAddTableRow,
                        onDeleteRow = onDeleteTableRow,
                        onAddCol = onAddTableCol,
                        onDeleteCol = onDeleteTableCol
                    )
                }
                is DocumentBlock.Callout -> {
                    CalloutRenderer(
                        callout = block,
                        scaleFactor = scaleFactor,
                        onTextChange = onTextChange
                    )
                }
                is DocumentBlock.Divider -> {
                    DividerRenderer(style = block.style)
                }
                is DocumentBlock.PageBreak -> {
                    PageBreakRenderer()
                }
            }

            // Controls on Selected Block
            if (isSelected) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onMoveUp, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.ArrowUpward, contentDescription = "Move Up", modifier = Modifier.size(14.dp))
                    }
                    IconButton(onClick = onMoveDown, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.ArrowDownward, contentDescription = "Move Down", modifier = Modifier.size(14.dp))
                    }
                    IconButton(onClick = onAddAfter, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Add, contentDescription = "Add Below", modifier = Modifier.size(14.dp), tint = WordBluePrimary)
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
private fun ParagraphRenderer(
    paragraph: DocumentBlock.Paragraph,
    scaleFactor: Float,
    isDarkPaper: Boolean,
    onTextChange: (String) -> Unit,
    onToggleCheck: () -> Unit
) {
    val fontFamily = when (paragraph.fontFamily) {
        FontFamilyChoice.CALIBRI, FontFamilyChoice.ARIAL -> FontFamily.SansSerif
        FontFamilyChoice.TIMES_NEW_ROMAN, FontFamilyChoice.GEORGIA -> FontFamily.Serif
        FontFamilyChoice.COURIER_NEW -> FontFamily.Monospace
        FontFamilyChoice.ROBOTO -> FontFamily.Default
    }

    val textAlign = when (paragraph.alignment) {
        BlockAlignment.LEFT -> TextAlign.Start
        BlockAlignment.CENTER -> TextAlign.Center
        BlockAlignment.RIGHT -> TextAlign.End
        BlockAlignment.JUSTIFY -> TextAlign.Justify
    }

    val textDecoration = when {
        paragraph.isUnderline && paragraph.isStrikethrough -> TextDecoration.combine(listOf(TextDecoration.Underline, TextDecoration.LineThrough))
        paragraph.isUnderline -> TextDecoration.Underline
        paragraph.isStrikethrough -> TextDecoration.LineThrough
        else -> TextDecoration.None
    }

    val effectiveFontSize = (paragraph.fontSizePt * scaleFactor).sp
    val fontColor = if (isDarkPaper && paragraph.textColorHex == 0xFF201F1EL) Color.White else Color(paragraph.textColorHex)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = (paragraph.indentLevel * 24).dp),
        verticalAlignment = Alignment.Top
    ) {
        // List Prefix
        when (paragraph.listType) {
            ListType.BULLET -> {
                Text(
                    "• ",
                    fontWeight = FontWeight.Bold,
                    fontSize = effectiveFontSize,
                    color = fontColor,
                    modifier = Modifier.padding(end = 4.dp)
                )
            }
            ListType.NUMBERED -> {
                Text(
                    "1. ",
                    fontWeight = FontWeight.Medium,
                    fontSize = effectiveFontSize,
                    color = fontColor,
                    modifier = Modifier.padding(end = 4.dp)
                )
            }
            ListType.CHECKLIST -> {
                IconButton(
                    onClick = onToggleCheck,
                    modifier = Modifier
                        .size(24.dp)
                        .padding(end = 4.dp)
                ) {
                    Icon(
                        imageVector = if (paragraph.isChecked) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                        contentDescription = "Check task",
                        tint = if (paragraph.isChecked) WordBluePrimary else Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            else -> {}
        }

        // Editable Text Field
        Box(
            modifier = Modifier
                .weight(1f)
                .background(
                    if (paragraph.highlightColorHex != 0L) Color(paragraph.highlightColorHex)
                    else Color.Transparent
                )
        ) {
            BasicTextField(
                value = paragraph.text,
                onValueChange = onTextChange,
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(
                    fontFamily = fontFamily,
                    fontSize = effectiveFontSize,
                    fontWeight = if (paragraph.isBold) FontWeight.Bold else FontWeight.Normal,
                    fontStyle = if (paragraph.isItalic) FontStyle.Italic else FontStyle.Normal,
                    textDecoration = textDecoration,
                    textAlign = textAlign,
                    color = fontColor,
                    lineHeight = (paragraph.fontSizePt * paragraph.lineSpacingMultiplier * scaleFactor * 1.3f).sp
                ),
                cursorBrush = SolidColor(WordBluePrimary),
                decorationBox = { innerTextField ->
                    if (paragraph.text.isEmpty()) {
                        Text(
                            text = if (paragraph.style == BlockStyle.TITLE) "Type document title..." else "Type text here...",
                            color = Color.Gray.copy(alpha = 0.6f),
                            fontSize = effectiveFontSize,
                            textAlign = textAlign
                        )
                    }
                    innerTextField()
                }
            )
        }
    }
}

@Composable
private fun TableRenderer(
    table: DocumentBlock.Table,
    scaleFactor: Float,
    onCellChange: (Int, Int, String) -> Unit,
    onAddRow: () -> Unit,
    onDeleteRow: (Int) -> Unit,
    onAddCol: () -> Unit,
    onDeleteCol: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        if (table.caption.isNotBlank()) {
            Text(
                table.caption,
                fontWeight = FontWeight.Bold,
                fontSize = (11 * scaleFactor).sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        // Table grid
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(table.style.headerBgHex), RoundedCornerShape(4.dp))
                .clip(RoundedCornerShape(4.dp))
        ) {
            table.rows.forEachIndexed { rIdx, row ->
                val isHeader = rIdx == 0 && table.hasHeaderRow
                val rowBg = when {
                    isHeader -> Color(table.style.headerBgHex)
                    rIdx % 2 == 1 -> Color(table.style.altRowBgHex)
                    else -> Color.White
                }
                val textColor = if (isHeader) Color.White else Color.Black

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(rowBg)
                ) {
                    row.forEachIndexed { cIdx, cellValue ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .border(0.5.dp, Color.LightGray)
                                .padding(horizontal = 6.dp, vertical = 6.dp)
                        ) {
                            BasicTextField(
                                value = cellValue,
                                onValueChange = { onCellChange(rIdx, cIdx, it) },
                                modifier = Modifier.fillMaxWidth(),
                                textStyle = TextStyle(
                                    fontSize = (10 * scaleFactor).sp,
                                    fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
                                    color = textColor
                                ),
                                cursorBrush = SolidColor(if (isHeader) Color.White else WordBluePrimary)
                            )
                        }
                    }
                }
            }
        }

        // Table management buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onAddRow,
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                modifier = Modifier.height(28.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Row", fontSize = 10.sp)
            }
            OutlinedButton(
                onClick = onAddCol,
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                modifier = Modifier.height(28.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Column", fontSize = 10.sp)
            }
            if (table.rows.size > 1) {
                TextButton(
                    onClick = { onDeleteRow(table.rows.lastIndex) },
                    contentPadding = PaddingValues(horizontal = 4.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text("Delete Last Row", fontSize = 10.sp, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun CalloutRenderer(
    callout: DocumentBlock.Callout,
    scaleFactor: Float,
    onTextChange: (String) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(6.dp),
        color = Color(callout.type.bgHex),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(callout.type.borderHex))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            val icon = when (callout.type) {
                CalloutType.INFO -> Icons.Default.Info
                CalloutType.TIP -> Icons.Default.Lightbulb
                CalloutType.WARNING -> Icons.Default.Warning
                CalloutType.QUOTE -> Icons.Default.FormatQuote
            }
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(callout.type.borderHex),
                modifier = Modifier
                    .size(22.dp)
                    .padding(top = 2.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    callout.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = (11 * scaleFactor).sp,
                    color = Color(callout.type.borderHex)
                )
                Spacer(modifier = Modifier.height(2.dp))
                BasicTextField(
                    value = callout.text,
                    onValueChange = onTextChange,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(
                        fontSize = (11 * scaleFactor).sp,
                        color = Color.Black
                    ),
                    cursorBrush = SolidColor(WordBluePrimary)
                )
            }
        }
    }
}

@Composable
private fun DividerRenderer(style: DividerStyle) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        when (style) {
            DividerStyle.SOLID -> HorizontalDivider(thickness = 1.dp, color = Color.Gray)
            DividerStyle.DOUBLE -> {
                Column {
                    HorizontalDivider(thickness = 1.dp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(2.dp))
                    HorizontalDivider(thickness = 1.dp, color = Color.Gray)
                }
            }
            DividerStyle.DASHED -> HorizontalDivider(thickness = 1.5.dp, color = Color.Gray)
        }
    }
}

@Composable
private fun PageBreakRenderer() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = WordBluePrimary.copy(alpha = 0.5f))
        Text(
            "   [ PAGE BREAK ]   ",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = WordBluePrimary
        )
        HorizontalDivider(modifier = Modifier.weight(1f), color = WordBluePrimary.copy(alpha = 0.5f))
    }
}
