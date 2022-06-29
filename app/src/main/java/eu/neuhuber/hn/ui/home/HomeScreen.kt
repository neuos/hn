package eu.neuhuber.hn.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import eu.neuhuber.hn.data.model.Id
import kotlinx.coroutines.launch


enum class SelectedList(val label: String, val icon: ImageVector) {
    Top("Top", Icons.Filled.Home), New("New", Icons.Filled.Notifications), Best(
        "Best",
        Icons.Filled.Star
    ),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navigateToComments: (Id) -> Unit, viewModel: HomeViewModel = viewModel()
) {
    val selected: SelectedList by viewModel.selected
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    Scaffold(bottomBar = {
        NavigationBar {
            SelectedList.values().forEach {
                NavigationBarItem(selected = selected == it, onClick = {
                    val changed = viewModel.changeView(it)
                    if (!changed) {
                        coroutineScope.launch {
                            listState.animateScrollToItem(0)
                        }
                    }
                }, label = { Text(it.label) }, icon = {
                    Icon(it.icon, it.label)
                })
            }
        }
    }) {
        val storyIds = viewModel.storyIds.value
        val isRefreshing by viewModel.refresh.isRefreshing.collectAsState()
        SwipeRefresh(
            state = rememberSwipeRefreshState(isRefreshing),
            modifier = Modifier.padding(it),
            onRefresh = {
                viewModel.refresh()
            }) {
            when {
                viewModel.errorMessage != null -> Column(
                    Modifier
                        .verticalScroll(rememberScrollState())
                        .fillMaxHeight()
                ) {
                    Text(text = viewModel.errorMessage.toString())
                }
                storyIds == null -> {
                    Column { (1..10).map { StoryPlaceholder() } }
                }
                else -> StoryList(list = storyIds, navigateToComments, viewModel, listState)
            }

        }
    }
}

