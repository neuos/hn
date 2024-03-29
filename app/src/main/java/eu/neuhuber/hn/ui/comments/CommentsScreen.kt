package eu.neuhuber.hn.ui.comments

import android.graphics.Typeface
import android.net.Uri
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.text.style.URLSpan
import android.text.style.UnderlineSpan
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
import androidx.compose.foundation.text.ClickableText
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.core.text.HtmlCompat
import co.touchlab.kermit.Logger
import eu.neuhuber.hn.R
import eu.neuhuber.hn.data.model.Id
import eu.neuhuber.hn.data.model.Item
import eu.neuhuber.hn.ui.error.ErrorComponent
import eu.neuhuber.hn.ui.newsList.openStory
import eu.neuhuber.hn.ui.theme.HnPreviews
import eu.neuhuber.hn.ui.theme.HnTheme
import eu.neuhuber.hn.ui.util.CardPlaceholder
import eu.neuhuber.hn.ui.util.Favicon
import eu.neuhuber.hn.ui.util.HtmlText
import eu.neuhuber.hn.ui.util.createBitmap
import eu.neuhuber.hn.ui.util.toLocalString
import org.koin.androidx.compose.koinViewModel
import java.time.Instant


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun CommentsScreen(
    newsId: Id?, modifier: Modifier = Modifier, viewModel: CommentsViewModel = koinViewModel()
) = Scaffold { paddingValues ->
    if (newsId == null) Box(modifier = Modifier.padding(paddingValues)) {
        Text(text = "Invalid News Item", modifier = modifier)
    }
    else {
        val refreshing by viewModel.refreshAll.isRefreshing.collectAsState()
        val refreshState =
            rememberPullRefreshState(refreshing, { viewModel.refreshAll(newsId) })
        Box(
            modifier = modifier
                .padding(paddingValues)
                .pullRefresh(refreshState)
        ) {
            val loadComment: LazyCommentTree? = viewModel.loadComment(newsId)
            when {
                viewModel.errorMessage != null -> ErrorComponent(message = viewModel.errorMessage.toString(),
                    retry = { viewModel.refreshAll(newsId) })

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
            CommentScreenHeader(loadComment.node)
            val commentNode = loadComment.node as? LazyCommentNode.Comment
            commentNode?.item?.text?.let {
                CommentCard(
                    text = it,
                    author = commentNode.item.by,
                    time = commentNode.item.time,
                    modifier = Modifier.padding(
                        top = 2.dp, start = 4.dp, end = 4.dp
                    )
                )
            }
        }
        items(loadComment.children, key = { it.id }) {
            CommentNode(id = it.id)
        }
    }
}

@Composable
private fun CommentNode(id: Id, depth: Int = 0, viewModel: CommentsViewModel = koinViewModel()) {
    val expanded = remember { mutableStateOf(depth < 2) }
    val modifier = Modifier
        .padding(
            top = 2.dp, start = ((depth + 1) * 4).dp, end = 4.dp
        )
        .fillMaxWidth()
    when (val commentNode = viewModel.loadComment(39866254)?.node) {
        null, is LazyCommentNode.Loading -> {
            CommentPlaceHolder(
                modifier = modifier
            )
        }

        is LazyCommentNode.Error -> ErrorComponent(
            message = commentNode.message,
            modifier = modifier,
            retry = { viewModel.refreshSingle(id) }
        )

        is LazyCommentNode.Comment -> {
            val item = commentNode.item
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
}

@Composable
private fun CommentScreenHeader(commentNode: LazyCommentNode) {
    val typography = MaterialTheme.typography
    when (commentNode) {
        is LazyCommentNode.Loading -> CommentPlaceHolder()
        is LazyCommentNode.Error -> Text(text = commentNode.message, style = typography.titleLarge)
        is LazyCommentNode.Comment -> ElevatedCard(Modifier.fillMaxWidth()) {
            val item = commentNode.item
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
                            contentDescription = contentDescription,
                            placeholder = painterResource(id = R.drawable.ic_baseline_open_in_browser_24)
                        )
                    }
                }
            }
        }
    }
}


@HnPreviews
@Composable
private fun CommentPlaceHolder(modifier: Modifier = Modifier) = HnTheme {
    CardPlaceholder(height = 64.dp, modifier = modifier)
}

@HnPreviews
@Composable
private fun CommentScreenHeaderPlaceholder() = CommentScreenHeader(
    LazyCommentNode.Comment(
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


