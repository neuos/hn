package eu.neuhuber.hn.ui.newsList

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import eu.neuhuber.hn.data.model.Id
import eu.neuhuber.hn.ui.home.ListType
import eu.neuhuber.hn.ui.home.StoryList
import eu.neuhuber.hn.ui.home.StoryPlaceholder
import kotlinx.coroutines.channels.Channel

@Composable
fun NewsList(
    paddingValues: PaddingValues,
    navigateToComments: (Id) -> Unit,
    scrollToTop: Channel<ListType>,
    listType: ListType
) {
    val viewModel = listType.viewModel()
    val storyIds = viewModel.storyIds.value
    val isRefreshing by viewModel.refresh.isRefreshing.collectAsState()
    val listState by viewModel.listState

    LaunchedEffect(listType) {
        for (selectedList in scrollToTop) {
            if (selectedList == listType) {
                listState.animateScrollToItem(0)
            }
        }
    }

    SwipeRefresh(
        state = rememberSwipeRefreshState(isRefreshing),
        modifier = Modifier.padding(paddingValues),
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

