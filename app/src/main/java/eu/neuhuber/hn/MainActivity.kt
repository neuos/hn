package eu.neuhuber.hn

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import eu.neuhuber.hn.ui.theme.HnTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HnTheme {
                HnNavGraph(navController = rememberNavController())
            }
        }
    }
}
