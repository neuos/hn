package eu.neuhuber.hn.ui

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navArgument
import androidx.navigation.compose.rememberNavController
import eu.neuhuber.hn.AppContainer
import eu.neuhuber.hn.data.model.Id
import eu.neuhuber.hn.ui.MainDestinations.STORY_ID_ARGUMENT
import eu.neuhuber.hn.ui.comments.CommentsScreen
import eu.neuhuber.hn.ui.home.HomeScreen


object MainDestinations {
    const val HOME_ROUTE = "home"
    const val COMMENTS_ROUTE = "comments"
    const val STORY_ID_ARGUMENT = "storyId"
}

@Composable
fun HnNavGraph(
    appContainer: AppContainer,
    navController: NavHostController = rememberNavController(),
    startDestination: String = MainDestinations.HOME_ROUTE
) {
    val actions = remember(navController) { MainActions(navController) }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(MainDestinations.HOME_ROUTE) {
            MaterialTheme() {
                HomeScreen(
                    navigateToComments = actions.navigateToComments,
                )
            }
        }
        composable(
            "${MainDestinations.COMMENTS_ROUTE}/{$STORY_ID_ARGUMENT}",
            arguments = listOf(navArgument(STORY_ID_ARGUMENT) { type = NavType.LongType })
        ) { backStackEntry ->
            CommentsScreen(
                newsId = backStackEntry.arguments?.getLong(STORY_ID_ARGUMENT),
                onBack = actions.upPress,
            )
        }
    }
}

class MainActions(navController: NavHostController) {
    val navigateToComments: (Id) -> Unit = { newsId ->
        navController.navigate("${MainDestinations.COMMENTS_ROUTE}/$newsId")
    }
    val upPress: () -> Unit = {
        navController.navigateUp()
    }
}
