package eu.neuhuber.hn.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import eu.neuhuber.hn.ui.theme.HnTheme

@Composable
fun HnApp() {
    HnTheme {
        HnNavGraph(rememberNavController())
    }
}

