package eu.neuhuber.hn

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import co.touchlab.kermit.Logger
import eu.neuhuber.hn.MainDestinations.STORY_ID_ARGUMENT
import eu.neuhuber.hn.data.model.Id
import eu.neuhuber.hn.ui.comments.CommentsScreen
import eu.neuhuber.hn.ui.home.HomeScreen


object MainDestinations {
    const val HOME_ROUTE = "home"
    const val COMMENTS_ROUTE = "comments"
    const val STORY_ID_ARGUMENT = "storyId"
}

class MainActions(navController: NavHostController) {
    val navigateToComments: (Id) -> Unit = { newsId ->
        navController.navigate("${MainDestinations.COMMENTS_ROUTE}/$newsId") { }
    }
}

@Composable
fun HnNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = MainDestinations.HOME_ROUTE,
) {
    val actions = remember(navController) { MainActions(navController) }
    val packageName = LocalContext.current.packageName
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier.fillMaxSize()
    ) {
        composable(
            MainDestinations.HOME_ROUTE,
            enterTransition = { slideInHorizontally { -it } },
            exitTransition = { slideOutHorizontally { -it } },
        ) {
            HomeScreen(
                navigateToComments = actions.navigateToComments,
            )
        }
        composable(
            "${MainDestinations.COMMENTS_ROUTE}/{$STORY_ID_ARGUMENT}",
            enterTransition = { slideInHorizontally { it } },
            exitTransition = { slideOutHorizontally { it } },
            arguments = listOf(navArgument(STORY_ID_ARGUMENT) { type = NavType.LongType }),
            deepLinks = listOf(navDeepLink {
                uriPattern =
                    "$packageName://${MainDestinations.COMMENTS_ROUTE}/{$STORY_ID_ARGUMENT}"
            },
                // TODO: make app return to source of link on back press instead of return to home screen
                navDeepLink {
                    uriPattern = "https://news.ycombinator.com/item?id={$STORY_ID_ARGUMENT}"
                    this.action
                })
        ) { backStackEntry ->
            Logger.withTag("HnNavGraph").d(
                "opening comments route for ${
                    backStackEntry.arguments?.getString(
                        STORY_ID_ARGUMENT
                    )
                }"
            )
            CommentsScreen(
                newsId = backStackEntry.arguments?.getLong(STORY_ID_ARGUMENT),
            )
        }
    }
}


