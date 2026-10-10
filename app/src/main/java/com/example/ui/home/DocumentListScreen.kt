package com.example.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.DocTemplates
import com.example.data.model.TemplateItem
import com.example.data.model.WordDocument
import com.example.data.model.calculateDocumentStats
import com.example.ui.WordViewModel
import com.example.ui.theme.WordBlueDark
import com.example.ui.theme.WordBluePrimary
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentListScreen(
    viewModel: WordViewModel,
    onOpenDocument: (WordDocument) -> Unit,
    modifier: Modifier = Modifier
) {
    val documents by viewModel.allDocuments.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var documentToRename by remember { mutableStateOf<WordDocument?>(null) }
    var renameText by remember { mutableStateOf("") }

    val filteredDocuments = remember(documents, searchQuery) {
        if (searchQuery.isBlank()) documents
        else documents.filter { it.title.contains(searchQuery, ignoreCase = true) }
    }

    val pinnedDocs = remember(filteredDocuments) { filteredDocuments.filter { it.isPinned } }
    val regularDocs = remember(filteredDocuments) { filteredDocuments.filter { !it.isPinned } }

    Scaffold(
        topBar = {
            Surface(
                color = WordBluePrimary,
                tonalElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_word_logo),
                                    contentDescription = "Word Logo",
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "DocuWord",
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                fontSize = 20.sp
                            )
                            Text(
                                "Microsoft Word Document Editor • ورڈ ڈاکومنٹ",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Search Documents Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search documents / تلاش کریں...", color = Color.Gray) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = WordBluePrimary) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            disabledContainerColor = Color.White,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        singleLine = true
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.createNewDocument("blank") },
                containerColor = WordBluePrimary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Document")
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Templates Section
            item {
                Column(modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 8.dp)) {
                    Text(
                        "Start New Document / نیا صفحہ",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "Select a blank page or professional pre-built template",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(DocTemplates.allTemplates) { template ->
                        TemplateCard(
                            template = template,
                            onClick = { viewModel.createNewDocument(template.templateKey) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            }

            // Pinned Documents Section (if any)
            if (pinnedDocs.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.PushPin,
                            contentDescription = null,
                            tint = WordBluePrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Pinned Documents (${pinnedDocs.size})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                items(pinnedDocs, key = { it.id }) { doc ->
                    DocumentRowItem(
                        doc = doc,
                        onClick = { onOpenDocument(doc) },
                        onTogglePin = { viewModel.togglePin(doc) },
                        onDuplicate = { viewModel.duplicateDocument(doc) },
                        onRename = {
                            documentToRename = doc
                            renameText = doc.title
                        },
                        onDelete = { viewModel.deleteDocument(doc.id) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                }
            }

            // Recent Documents Section
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.History,
                        contentDescription = null,
                        tint = WordBluePrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Recent Documents (${regularDocs.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (regularDocs.isEmpty() && pinnedDocs.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Description,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = Color.LightGray
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "No documents found",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.Gray
                            )
                            Text(
                                "Tap + to create your first document",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }
                }
            } else {
                items(regularDocs, key = { it.id }) { doc ->
                    DocumentRowItem(
                        doc = doc,
                        onClick = { onOpenDocument(doc) },
                        onTogglePin = { viewModel.togglePin(doc) },
                        onDuplicate = { viewModel.duplicateDocument(doc) },
                        onRename = {
                            documentToRename = doc
                            renameText = doc.title
                        },
                        onDelete = { viewModel.deleteDocument(doc.id) }
                    )
                }
            }
        }
    }

    // Rename Dialog
    if (documentToRename != null) {
        AlertDialog(
            onDismissRequest = { documentToRename = null },
            title = { Text("Rename Document", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    label = { Text("Document Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val target = documentToRename
                        if (target != null && renameText.isNotBlank()) {
                            viewModel.openDocument(target)
                            viewModel.updateTitle(renameText.trim())
                            viewModel.closeDocument()
                        }
                        documentToRename = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WordBluePrimary)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { documentToRename = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun TemplateCard(
    template: TemplateItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(130.dp)
            .height(145.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Miniature Paper Preview
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(75.dp),
                shape = RoundedCornerShape(4.dp),
                color = Color(0xFFF8F9FA),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFFE0E0E0))
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (template.templateKey == "blank") {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            tint = WordBluePrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.6f)
                                    .height(4.dp)
                                    .background(WordBluePrimary, RoundedCornerShape(2.dp))
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.9f)
                                    .height(3.dp)
                                    .background(Color.LightGray, RoundedCornerShape(2.dp))
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.8f)
                                    .height(3.dp)
                                    .background(Color.LightGray, RoundedCornerShape(2.dp))
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.5f)
                                    .height(3.dp)
                                    .background(Color.LightGray, RoundedCornerShape(2.dp))
                            )
                        }
                    }
                }
            }

            Column {
                Text(
                    template.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    template.urduTitle,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun DocumentRowItem(
    doc: WordDocument,
    onClick: () -> Unit,
    onTogglePin: () -> Unit,
    onDuplicate: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    val stats = remember(doc) { calculateDocumentStats(doc) }
    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy • hh:mm a", Locale.getDefault()) }
    var menuExpanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Word Icon badge
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = WordBluePrimary.copy(alpha = 0.12f),
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        "W",
                        fontWeight = FontWeight.Black,
                        color = WordBluePrimary,
                        fontSize = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Title & Metadata
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        doc.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (doc.isPinned) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            Icons.Default.PushPin,
                            contentDescription = "Pinned",
                            tint = WordBluePrimary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    "${stats.words} words • ${doc.pageSize.name} • Modified ${dateFormat.format(Date(doc.updatedAt))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Menu button
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "Document Actions",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(if (doc.isPinned) "Unpin" else "Pin to Top") },
                        leadingIcon = { Icon(Icons.Default.PushPin, contentDescription = null) },
                        onClick = {
                            menuExpanded = false
                            onTogglePin()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Rename") },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                        onClick = {
                            menuExpanded = false
                            onRename()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Make a Copy") },
                        leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
                        onClick = {
                            menuExpanded = false
                            onDuplicate()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                        leadingIcon = { Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                        onClick = {
                            menuExpanded = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}
