package eu.neuhuber.hn.ui.home

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import eu.neuhuber.hn.data.model.Id
import eu.neuhuber.hn.ui.newsList.BestNewsListViewModel
import eu.neuhuber.hn.ui.newsList.BookmarksNewsListViewModel
import eu.neuhuber.hn.ui.newsList.NewNewsListViewModel
import eu.neuhuber.hn.ui.newsList.NewsList
import eu.neuhuber.hn.ui.newsList.NewsListViewModel
import eu.neuhuber.hn.ui.newsList.TopNewsListViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel


enum class ListType(val label: String, val icon: ImageVector) {
    Top("Top", Icons.Filled.Home), New("New", Icons.Filled.Notifications), Best(
        "Best", Icons.Filled.Star
    ),
    Bookmarks("Bookmarks", Icons.Filled.CollectionsBookmark);

    @Composable
    fun viewModel(): NewsListViewModel {
        return when (this) {
            Top -> koinViewModel<TopNewsListViewModel>()
            New -> koinViewModel<NewNewsListViewModel>()
            Best -> koinViewModel<BestNewsListViewModel>()
            Bookmarks -> koinViewModel<BookmarksNewsListViewModel>()
        }
    }
}

@Composable
fun HomeScreen(
    navigateToComments: (Id) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel()
) {
    val selected: ListType by viewModel.selected
    val coroutineScope = rememberCoroutineScope()
    val scrollToTop by viewModel.scrollToTop
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
        NavigationBar {
            ListType.entries.forEach {
                NavigationBarItem(selected = selected == it, onClick = {
                    coroutineScope.launch {
                        viewModel.navBarSelect(it)
                    }
                }, label = { Text(it.label) }, icon = {
                    Icon(it.icon, it.label)
                })
            }
        }
    }, modifier = modifier) { paddingValues ->
        NewsList(navigateToComments, scrollToTop, selected, snackbarHostState, Modifier.padding(paddingValues))
    }
}
