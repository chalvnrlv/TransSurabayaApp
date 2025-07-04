package com.example.transsurabayaapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.transsurabayaapp.ui.navigation.AppNavigation
import com.example.transsurabayaapp.viewmodel.*

class MainActivity : ComponentActivity() {
    private val viewModel: TransSurabayaViewModel by viewModels {
        TransSurabayaViewModelFactory((application as TransSurabayaApp).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(viewModel)
                }
            }
        }
    }
}