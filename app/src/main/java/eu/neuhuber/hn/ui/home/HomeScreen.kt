package eu.neuhuber.hn.ui.home

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import eu.neuhuber.hn.R
import eu.neuhuber.hn.data.model.Id
import eu.neuhuber.hn.data.model.Item
import eu.neuhuber.hn.ui.theme.typography
import eu.neuhuber.hn.ui.util.CardPlaceholder


@Composable
fun HomeScreen(
    navigateToComments: (Id) -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val storyIds = viewModel.storyIds.value

    val isRefreshing by viewModel.refresh.isRefreshing.collectAsState()
    SwipeRefresh(
        state = rememberSwipeRefreshState(isRefreshing),
        onRefresh = {
            viewModel.refresh()
        }) {

        when {
            viewModel.errorMessage != null ->
                Column(Modifier.verticalScroll(rememberScrollState()).fillMaxHeight()) {
                    Text(text = viewModel.errorMessage.toString())
                    Text(text = viewModel.errorMessage.toString())
                    Text(text = viewModel.errorMessage.toString())
                    Text(text = viewModel.errorMessage.toString())
                }
            storyIds == null -> {
                Column { (1..10).map { StoryPlaceholder() } }
            }
            else ->
                StoryList(list = storyIds, navigateToComments, viewModel)
        }

    }


}


@Composable
fun StoryList(
    list: List<Id>,
    navigateToComments: (Id) -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    LazyColumn(Modifier.fillMaxHeight()) {
        items(list) {
            val item = viewModel.loadStory(it)
            if (item == null) StoryPlaceholder()
            else Story(item, navigateToComments)
        }
    }
}


@Preview
@Composable
private fun StoryPlaceholder() = CardPlaceholder(height = 96.dp)


@Composable
fun Story(item: Item, navigateToComments: (Id) -> Unit) {
    val context = LocalContext.current
    val typography = typography()
    Card(
        Modifier
            .fillMaxWidth()
            .padding(4.dp), elevation = 8.dp
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            Column(
                Modifier
                    .width(64.dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text((item.score ?: 0).toString(), style = typography.h6)
            }
            Column(
                Modifier
                    .weight(1f)
                    .clickable(enabled = item.url != null) { openStory(context, item) }
                    .padding(4.dp)
                    .fillMaxHeight()) {
                Text(item.by ?: "no author", style = typography.overline)
                Text(item.title ?: "no title", style = typography.h6)
                Text(item.url?.host ?: "url", style = typography.caption)
            }
            Column(
                modifier = Modifier
                    .defaultMinSize(minHeight = 64.dp, minWidth = 40.dp)
                    .fillMaxHeight()
                    .clickable { navigateToComments(item.id) },
                Arrangement.Center,
                Alignment.CenterHorizontally
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_baseline_question_answer_24),
                    contentDescription = null
                )
                Text(text = (item.descendants ?: 0).toString(), style = typography.body1)
            }
        }
    }
}

fun openStory(context: Context, item: Item) {
    item.url?.let {
        val intent = CustomTabsIntent.Builder().build();
        intent.launchUrl(context, it);
        Log.i("hn", it.toString())
    }
}

@Preview
@Composable
fun StoryPreview() {
    Story(
        item = Item(
            id = 0,
            title = "Something very newsworthy has happend again",
            score = 462,
            by = "neuos",
            descendants = 384,
            url = Uri.parse("https://neuhuber.eu/news/1")
        )
    ) {}
}