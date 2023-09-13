package eu.neuhuber.hn.ui.newsList

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkRemove
import androidx.compose.material.icons.outlined.BookmarkAdd
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.DismissDirection
import androidx.compose.material3.DismissState
import androidx.compose.material3.DismissValue
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismiss
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDismissState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.app.TaskStackBuilder
import androidx.core.net.toUri
import co.touchlab.kermit.Logger
import eu.neuhuber.hn.MainActivity
import eu.neuhuber.hn.R
import eu.neuhuber.hn.data.model.Id
import eu.neuhuber.hn.data.model.Item
import eu.neuhuber.hn.data.repo.FakeNewsRepository
import eu.neuhuber.hn.data.repo.MemoryBookmarkRepository
import eu.neuhuber.hn.ui.theme.HnPreviews
import eu.neuhuber.hn.ui.theme.HnTheme
import eu.neuhuber.hn.ui.theme.navbar
import eu.neuhuber.hn.ui.util.AutoSizeText
import eu.neuhuber.hn.ui.util.CardPlaceholder
import eu.neuhuber.hn.ui.util.createBitmap
import eu.neuhuber.hn.ui.util.toLocalString
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.launch
import java.time.Instant


@Composable
fun StoryList(
    list: ImmutableList<Id>,
    navigateToComments: (Id) -> Unit,
    listState: LazyListState,
    viewModel: NewsListViewModel,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier.fillMaxHeight(), listState) {
        items(list, key = { it }) {
            val item = viewModel.loadStory(it)
            if (item == null) StoryPlaceholder()
            else Story(item, navigateToComments, viewModel, snackbarHostState = snackbarHostState)
        }
    }
}


@Composable
fun StoryPlaceholder() = CardPlaceholder(height = 96.dp)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Story(
    item: Item,
    navigateToComments: (Id) -> Unit,
    viewModel: NewsListViewModel,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = SnackbarHostState(),
) {
    val scope = rememberCoroutineScope()
    val dismissState = rememberDismissState(
        confirmValueChange = {
            if (it != DismissValue.Default) {
                scope.launch {
                    viewModel.toggleBookmark(item).onSuccess { isBookmarked ->
                        snackbarHostState.currentSnackbarData?.dismiss()
                        snackbarHostState.showSnackbar("Bookmark ${if (isBookmarked) "added" else "removed"}")
                    }
                }
            }
            false// don't dismiss
        },
    )

    val isBookmarked = viewModel.bookmarkedIds.contains(item.id)

    SwipeToDismiss(state = dismissState,
        background = { SwipeBackground(dismissState, isBookmarked = isBookmarked, modifier) },
        dismissContent = {
            StoryCard(item, navigateToComments, modifier)
        })
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun SwipeBackground(
    dismissState: DismissState,
    isBookmarked: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = if(dismissState.currentValue < dismissState.targetValue) CardDefaults.cardColors(MaterialTheme.colorScheme.primaryContainer) else CardDefaults.cardColors()
    Card(
        modifier
            .fillMaxSize()
            .padding(4.dp),
        colors = colors,
    ) {
        Row(Modifier.fillMaxHeight(), verticalAlignment = Alignment.CenterVertically) {
            if (dismissState.dismissDirection == DismissDirection.StartToEnd) {
                BookmarkIcon(isBookmarked)
            }
            Spacer(Modifier.weight(1f))
            if (dismissState.dismissDirection == DismissDirection.EndToStart) {
                BookmarkIcon(isBookmarked)
            }
        }
    }
}

@Composable
private fun BookmarkIcon(isBookmarked: Boolean = false) {
    Icon(
        if (isBookmarked) Icons.Filled.BookmarkRemove else Icons.Outlined.BookmarkAdd,
        contentDescription = null,
        modifier = Modifier
            .padding(16.dp)
            .size(32.dp)
    )
}

@Composable
private fun StoryCard(
    item: Item,
    navigateToComments: (Id) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val colors = MaterialTheme.colorScheme

    val typography = MaterialTheme.typography

    ElevatedCard(
        modifier
            .fillMaxWidth()
            .padding(4.dp),
    ) {
        Row(
            Modifier.height(IntrinsicSize.Min), verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                Modifier.width(40.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AutoSizeText((item.score ?: 0).toString(), style = typography.titleMedium)
            }
            Column(
                Modifier
                    .weight(1f)
                    .clickable {
                        if (item.url == null) navigateToComments(item.id)
                        else {
                            val icon =
                                createBitmap(context, R.drawable.ic_baseline_question_answer_24)
                            openStory(context, item, colors, icon)
                        }
                    }
                    .padding(4.dp)) {
                Text(
                    "${item.by} - ${item.time?.toLocalString()}", style = typography.labelSmall
                )
                Text(item.title ?: "no title", style = typography.titleLarge)
                item.url?.host?.let { Text(it, style = typography.labelMedium) }
            }
            Column(
                modifier = Modifier
                    .width(40.dp)
                    .fillMaxHeight()
                    .clickable { navigateToComments(item.id) },
                Arrangement.Center,
                Alignment.CenterHorizontally
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_baseline_question_answer_24),
                    contentDescription = null
                )
                AutoSizeText(
                    text = (item.descendants ?: 0).toString(), style = typography.bodyMedium
                )
            }
        }
    }
}


