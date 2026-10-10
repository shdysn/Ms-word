package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.WordViewModel
import com.example.ui.editor.WordEditorScreen
import com.example.ui.home.DocumentListScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: WordViewModel = viewModel()
                val activeDoc by viewModel.activeDocument.collectAsState()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (activeDoc != null) {
                        WordEditorScreen(
                            viewModel = viewModel,
                            onBack = { viewModel.closeDocument() }
                        )
                    } else {
                        DocumentListScreen(
                            viewModel = viewModel,
                            onOpenDocument = { doc -> viewModel.openDocument(doc) }
                        )
                    }
                }
            }
        }
    }
}
