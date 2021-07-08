package eu.neuhuber.hn.ui.home

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.TaskStackBuilder
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import eu.neuhuber.hn.R
import eu.neuhuber.hn.data.model.Id
import eu.neuhuber.hn.data.model.Item
import eu.neuhuber.hn.MainActivity
import eu.neuhuber.hn.ui.util.CardPlaceholder
import eu.neuhuber.hn.ui.util.createBitmap
import eu.neuhuber.hn.ui.util.toLocalString


enum class SelectedList(val label: String, val icon: ImageVector) {
    Top("Top", Icons.Filled.Home),
    New("New", Icons.Filled.Notifications),
    Best("Best", Icons.Filled.Star),
}

@Composable
fun HomeScreen(
    navigateToComments: (Id) -> Unit,
    viewModel: HomeViewModel = viewModel()
) {

    val selected: SelectedList by viewModel.selected

    Scaffold(
        bottomBar = {
            BottomNavigation() {
                SelectedList.values().forEach {
                    BottomNavigationItem(
                        selected = selected == it,
                        onClick = { viewModel.select(it)},
                        label = { Text(it.label) },
                        icon = {
                            Icon(it.icon, it.label)
                        })
                }
            }
        }

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
                    Column(
                        Modifier
                            .verticalScroll(rememberScrollState())
                            .fillMaxHeight()
                    ) {
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
    val typography = MaterialTheme.typography
    val colors = MaterialTheme.colors

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
                    .width(40.dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text((item.score ?: 0).toString(), style = typography.subtitle2)
            }
            Column(
                Modifier
                    .weight(1f)
                    .clickable(enabled = item.url != null) {
                        val icon = createBitmap(context, R.drawable.ic_baseline_question_answer_24)

                        openStory(context, item, colors, icon)
                    }
                    .padding(4.dp)
                    .fillMaxHeight()) {
                Text("${item.by} - ${item.time?.toLocalString()}", style = typography.overline)
                Text(item.title ?: "no title", style = typography.h6)
                item.url?.host?.let { Text(it, style = typography.caption) }
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


fun openStory(context: Context, item: Item, colors: Colors, icon: Bitmap) {
    item.url?.let { uri ->
        context.resources

        val deepLinkIntent = Intent(
            Intent.ACTION_VIEW,
            "eu.neuhuber.hn://comments/${item.id}".toUri(),
            context,
            MainActivity::class.java
        )

        val deepLinkPendingIntent: PendingIntent = TaskStackBuilder.create(context).run {
            addNextIntentWithParentStack(deepLinkIntent)
            getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
        } ?: throw Exception("Intent not found")

        val colorScheme = CustomTabColorSchemeParams.Builder()
            .setToolbarColor(colors.primaryVariant.toArgb())
            .setSecondaryToolbarColor(colors.secondary.toArgb())
            .build()

        val intent = CustomTabsIntent.Builder()
            .setDefaultColorSchemeParams(colorScheme)
            .setActionButton(icon, "Show Comments", deepLinkPendingIntent, false)
            .build();
        intent.launchUrl(context, uri)
        Log.i("openStory", uri.toString())
    }
}


@Preview
@Composable
fun StoryPreview() {
    Story(
        item = Item(
            id = 0,
            title = "Something very newsworthy has happend again",
            score = 446,
            by = "neuos",
            descendants = 384,
            url = Uri.parse("https://neuhuber.eu/news/1")
        )
    ) {}
}