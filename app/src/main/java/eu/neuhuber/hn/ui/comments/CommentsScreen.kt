package eu.neuhuber.hn.ui.comments

import android.net.Uri
import android.text.method.LinkMovementMethod
import android.widget.TextView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.HtmlCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import eu.neuhuber.hn.R
import eu.neuhuber.hn.data.model.Id
import eu.neuhuber.hn.data.model.Item
import eu.neuhuber.hn.ui.error.ErrorComponent
import eu.neuhuber.hn.ui.newsList.openStory
import eu.neuhuber.hn.ui.theme.HnPreviews
import eu.neuhuber.hn.ui.theme.HnTheme
import eu.neuhuber.hn.ui.util.CardPlaceholder
import eu.neuhuber.hn.ui.util.Favicon
import eu.neuhuber.hn.ui.util.createBitmap
import eu.neuhuber.hn.ui.util.toLocalString
import java.time.Instant


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun CommentsScreen(
    newsId: Id?, viewModel: CommentsViewModel = viewModel()
) {
    if (newsId == null) Text(text = "Invalid News Item")
    else {
        val refreshing by viewModel.refresh.isRefreshing.collectAsState()
        val refreshState = rememberPullRefreshState(refreshing, { viewModel.refresh(newsId) })
        Box(modifier = Modifier.pullRefresh(refreshState)) {
            val loadComment: LazyCommentTree? = viewModel.loadComment(newsId)
            when {
                viewModel.errorMessage != null -> ErrorComponent(message = viewModel.errorMessage.toString(),
                    retry = { viewModel.refresh(newsId) })

                loadComment == null -> CommentPlaceHolder()
                else -> CommentsColumn(loadComment)
            }
            PullRefreshIndicator(refreshing, refreshState, Modifier.align(Alignment.TopCenter))
        }
    }
}

@Composable
private fun CommentsColumn(loadComment: LazyCommentTree) {
    LazyColumn(Modifier.fillMaxHeight()) {
        item {
            CommentScreenHeader(loadComment.item)
            loadComment.item?.text?.let {
                CommentCard(
                    text = it, author = loadComment.item?.by, time = loadComment.item?.time
                )
            }
        }
        items(loadComment.children) {
            CommentNode(id = it.id)
        }
    }
}

@Composable
fun CommentNode(id: Id, depth: Int = 0, viewModel: CommentsViewModel = viewModel()) {
    val expanded = remember { mutableStateOf(depth < 2) }

    val item = viewModel.loadComment(id)?.item

    if (item == null) CommentPlaceHolder()
    else {
        CommentCard(
            text = item.text,
            childCount = item.kids?.size ?: 0,
            depth = depth,
            isExpanded = expanded.value,
            author = item.by,
            time = item.time
        ) {
            expanded.value = !expanded.value
        }

        AnimatedVisibility(visible = expanded.value) {
            Column {
                item.kids?.forEach {
                    CommentNode(it, depth + 1)
                }
            }
        }

    }
}

@Composable
fun CommentScreenHeader(item: Item?) {
    val typography = MaterialTheme.typography
    if (item == null) CommentPlaceHolder()
    else {
        ElevatedCard(Modifier.fillMaxWidth()) {

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
                    Text((item.score ?: 0).toString(), style = typography.titleMedium)
                }

                Column(
                    Modifier
                        .weight(1f)
                        .padding(8.dp)
                        .fillMaxHeight()
                ) {
                    Text(item.by ?: "no author", style = typography.labelSmall)
                    Text(item.title ?: "no title", style = typography.titleMedium)
                    item.url?.host?.let {
                        Text(it, style = typography.bodySmall)
                    }
                }
                if (item.url != null) {

                    val context = LocalContext.current
                    val colors = MaterialTheme.colorScheme

                    Column(modifier = Modifier
                        .width(40.dp)
                        .defaultMinSize(minHeight = 64.dp)
                        .fillMaxHeight()
                        .clickable {
                            val icon =
                                createBitmap(context, R.drawable.ic_baseline_question_answer_24)
                            openStory(context, item, colors, icon)
                        }
                        .padding(8.dp), Arrangement.Center, Alignment.CenterHorizontally) {
                        val contentDescription = "open in browser"
                        Favicon(
                            uri = item.url,
                            contentDescription,
                            painterResource(id = R.drawable.ic_baseline_open_in_browser_24)
                        )
                    }
                }
            }
        }
    }
}


@HnPreviews
@Composable
fun CommentPlaceHolder() = HnTheme {
    CardPlaceholder(height = 64.dp)
}

@HnPreviews
@Composable
fun CommentScreenHeaderPlaceholder() = CommentScreenHeader(
    Item(
        id = 0,
        title = "Something very newsworthy has happened again",
        score = 123456,
        by = "neuos",
        descendants = 7890123,
        url = Uri.parse("https://neuhuber.eu/news/1"),
        time = Instant.now(),
    )
)

@Composable
fun CommentCard(
    text: String?,
    modifier: Modifier = Modifier,
    author: String? = null,
    time: Instant? = null,
    childCount: Int = 0,
    depth: Int = 0,
    isExpanded: Boolean = false,
    toggleExpand: () -> Unit = {}
) {
    val expandable = childCount > 0
    val typography = MaterialTheme.typography

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                top = if (depth == 0) 8.dp else 4.dp, start = ((depth + 1) * 4).dp, end = 4.dp
            ),
    ) {
        Column {
            Box(
                modifier = Modifier.padding(
                    top = 8.dp, start = 8.dp, end = 8.dp, bottom = if (expandable) 0.dp else 8.dp
                )
            ) {
                Column {
                    author?.let {
                        Text(
                            "$it - ${time?.toLocalString()}", style = typography.labelSmall
                        )
                    }
                    if (text != null) {
                        HtmlText(text = text)
                    } else {
                        Text(text = "deleted", textDecoration = TextDecoration.LineThrough)
                    }
                }
            }



            if (expandable) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable(
                            onClickLabel = "This is the label", onClick = toggleExpand
                        )
                        .padding(vertical = 8.dp), horizontalArrangement = Arrangement.Center
                ) {
                    Text("$childCount Comments", style = typography.labelLarge)
                    if (isExpanded) Icon(
                        Icons.Filled.KeyboardArrowUp, contentDescription = "collapse"
                    )
                    else Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "expand")

                }
            }
        }
    }
}

@Composable
fun HtmlText(text: String) {
    val context = LocalContext.current
    val linkColor = MaterialTheme.colorScheme.primary

    AndroidView(factory = {
        TextView(context).apply {
            setText(HtmlCompat.fromHtml(text, HtmlCompat.FROM_HTML_MODE_COMPACT))
            textSize = 16f
            setLinkTextColor(linkColor.toArgb())
            movementMethod = LinkMovementMethod.getInstance()
        }
    })
}


