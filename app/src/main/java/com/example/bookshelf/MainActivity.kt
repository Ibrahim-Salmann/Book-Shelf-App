package com.example.bookshelf

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.bookshelf.screens.BookShelfScreen
import com.example.bookshelf.ui.theme.BookShelfTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BookShelfTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    BookShelfScreen()
                }
            }
        }
    }
}
