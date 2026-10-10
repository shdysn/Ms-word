package com.example.ui.editor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.RibbonTab
import com.example.ui.WordViewModel
import com.example.ui.theme.*

@Composable
fun RibbonBar(
    viewModel: WordViewModel,
    modifier: Modifier = Modifier
) {
    val activeTab by viewModel.activeRibbonTab.collectAsState()
    val isExpanded by viewModel.isRibbonExpanded.collectAsState()
    val activeDoc by viewModel.activeDocument.collectAsState()
    val selectedIndex by viewModel.selectedBlockIndex.collectAsState()
    val selectedBlock = activeDoc?.blocks?.getOrNull(selectedIndex)
    val selectedParagraph = selectedBlock as? DocumentBlock.Paragraph

    val canUndo by viewModel.canUndo.collectAsState()
    val canRedo by viewModel.canRedo.collectAsState()

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp,
        shadowElevation = 3.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Tab Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ScrollableTabRow(
                    selectedTabIndex = activeTab.ordinal,
                    modifier = Modifier.weight(1f),
                    edgePadding = 8.dp,
                    containerColor = Color.Transparent,
                    contentColor = WordBluePrimary
                ) {
                    RibbonTab.values().forEach { tab ->
                        Tab(
                            selected = activeTab == tab,
                            onClick = { viewModel.setRibbonTab(tab) },
                            text = {
                                Text(
                                    tab.labelEn,
                                    fontWeight = if (activeTab == tab) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.sp
                                )
                            }
                        )
                    }
                }

                IconButton(
                    onClick = { viewModel.toggleRibbonExpanded() },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Collapse Ribbon" else "Expand Ribbon",
                        tint = WordBluePrimary
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Expandable Ribbon Body
            AnimatedVisibility(visible = isExpanded) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    when (activeTab) {
                        RibbonTab.HOME -> HomeTabContent(
                            viewModel = viewModel,
                            paragraph = selectedParagraph,
                            canUndo = canUndo,
                            canRedo = canRedo
                        )
                        RibbonTab.INSERT -> InsertTabContent(viewModel = viewModel)
                        RibbonTab.LAYOUT -> LayoutTabContent(
                            doc = activeDoc,
                            viewModel = viewModel
                        )
                        RibbonTab.REVIEW -> ReviewTabContent(viewModel = viewModel)
                        RibbonTab.VIEW -> ViewTabContent(viewModel = viewModel)
                        RibbonTab.FILE -> FileTabContent(viewModel = viewModel)
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeTabContent(
    viewModel: WordViewModel,
    paragraph: DocumentBlock.Paragraph?,
    canUndo: Boolean,
    canRedo: Boolean
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Section 1: Undo / Redo
        RibbonGroup(title = "History") {
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                RibbonIconButton(
                    icon = Icons.AutoMirrored.Filled.Undo,
                    label = "Undo",
                    enabled = canUndo,
                    onClick = { viewModel.undo() }
                )
                RibbonIconButton(
                    icon = Icons.AutoMirrored.Filled.Redo,
                    label = "Redo",
                    enabled = canRedo,
                    onClick = { viewModel.redo() }
                )
            }
        }

        RibbonVerticalDivider()

        // Section 2: Font Family & Size
        RibbonGroup(title = "Font") {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Font Family Dropdown
                var fontMenuExpanded by remember { mutableStateOf(false) }
                val currentFont = paragraph?.fontFamily ?: FontFamilyChoice.CALIBRI

                Box {
                    OutlinedButton(
                        onClick = { fontMenuExpanded = true },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text(currentFont.displayName, fontSize = 11.sp)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                    DropdownMenu(
                        expanded = fontMenuExpanded,
                        onDismissRequest = { fontMenuExpanded = false }
                    ) {
                        FontFamilyChoice.values().forEach { font ->
                            DropdownMenuItem(
                                text = { Text(font.displayName) },
                                onClick = {
                                    viewModel.setFontFamily(font)
                                    fontMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                // Font Size stepper
                val currentSize = paragraph?.fontSizePt ?: 11
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { viewModel.changeFontSizeBy(-1) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrease Font", modifier = Modifier.size(16.dp))
                    }
                    Text(
                        "$currentSize",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                    IconButton(
                        onClick = { viewModel.changeFontSizeBy(1) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Increase Font", modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // B, I, U, S, X2, X^2
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                RibbonToggleIconButton(
                    icon = Icons.Default.FormatBold,
                    label = "B",
                    isSelected = paragraph?.isBold == true,
                    onClick = { viewModel.toggleBold() }
                )
                RibbonToggleIconButton(
                    icon = Icons.Default.FormatItalic,
                    label = "I",
                    isSelected = paragraph?.isItalic == true,
                    onClick = { viewModel.toggleItalic() }
                )
                RibbonToggleIconButton(
                    icon = Icons.Default.FormatUnderlined,
                    label = "U",
                    isSelected = paragraph?.isUnderline == true,
                    onClick = { viewModel.toggleUnderline() }
                )
                RibbonToggleIconButton(
                    icon = Icons.Default.FormatStrikethrough,
                    label = "S",
                    isSelected = paragraph?.isStrikethrough == true,
                    onClick = { viewModel.toggleStrikethrough() }
                )
                RibbonToggleIconButton(
                    icon = Icons.Default.Subscript,
                    label = "X₂",
                    isSelected = paragraph?.isSubscript == true,
                    onClick = { viewModel.toggleSubscript() }
                )
                RibbonToggleIconButton(
                    icon = Icons.Default.Superscript,
                    label = "X²",
                    isSelected = paragraph?.isSuperscript == true,
                    onClick = { viewModel.toggleSuperscript() }
                )

                // Font Color Palette popup
                var showColorMenu by remember { mutableStateOf(false) }
                Box {
                    IconButton(
                        onClick = { showColorMenu = true },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(Icons.Default.FormatColorText, contentDescription = "Text Color", tint = Color(paragraph?.textColorHex ?: 0xFF201F1E))
                    }
                    DropdownMenu(
                        expanded = showColorMenu,
                        onDismissRequest = { showColorMenu = false }
                    ) {
                        Text("Font Colors", fontWeight = FontWeight.Bold, modifier = Modifier.padding(8.dp), fontSize = 12.sp)
                        val colors = listOf(
                            0xFF201F1E to "Black",
                            0xFF185ABD to "Word Blue",
                            0xFFD83B01 to "Dark Orange",
                            0xFF107C41 to "Forest Green",
                            0xFFB146C2 to "Purple",
                            0xFFA4262C to "Dark Red"
                        )
                        colors.forEach { (colorLong, name) ->
                            DropdownMenuItem(
                                leadingIcon = {
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(Color(colorLong))
                                    )
                                },
                                text = { Text(name) },
                                onClick = {
                                    viewModel.setTextColor(colorLong)
                                    showColorMenu = false
                                }
                            )
                        }
                    }
                }

                // Highlight Color Palette popup
                var showHighlightMenu by remember { mutableStateOf(false) }
                Box {
                    IconButton(
                        onClick = { showHighlightMenu = true },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(Icons.Default.BorderColor, contentDescription = "Highlight", tint = WordBluePrimary)
                    }
                    DropdownMenu(
                        expanded = showHighlightMenu,
                        onDismissRequest = { showHighlightMenu = false }
                    ) {
                        Text("Highlight Color", fontWeight = FontWeight.Bold, modifier = Modifier.padding(8.dp), fontSize = 12.sp)
                        val highColors = listOf(
                            0x00000000L to "No Color",
                            0xFFFFF176L to "Yellow",
                            0xFFA5D6A7L to "Bright Green",
                            0xFF80DEEAL to "Turquoise",
                            0xFFF48FB1L to "Pink",
                            0xFFFFCC80L to "Orange"
                        )
                        highColors.forEach { (colorLong, name) ->
                            DropdownMenuItem(
                                leadingIcon = {
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(if (colorLong == 0L) Color.Transparent else Color(colorLong))
                                            .border(1.dp, Color.Gray, CircleShape)
                                    )
                                },
                                text = { Text(name) },
                                onClick = {
                                    viewModel.setHighlightColor(colorLong)
                                    showHighlightMenu = false
                                }
                            )
                        }
                    }
                }

                // Clear Formatting
                IconButton(
                    onClick = { viewModel.clearFormatting() },
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(Icons.Default.FormatClear, contentDescription = "Clear Formatting", modifier = Modifier.size(18.dp))
                }
            }
        }

        RibbonVerticalDivider()

        // Section 3: Paragraph & Alignment
        RibbonGroup(title = "Paragraph") {
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                RibbonToggleIconButton(
                    icon = Icons.Default.FormatAlignLeft,
                    label = "Left",
                    isSelected = paragraph?.alignment == BlockAlignment.LEFT,
                    onClick = { viewModel.setAlignment(BlockAlignment.LEFT) }
                )
                RibbonToggleIconButton(
                    icon = Icons.Default.FormatAlignCenter,
                    label = "Center",
                    isSelected = paragraph?.alignment == BlockAlignment.CENTER,
                    onClick = { viewModel.setAlignment(BlockAlignment.CENTER) }
                )
                RibbonToggleIconButton(
                    icon = Icons.Default.FormatAlignRight,
                    label = "Right",
                    isSelected = paragraph?.alignment == BlockAlignment.RIGHT,
                    onClick = { viewModel.setAlignment(BlockAlignment.RIGHT) }
                )
                RibbonToggleIconButton(
                    icon = Icons.Default.FormatAlignJustify,
                    label = "Justify",
                    isSelected = paragraph?.alignment == BlockAlignment.JUSTIFY,
                    onClick = { viewModel.setAlignment(BlockAlignment.JUSTIFY) }
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Lists & Indent
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                RibbonToggleIconButton(
                    icon = Icons.Default.FormatListBulleted,
                    label = "Bullets",
                    isSelected = paragraph?.listType == ListType.BULLET,
                    onClick = { viewModel.setListType(ListType.BULLET) }
                )
                RibbonToggleIconButton(
                    icon = Icons.Default.FormatListNumbered,
                    label = "Numbers",
                    isSelected = paragraph?.listType == ListType.NUMBERED,
                    onClick = { viewModel.setListType(ListType.NUMBERED) }
                )
                RibbonToggleIconButton(
                    icon = Icons.Default.CheckCircleOutline,
                    label = "Tasks",
                    isSelected = paragraph?.listType == ListType.CHECKLIST,
                    onClick = { viewModel.setListType(ListType.CHECKLIST) }
                )
                RibbonIconButton(
                    icon = Icons.Default.FormatIndentDecrease,
                    label = "Outdent",
                    onClick = { viewModel.changeIndent(-1) }
                )
                RibbonIconButton(
                    icon = Icons.Default.FormatIndentIncrease,
                    label = "Indent",
                    onClick = { viewModel.changeIndent(1) }
                )
            }
        }

        RibbonVerticalDivider()

        // Section 4: Styles Quick Gallery
        RibbonGroup(title = "Styles") {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BlockStyle.values().forEach { style ->
                    val isSelected = paragraph?.style == style
                    Card(
                        modifier = Modifier
                            .height(58.dp)
                            .widthIn(min = 68.dp)
                            .clickable { viewModel.setStyle(style) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) WordBluePrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        border = if (isSelected) CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(WordBluePrimary)) else null
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(4.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "Aa",
                                fontWeight = if (style.isDefaultBold) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 16.sp,
                                color = if (isSelected) WordBluePrimary else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                style.displayName,
                                fontSize = 10.sp,
                                maxLines = 1,
                                color = if (isSelected) WordBluePrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InsertTabContent(viewModel: WordViewModel) {
    val scrollState = rememberScrollState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        RibbonGroup(title = "Tables") {
            RibbonButtonWithLabel(
                icon = Icons.Default.TableChart,
                label = "Table",
                onClick = { viewModel.showTableDialog(true) }
            )
        }

        RibbonVerticalDivider()

        RibbonGroup(title = "Pages & Breaks") {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                RibbonButtonWithLabel(
                    icon = Icons.Default.InsertPageBreak,
                    label = "Page Break",
                    onClick = { viewModel.insertPageBreak() }
                )
                RibbonButtonWithLabel(
                    icon = Icons.Default.HorizontalRule,
                    label = "Divider Line",
                    onClick = { viewModel.insertDivider(DividerStyle.SOLID) }
                )
            }
        }

        RibbonVerticalDivider()

        RibbonGroup(title = "Callouts & Notes") {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                RibbonButtonWithLabel(
                    icon = Icons.Default.Info,
                    label = "Note Box",
                    onClick = { viewModel.insertCallout(CalloutType.INFO, "Key point for the document.") }
                )
                RibbonButtonWithLabel(
                    icon = Icons.Default.Lightbulb,
                    label = "Tip Box",
                    onClick = { viewModel.insertCallout(CalloutType.TIP, "Helpful tip for readers.") }
                )
                RibbonButtonWithLabel(
                    icon = Icons.Default.Warning,
                    label = "Warning",
                    onClick = { viewModel.insertCallout(CalloutType.WARNING, "Important notice or restriction.") }
                )
            }
        }

        RibbonVerticalDivider()

        RibbonGroup(title = "Text & Symbols") {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                RibbonButtonWithLabel(
                    icon = Icons.Default.Abc,
                    label = "Symbols",
                    onClick = { viewModel.showSymbolDialog(true) }
                )
                RibbonButtonWithLabel(
                    icon = Icons.Default.Today,
                    label = "Date & Time",
                    onClick = { viewModel.insertCurrentDateTime() }
                )
                RibbonButtonWithLabel(
                    icon = Icons.Default.ViewAgenda,
                    label = "Header/Footer",
                    onClick = { viewModel.showHeaderFooterDialog(true) }
                )
            }
        }
    }
}

@Composable
private fun LayoutTabContent(
    doc: WordDocument?,
    viewModel: WordViewModel
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Page Orientation
        RibbonGroup(title = "Orientation") {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                FilterChip(
                    selected = doc?.orientation == PageOrientation.PORTRAIT,
                    onClick = { viewModel.setPageOrientation(PageOrientation.PORTRAIT) },
                    label = { Text("Portrait", fontSize = 11.sp) },
                    leadingIcon = { Icon(Icons.Default.StayCurrentPortrait, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                FilterChip(
                    selected = doc?.orientation == PageOrientation.LANDSCAPE,
                    onClick = { viewModel.setPageOrientation(PageOrientation.LANDSCAPE) },
                    label = { Text("Landscape", fontSize = 11.sp) },
                    leadingIcon = { Icon(Icons.Default.StayCurrentLandscape, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
            }
        }

        RibbonVerticalDivider()

        // Page Margins
        RibbonGroup(title = "Margins") {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                PageMargins.values().forEach { margin ->
                    FilterChip(
                        selected = doc?.margins == margin,
                        onClick = { viewModel.setPageMargins(margin) },
                        label = { Text(margin.displayName.take(7), fontSize = 11.sp) }
                    )
                }
            }
        }

        RibbonVerticalDivider()

        // Paper Size
        RibbonGroup(title = "Size") {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                DocPageSize.values().forEach { size ->
                    FilterChip(
                        selected = doc?.pageSize == size,
                        onClick = { viewModel.setPageSize(size) },
                        label = { Text(size.name, fontSize = 11.sp) }
                    )
                }
            }
        }

        RibbonVerticalDivider()

        // Paper Tone / Background
        RibbonGroup(title = "Page Color") {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                PageTone.values().forEach { tone ->
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(tone.hexColor))
                            .border(
                                width = if (doc?.pageTone == tone) 2.dp else 1.dp,
                                color = if (doc?.pageTone == tone) WordBluePrimary else Color.LightGray,
                                shape = CircleShape
                            )
                            .clickable { viewModel.setPageTone(tone) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ReviewTabContent(viewModel: WordViewModel) {
    val isSpeaking by viewModel.isSpeaking.collectAsState()

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        RibbonGroup(title = "Proofing & Stats") {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                RibbonButtonWithLabel(
                    icon = Icons.Default.Analytics,
                    label = "Word Count",
                    onClick = { viewModel.showStatsDialog(true) }
                )
                RibbonButtonWithLabel(
                    icon = Icons.Default.FindReplace,
                    label = "Find & Replace",
                    onClick = { viewModel.showFindReplace(true) }
                )
            }
        }

        RibbonVerticalDivider()

        RibbonGroup(title = "Speech") {
            RibbonButtonWithLabel(
                icon = if (isSpeaking) Icons.Default.Stop else Icons.Default.VolumeUp,
                label = if (isSpeaking) "Stop Audio" else "Read Aloud",
                onClick = { viewModel.toggleReadAloud() }
            )
        }

        RibbonVerticalDivider()

        RibbonGroup(title = "Information") {
            RibbonButtonWithLabel(
                icon = Icons.Default.Info,
                label = "Properties",
                onClick = { viewModel.showPropertiesDialog(true) }
            )
        }
    }
}

@Composable
private fun ViewTabContent(viewModel: WordViewModel) {
    val isPrintLayout by viewModel.isPrintLayout.collectAsState()
    val zoom by viewModel.zoomPercent.collectAsState()
    val showRuler by viewModel.showRuler.collectAsState()

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        RibbonGroup(title = "Document Views") {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                FilterChip(
                    selected = isPrintLayout,
                    onClick = { if (!isPrintLayout) viewModel.togglePrintLayout() },
                    label = { Text("Print Layout", fontSize = 11.sp) },
                    leadingIcon = { Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                FilterChip(
                    selected = !isPrintLayout,
                    onClick = { if (isPrintLayout) viewModel.togglePrintLayout() },
                    label = { Text("Mobile View", fontSize = 11.sp) },
                    leadingIcon = { Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
            }
        }

        RibbonVerticalDivider()

        RibbonGroup(title = "Show") {
            FilterChip(
                selected = showRuler,
                onClick = { viewModel.toggleRuler() },
                label = { Text("Ruler", fontSize = 11.sp) },
                leadingIcon = { Icon(Icons.Default.Straighten, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
        }

        RibbonVerticalDivider()

        RibbonGroup(title = "Zoom") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { viewModel.setZoom(zoom - 25) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.ZoomOut, contentDescription = "Zoom Out")
                }
                Text("$zoom%", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp))
                IconButton(
                    onClick = { viewModel.setZoom(zoom + 25) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.ZoomIn, contentDescription = "Zoom In")
                }
                TextButton(
                    onClick = { viewModel.setZoom(100) },
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    Text("100%", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun FileTabContent(viewModel: WordViewModel) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
    val doc = viewModel.activeDocument.collectAsState().value

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        RibbonGroup(title = "Export & Share") {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                RibbonButtonWithLabel(
                    icon = Icons.Default.Share,
                    label = "Share Text",
                    onClick = {
                        val text = viewModel.exportToPlainText()
                        val sendIntent = android.content.Intent().apply {
                            action = android.content.Intent.ACTION_SEND
                            putExtra(android.content.Intent.EXTRA_TEXT, text)
                            putExtra(android.content.Intent.EXTRA_TITLE, doc?.title ?: "Document")
                            type = "text/plain"
                        }
                        val shareIntent = android.content.Intent.createChooser(sendIntent, "Share Document")
                        context.startActivity(shareIntent)
                    }
                )

                RibbonButtonWithLabel(
                    icon = Icons.Default.Print,
                    label = "Print / PDF",
                    onClick = {
                        val printManager = context.getSystemService(android.content.Context.PRINT_SERVICE) as? android.print.PrintManager
                        if (printManager != null) {
                            val webView = android.webkit.WebView(context)
                            webView.loadDataWithBaseURL(null, viewModel.exportToHtml(), "text/html", "utf-8", null)
                            val printAdapter = webView.createPrintDocumentAdapter(doc?.title ?: "Document")
                            val printJobName = "${doc?.title ?: "Document"}.pdf"
                            printManager.print(printJobName, printAdapter, android.print.PrintAttributes.Builder().build())
                        }
                    }
                )

                RibbonButtonWithLabel(
                    icon = Icons.Default.ContentCopy,
                    label = "Copy MD",
                    onClick = {
                        val md = viewModel.exportToMarkdown()
                        clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(md))
                        android.widget.Toast.makeText(context, "Copied Markdown to clipboard!", android.widget.Toast.LENGTH_SHORT).show()
                    }
                )

                RibbonButtonWithLabel(
                    icon = Icons.Default.Code,
                    label = "Copy HTML",
                    onClick = {
                        val html = viewModel.exportToHtml()
                        clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(html))
                        android.widget.Toast.makeText(context, "Copied HTML to clipboard!", android.widget.Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }
}

@Composable
private fun RibbonGroup(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        content()
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            title,
            fontSize = 9.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun RibbonVerticalDivider() {
    Box(
        modifier = Modifier
            .height(50.dp)
            .width(1.dp)
            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    )
}

@Composable
private fun RibbonIconButton(
    icon: ImageVector,
    label: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.size(34.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(18.dp),
            tint = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        )
    }
}

@Composable
private fun RibbonToggleIconButton(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(if (isSelected) WordBluePrimary.copy(alpha = 0.2f) else Color.Transparent)
            .border(
                width = if (isSelected) 1.dp else 0.dp,
                color = if (isSelected) WordBluePrimary else Color.Transparent,
                shape = RoundedCornerShape(4.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(18.dp),
            tint = if (isSelected) WordBluePrimary else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun RibbonButtonWithLabel(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = WordBluePrimary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            label,
            fontSize = 10.sp,
            maxLines = 1,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
