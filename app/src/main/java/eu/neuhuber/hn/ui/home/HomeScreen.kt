package eu.neuhuber.hn.ui.home

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import eu.neuhuber.hn.data.model.Id
import eu.neuhuber.hn.ui.newsList.BestNewsListViewModel
import eu.neuhuber.hn.ui.newsList.NewNewsListViewModel
import eu.neuhuber.hn.ui.newsList.NewsList
import eu.neuhuber.hn.ui.newsList.NewsListViewModel
import eu.neuhuber.hn.ui.newsList.TopNewsListViewModel
import kotlinx.coroutines.launch


enum class ListType(val label: String, val icon: ImageVector) {
    Top("Top", Icons.Filled.Home), New("New", Icons.Filled.Notifications), Best(
        "Best", Icons.Filled.Star
    );

    @Composable
    fun viewModel(): NewsListViewModel = when (this) {
        Top -> viewModel<TopNewsListViewModel>()
        New -> viewModel<NewNewsListViewModel>()
        Best -> viewModel<BestNewsListViewModel>()
    }
}

@Composable
fun HomeScreen(
    navigateToComments: (Id) -> Unit, viewModel: HomeViewModel = viewModel()
) {
    val selected: ListType by viewModel.selected
    val coroutineScope = rememberCoroutineScope()
    val scrollToTop by viewModel.scrollToTop

    Scaffold(bottomBar = {
        NavigationBar {
            ListType.values().forEach {
                NavigationBarItem(selected = selected == it, onClick = {
                    coroutineScope.launch {
                        viewModel.navBarSelect(it)
                    }
                }, label = { Text(it.label) }, icon = {
                    Icon(it.icon, it.label)
                })
            }
        }
    }) { paddingValues ->
        NewsList(navigateToComments, scrollToTop, selected, Modifier.padding(paddingValues))
    }
}
