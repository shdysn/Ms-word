package com.example.ui.editor

import android.content.Context
import android.content.Intent
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.WordViewModel
import com.example.ui.dialogs.*
import com.example.ui.theme.WordBluePrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordEditorScreen(
    viewModel: WordViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeDoc by viewModel.activeDocument.collectAsState()
    val canUndo by viewModel.canUndo.collectAsState()
    val canRedo by viewModel.canRedo.collectAsState()
    val isFindReplaceVisible by viewModel.isFindReplaceVisible.collectAsState()

    val isStatsVisible by viewModel.isStatsDialogVisible.collectAsState()
    val isSymbolVisible by viewModel.isSymbolDialogVisible.collectAsState()
    val isTableVisible by viewModel.isTableDialogVisible.collectAsState()
    val isPropsVisible by viewModel.isPropertiesDialogVisible.collectAsState()
    val isHeaderFooterVisible by viewModel.isHeaderFooterDialogVisible.collectAsState()

    val context = LocalContext.current
    var showOverflowMenu by remember { mutableStateOf(false) }
    var isEditingTitle by remember { mutableStateOf(false) }

    BackHandler {
        viewModel.closeDocument()
        onBack()
    }

    if (activeDoc == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = WordBluePrimary)
        }
        return
    }

    val doc = activeDoc!!

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (isEditingTitle) {
                        OutlinedTextField(
                            value = doc.title,
                            onValueChange = { viewModel.updateTitle(it) },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            trailingIcon = {
                                IconButton(onClick = { isEditingTitle = false }) {
                                    Icon(Icons.Default.Check, contentDescription = "Done")
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = doc.title,
                                maxLines = 1,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier
                                    .weight(1f, fill = false)
                                    .padding(end = 8.dp)
                            )
                            IconButton(
                                onClick = { isEditingTitle = true },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Rename Document",
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            // Saved indicator chip
                            Surface(
                                shape = MaterialTheme.shapes.extraSmall,
                                color = WordBluePrimary.copy(alpha = 0.12f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.CloudDone,
                                        contentDescription = "Saved",
                                        tint = WordBluePrimary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Saved", fontSize = 10.sp, color = WordBluePrimary, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.closeDocument()
                        onBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to Home")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.undo() },
                        enabled = canUndo
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Undo,
                            contentDescription = "Undo",
                            tint = if (canUndo) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        )
                    }
                    IconButton(
                        onClick = { viewModel.redo() },
                        enabled = canRedo
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Redo,
                            contentDescription = "Redo",
                            tint = if (canRedo) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        )
                    }
                    IconButton(
                        onClick = { viewModel.showFindReplace(!isFindReplaceVisible) }
                    ) {
                        Icon(Icons.Default.Search, contentDescription = "Find & Replace")
                    }

                    // Share button
                    IconButton(
                        onClick = {
                            val text = viewModel.exportToPlainText()
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, text)
                                putExtra(Intent.EXTRA_TITLE, doc.title)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share Document"))
                        }
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }

                    // Overflow Menu
                    Box {
                        IconButton(onClick = { showOverflowMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More options")
                        }
                        DropdownMenu(
                            expanded = showOverflowMenu,
                            onDismissRequest = { showOverflowMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Word Count & Statistics") },
                                leadingIcon = { Icon(Icons.Default.Analytics, contentDescription = null) },
                                onClick = {
                                    showOverflowMenu = false
                                    viewModel.showStatsDialog(true)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Print / Save as PDF") },
                                leadingIcon = { Icon(Icons.Default.Print, contentDescription = null) },
                                onClick = {
                                    showOverflowMenu = false
                                    val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
                                    if (printManager != null) {
                                        val webView = WebView(context)
                                        webView.loadDataWithBaseURL(null, viewModel.exportToHtml(), "text/html", "utf-8", null)
                                        val printAdapter = webView.createPrintDocumentAdapter(doc.title)
                                        printManager.print("${doc.title}.pdf", printAdapter, PrintAttributes.Builder().build())
                                    }
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Header & Footer") },
                                leadingIcon = { Icon(Icons.Default.ViewAgenda, contentDescription = null) },
                                onClick = {
                                    showOverflowMenu = false
                                    viewModel.showHeaderFooterDialog(true)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Document Properties") },
                                leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) },
                                onClick = {
                                    showOverflowMenu = false
                                    viewModel.showPropertiesDialog(true)
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Find & Replace Floating / Top bar
            AnimatedVisibility(visible = isFindReplaceVisible) {
                FindReplaceBar(viewModel = viewModel)
            }

            // Word Ribbon Bar
            RibbonBar(viewModel = viewModel)

            // Document Canvas (Paper sheet)
            DocumentCanvas(
                viewModel = viewModel,
                doc = doc,
                modifier = Modifier.weight(1f)
            )
        }
    }

    // Active Dialogs
    if (isStatsVisible) {
        DocumentStatsDialog(
            document = doc,
            onDismiss = { viewModel.showStatsDialog(false) }
        )
    }

    if (isSymbolVisible) {
        SymbolPickerDialog(
            onSelectSymbol = { sym -> viewModel.insertSymbol(sym) },
            onDismiss = { viewModel.showSymbolDialog(false) }
        )
    }

    if (isTableVisible) {
        InsertTableDialog(
            onInsert = { rows, cols, style -> viewModel.insertTable(rows, cols, style) },
            onDismiss = { viewModel.showTableDialog(false) }
        )
    }

    if (isPropsVisible) {
        DocumentPropertiesDialog(
            doc = doc,
            onDismiss = { viewModel.showPropertiesDialog(false) }
        )
    }

    if (isHeaderFooterVisible) {
        HeaderFooterDialog(
            currentHeader = doc.headerText,
            currentFooter = doc.footerText,
            showNumbers = doc.showPageNumbers,
            onSave = { h, f, n -> viewModel.setHeaderFooter(h, f, n) },
            onDismiss = { viewModel.showHeaderFooterDialog(false) }
        )
    }
}