fun openStory(context: Context, item: Item, colors: ColorScheme, icon: Bitmap) {
    item.url?.let { uri ->
        val showCommentsIntent = Intent(
            Intent.ACTION_VIEW,
            "eu.neuhuber.hn://comments/${item.id}".toUri(),
            context,
            MainActivity::class.java
        )

        val showCommentsPendingIntent: PendingIntent = TaskStackBuilder.create(context).run {
            addNextIntentWithParentStack(showCommentsIntent)
            getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        } ?: throw Exception("Intent not found")

        val colorScheme =
            CustomTabColorSchemeParams.Builder().setToolbarColor(colors.background.toArgb())
                .setNavigationBarColor(colors.navbar.toArgb()).build()

        val intent = CustomTabsIntent.Builder().setDefaultColorSchemeParams(colorScheme)
            .setActionButton(icon, "Show Comments", showCommentsPendingIntent, true).build()
        intent.launchUrl(context, uri)
        Logger.withTag("openStory").i { uri.toString() }
    }
}


@HnPreviews
@Composable
fun StoryPreview() = HnTheme {
    Story(
        item = Item(
            id = 0,
            title = "Something very newsworthy has happened again",
            score = 446,
            by = "neuos",
            descendants = 384,
            url = Uri.parse("https://neuhuber.eu/news/1"),
            time = Instant.now(),
        ),
        navigateToComments = {},
        viewModel = TopNewsListViewModel(FakeNewsRepository, MemoryBookmarkRepository()),
    )
}

@HnPreviews
@Composable
fun StoryPreviewLargeNumbers() = HnTheme {
    Story(
        item = Item(
            id = 0,
            title = "Something very newsworthy has happened again",
            score = 123456,
            by = "neuos",
            descendants = 7890123,
            url = Uri.parse("https://neuhuber.eu/news/1"),
            time = Instant.now(),
        ),
        navigateToComments = {},
        viewModel = TopNewsListViewModel(FakeNewsRepository, MemoryBookmarkRepository()),
    )
}

@HnPreviews
@Composable
fun StoryPreviewSmallNumbers() = HnTheme {
    Story(
        item = Item(
            id = 0,
            title = "Something very newsworthy has happened again",
            score = 1,
            by = "neuos",
            descendants = 2,
            url = Uri.parse("https://neuhuber.eu/news/1"),
            time = Instant.now(),
        ),
        navigateToComments = {},
        viewModel = TopNewsListViewModel(FakeNewsRepository, MemoryBookmarkRepository()),
    )
}

@HnPreviews
@Composable
fun StoryPlaceholderPreview() = HnTheme {
    StoryPlaceholder()
}