package eu.neuhuber.hn.ui.newsList

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import eu.neuhuber.hn.data.model.Id
import eu.neuhuber.hn.ui.error.ErrorComponent
import eu.neuhuber.hn.ui.home.ListType
import eu.neuhuber.hn.ui.util.invoke
import kotlinx.coroutines.channels.Channel

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun NewsList(
    navigateToComments: (Id) -> Unit,
    scrollToTop: Channel<ListType>,
    listType: ListType,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    val viewModel = listType.viewModel()
    val storyIds = viewModel.storyIds
    val refreshing by viewModel.refresh.isRefreshing.collectAsState()
    val refreshState = rememberPullRefreshState(refreshing, { viewModel.refresh() })
    val listState = viewModel.listState

    LaunchedEffect(viewModel) {
        if (viewModel is BookmarksNewsListViewModel) {
            viewModel.refresh()
        }
    }
    LaunchedEffect(listType) {
        for (selectedList in scrollToTop) {
            if (selectedList == listType) {
                listState.animateScrollToItem(0)
            }
        }
    }

    Box(modifier = modifier.pullRefresh(refreshState)) {
        when {
            viewModel.errorMessage != null -> Column(
                Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxHeight()
            ) {
                ErrorComponent(message = viewModel.errorMessage.toString()) {
                    viewModel.refresh()
                }
            }

            storyIds == null -> {
                Column { (1..10).map { StoryPlaceholder() } }
            }

            else -> StoryList(
                storyIds, navigateToComments, listState, viewModel, snackbarHostState
            )
        }
        PullRefreshIndicator(
            refreshing = refreshing, state = refreshState, Modifier.align(Alignment.TopCenter)
        )
    }
}

