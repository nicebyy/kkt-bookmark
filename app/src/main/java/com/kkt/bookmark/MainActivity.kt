package com.kkt.bookmark

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.kkt.bookmark.ui.navigation.NavGraph
import com.kkt.bookmark.ui.theme.KKTBookmarkTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KKTBookmarkTheme {
                NavGraph()
            }
        }
    }
}
