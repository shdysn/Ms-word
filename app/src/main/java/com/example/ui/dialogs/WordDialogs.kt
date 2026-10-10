package com.example.ui.dialogs

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.WordViewModel
import com.example.ui.theme.WordBluePrimary
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DocumentStatsDialog(
    document: WordDocument,
    onDismiss: () -> Unit
) {
    val stats = remember(document) { calculateDocumentStats(document) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Analytics,
                    contentDescription = null,
                    tint = WordBluePrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Document Statistics", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatRow("Words", stats.words.toString())
                StatRow("Characters (with spaces)", stats.charactersWithSpaces.toString())
                StatRow("Characters (no spaces)", stats.charactersWithoutSpaces.toString())
                StatRow("Paragraphs", stats.paragraphs.toString())
                StatRow("Lines (approx.)", stats.lines.toString())
                Divider(color = MaterialTheme.colorScheme.outlineVariant)
                StatRow("Estimated Pages", "${stats.estimatedPages} pages")
                StatRow("Reading Time", "~${stats.readingTimeMinutes} min")
                StatRow("Speaking Time", "~${stats.speakingTimeMinutes} min")
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = WordBluePrimary)
            ) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun FindReplaceBar(
    viewModel: WordViewModel,
    modifier: Modifier = Modifier
) {
    val findQuery by viewModel.findQuery.collectAsState()
    val replaceQuery by viewModel.replaceQuery.collectAsState()
    val matchCount by viewModel.searchMatchCount.collectAsState()

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 6.dp,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = findQuery,
                    onValueChange = { viewModel.updateFindQuery(it) },
                    label = { Text("Find text...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = WordBluePrimary) },
                    trailingIcon = {
                        if (findQuery.isNotEmpty()) {
                            Badge(containerColor = if (matchCount > 0) WordBluePrimary else MaterialTheme.colorScheme.error) {
                                Text("$matchCount")
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )

                IconButton(
                    onClick = { viewModel.showFindReplace(false) }
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close Find")
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = replaceQuery,
                    onValueChange = { viewModel.updateReplaceQuery(it) },
                    label = { Text("Replace with...") },
                    leadingIcon = { Icon(Icons.Default.FindReplace, contentDescription = null) },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )

                Button(
                    onClick = { viewModel.replaceAllOccurrences() },
                    enabled = findQuery.isNotBlank() && matchCount > 0,
                    colors = ButtonDefaults.buttonColors(containerColor = WordBluePrimary)
                ) {
                    Text("Replace All")
                }
            }
        }
    }
}

@Composable
fun SymbolPickerDialog(
    onSelectSymbol: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val commonSymbols = remember {
        listOf(
            "©", "®", "™", "§", "¶", "•", "–", "—",
            "€", "$", "£", "¥", "₹", "₨", "¢", "%",
            "π", "∑", "√", "∞", "≠", "±", "≤", "≥",
            "×", "÷", "≈", "‰", "°", "µ", "Ω", "∆",
            "→", "←", "↑", "↓", "↔", "⇒", "★", "✓",
            "½", "¼", "¾", "¹", "²", "³", "α", "β"
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Insert Symbol", fontWeight = FontWeight.Bold) },
        text = {
            LazyVerticalGrid(
                columns = GridCells.Fixed(6),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 280.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(commonSymbols) { sym ->
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable {
                                onSelectSymbol(sym)
                                onDismiss()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(sym, fontSize = 20.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun InsertTableDialog(
    onInsert: (rows: Int, cols: Int, style: TableStyle) -> Unit,
    onDismiss: () -> Unit
) {
    var rows by remember { mutableStateOf(3) }
    var cols by remember { mutableStateOf(3) }
    var selectedStyle by remember { mutableStateOf(TableStyle.BLUE_OFFICE) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Insert Table", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Rows Picker
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Number of Rows:")
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { if (rows > 1) rows-- }) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrease")
                        }
                        Text("$rows", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
                        IconButton(onClick = { if (rows < 8) rows++ }) {
                            Icon(Icons.Default.Add, contentDescription = "Increase")
                        }
                    }
                }

                // Columns Picker
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Number of Columns:")
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { if (cols > 1) cols-- }) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrease")
                        }
                        Text("$cols", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
                        IconButton(onClick = { if (cols < 6) cols++ }) {
                            Icon(Icons.Default.Add, contentDescription = "Increase")
                        }
                    }
                }

                // Table Style selector
                Text("Table Style:", fontWeight = FontWeight.Medium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TableStyle.values().forEach { style ->
                        val isSelected = style == selectedStyle
                        OutlinedButton(
                            onClick = { selectedStyle = style },
                            modifier = Modifier.weight(1f),
                            colors = if (isSelected) {
                                ButtonDefaults.outlinedButtonColors(
                                    containerColor = WordBluePrimary.copy(alpha = 0.15f)
                                )
                            } else ButtonDefaults.outlinedButtonColors()
                        ) {
                            Text(
                                style.name.replace("_", " ").take(6),
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onInsert(rows, cols, selectedStyle)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = WordBluePrimary)
            ) {
                Text("Insert Table")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun DocumentPropertiesDialog(
    doc: WordDocument,
    onDismiss: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy 'at' hh:mm a", Locale.getDefault()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, contentDescription = null, tint = WordBluePrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Document Properties", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatRow("Title", doc.title)
                StatRow("Author", doc.author)
                StatRow("Page Size", doc.pageSize.displayName)
                StatRow("Orientation", doc.orientation.name)
                StatRow("Margins", doc.margins.displayName)
                StatRow("Paper Tone", doc.pageTone.displayName)
                StatRow("Total Elements", "${doc.blocks.size} blocks")
                Divider(color = MaterialTheme.colorScheme.outlineVariant)
                StatRow("Created", dateFormat.format(Date(doc.createdAt)))
                StatRow("Last Modified", dateFormat.format(Date(doc.updatedAt)))
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = WordBluePrimary)
            ) {
                Text("Done")
            }
        }
    )
}

@Composable
fun HeaderFooterDialog(
    currentHeader: String,
    currentFooter: String,
    showNumbers: Boolean,
    onSave: (header: String, footer: String, showNumbers: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var header by remember { mutableStateOf(currentHeader) }
    var footer by remember { mutableStateOf(currentFooter) }
    var numbers by remember { mutableStateOf(showNumbers) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Header & Footer", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = header,
                    onValueChange = { header = it },
                    label = { Text("Header Text (Top of page)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = footer,
                    onValueChange = { footer = it },
                    label = { Text("Footer Text (Bottom of page)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = numbers,
                        onCheckedChange = { numbers = it },
                        colors = CheckboxDefaults.colors(checkedColor = WordBluePrimary)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Show Page Number (e.g. Page 1)")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(header, footer, numbers)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = WordBluePrimary)
            ) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
