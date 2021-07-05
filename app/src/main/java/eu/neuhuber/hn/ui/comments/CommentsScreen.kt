package eu.neuhuber.hn.ui.comments

import android.text.method.LinkMovementMethod
import android.widget.TextView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.HtmlCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import eu.neuhuber.hn.R
import eu.neuhuber.hn.data.model.Id
import eu.neuhuber.hn.data.model.Item
import eu.neuhuber.hn.ui.home.openStory
import eu.neuhuber.hn.ui.theme.typography
import eu.neuhuber.hn.ui.util.CardPlaceholder
import eu.neuhuber.hn.ui.util.Favicon
import eu.neuhuber.hn.ui.util.createBitmap


// TODO: top bar
// TODO: icon
// TODO: top, newest, catchup


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CommentsScreen(
    newsId: Id?,
    onBack: () -> Unit,
    viewModel: CommentsViewModel = viewModel()
) {
    if (newsId == null) Text(text = "Invalid News Item")
    else {

        val loadComment: LazyCommentTree? = viewModel.loadComment(newsId)
        if (loadComment == null) CommentPlaceHolder()
        else {
            LazyColumn(Modifier.fillMaxHeight()) {
                item {
                    CommentScreenHeader(loadComment.item)
                }
                items(loadComment.children) {
                    CommentNode(id = it.id)
                }
            }
        }
    }
}

@Composable
fun CommentScreenHeader(item: Item?) {
    val typography = typography()
    if (item == null) CommentPlaceHolder()
    else {
        Card(Modifier.fillMaxWidth(), elevation = 8.dp) {

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
                        .padding(8.dp)
                        .fillMaxHeight()
                ) {
                    Text(item.by ?: "no author", style = typography.overline)
                    Text(item.title ?: "no title", style = typography.h6)
                    Text(item.url?.host ?: "url", style = typography.caption)
                }
                if (item.url != null) {

                    val context = LocalContext.current
                    val colors = MaterialTheme.colors
                    val icon = remember{ createBitmap(context, R.drawable.ic_baseline_question_answer_24) }

                    Column(
                        modifier = Modifier
                            .width(40.dp)
                            .defaultMinSize(minHeight = 64.dp)
                            .fillMaxHeight()
                            .clickable { openStory(context, item, colors, icon) }
                            .padding(8.dp),
                        Arrangement.Center,
                        Alignment.CenterHorizontally
                    ) {
                        val contentDescription = "open in browser"
                        Favicon(uri = item.url, contentDescription) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_baseline_open_in_browser_24),
                                contentDescription = contentDescription
                            )
                        }
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun CommentNode(id: Id, depth: Int = 0, viewModel: CommentsViewModel = viewModel()) {
    val expanded = remember { mutableStateOf(depth < 2) }

    val item = viewModel.loadComment(id)?.item

    if (item == null)
        CommentPlaceHolder()
    else {
        CommentCard(
            text = item.text.toString(),
            childCount = item.kids?.size ?: 0,
            depth = depth,
            isExpanded = expanded.value
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


@Preview
@Composable
fun CommentPlaceHolder() = CardPlaceholder(height = 64.dp)

@Composable
fun CommentCard(
    modifier: Modifier = Modifier,
    text: String,
    childCount: Int,
    depth: Int,
    isExpanded: Boolean,
    toggleExpand: () -> Unit
) {
    val expandable = childCount > 0
    val typography = typography()

    Card(
        elevation = (4).dp,
        modifier = modifier
            .fillMaxWidth()
            .padding(
                top = if (depth == 0) 8.dp else 4.dp,
                start = ((depth + 1) * 4).dp,
                end = 4.dp
            ),
    ) {
        Column {
            Box(
                modifier = Modifier.padding(
                    top = 8.dp,
                    start = 8.dp,
                    end = 8.dp,
                    bottom = if (expandable) 0.dp else 8.dp
                )
            )
            { HtmlText(text = text) }



            if (expandable) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable(
                            enabled = expandable,
                            "This is the label",
                            onClick = toggleExpand
                        )
                        .padding(vertical = 8.dp), horizontalArrangement = Arrangement.Center
                ) {
                    Text("$childCount Comments", style = typography.button)
                    if (isExpanded)
                        Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "collapse")
                    else
                        Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "expand")

                }
            }
        }
    }
}

@Composable
fun HtmlText(text: String) {
    val context = LocalContext.current
    val linkColor = MaterialTheme.colors.primary

    AndroidView(factory = {
        TextView(context).apply {
            setText(HtmlCompat.fromHtml(text, HtmlCompat.FROM_HTML_MODE_COMPACT))
            textSize = 16f
            setLinkTextColor(linkColor.toArgb())
            movementMethod = LinkMovementMethod.getInstance()
        }
    })
}


