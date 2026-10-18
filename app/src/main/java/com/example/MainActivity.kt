package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.data.ScrimsRepository
import com.example.ui.MainScreen
import com.example.ui.theme.ScrimsNepalTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ScrimsNepalTheme {
                val repository = remember { ScrimsRepository() }
                MainScreen(
                    repository = repository,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
