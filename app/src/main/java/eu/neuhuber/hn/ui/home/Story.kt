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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.app.TaskStackBuilder
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import eu.neuhuber.hn.MainActivity
import eu.neuhuber.hn.R
import eu.neuhuber.hn.data.model.Id
import eu.neuhuber.hn.data.model.Item
import eu.neuhuber.hn.ui.theme.ColoredTheme
import eu.neuhuber.hn.ui.theme.HnPreview
import eu.neuhuber.hn.ui.theme.navbar
import eu.neuhuber.hn.ui.util.CardPlaceholder
import eu.neuhuber.hn.ui.util.createBitmap
import eu.neuhuber.hn.ui.util.toLocalString
import java.time.Instant


@Composable
fun StoryList(
    list: List<Id>,
    navigateToComments: (Id) -> Unit,
    viewModel: HomeViewModel = viewModel(),
    listState: LazyListState
) {
    LazyColumn(Modifier.fillMaxHeight(), listState) {
        items(list) {
            val item = viewModel.loadStory(it)
            if (item == null) StoryPlaceholder()
            else Story(item, navigateToComments)
        }
    }
}


@Composable
fun StoryPlaceholder() = CardPlaceholder(height = 96.dp)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Story(item: Item, navigateToComments: (Id) -> Unit) {
    val context = LocalContext.current
    val typography = MaterialTheme.typography
    val colors = MaterialTheme.colorScheme

    ElevatedCard(
        Modifier
            .fillMaxWidth()
            .padding(4.dp),
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
                Text((item.score ?: 0).toString(), style = typography.titleMedium)
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
                    .padding(4.dp)
                    .fillMaxHeight()) {
                Text("${item.by} - ${item.time?.toLocalString()}", style = typography.labelSmall)
                Text(item.title ?: "no title", style = typography.titleLarge)
                item.url?.host?.let { Text(it, style = typography.labelMedium) }
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
                Text(text = (item.descendants ?: 0).toString(), style = typography.bodyMedium)
            }
        }
    }
}


fun openStory(context: Context, item: Item, colors: ColorScheme, icon: Bitmap) {
    item.url?.let { uri ->
        context.resources

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

        val intent = CustomTabsIntent.Builder()
            .setDefaultColorSchemeParams(colorScheme)
            .setActionButton(icon, "Show Comments", showCommentsPendingIntent, true)
            .build()
        intent.launchUrl(context, uri)
        Log.i("openStory", uri.toString())
    }
}


@HnPreview
@Composable
fun StoryPreview() {
    ColoredTheme {
        Story(
            item = Item(
                id = 0,
                title = "Something very newsworthy has happened again",
                score = 446,
                by = "neuos",
                descendants = 384,
                url = Uri.parse("https://neuhuber.eu/news/1"),
                time = Instant.now(),
            ), navigateToComments = {}
        )
    }
}

@HnPreview
@Composable
fun StoryPlaceholderPreview() {
    ColoredTheme {
        StoryPlaceholder()
    }
}
