package eu.neuhuber.hn.ui

import androidx.compose.material.Scaffold
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import eu.neuhuber.hn.AppContainer
import eu.neuhuber.hn.ui.theme.HnTheme

@Composable
fun HnApp(appContainer: AppContainer) {
    HnTheme {
        val navController = rememberNavController()
        val scaffoldState = rememberScaffoldState()
        Scaffold(scaffoldState = scaffoldState) {
            HnNavGraph(
                appContainer = appContainer,
                navController = navController
            )
        }
    }
}

