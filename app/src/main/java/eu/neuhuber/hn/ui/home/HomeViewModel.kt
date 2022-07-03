package eu.neuhuber.hn.ui.home

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.CONFLATED


class HomeViewModel : ViewModel() {
    val scrollToTop = mutableStateOf(Channel<ListType>(CONFLATED))
    val selected = mutableStateOf(ListType.Top)

    suspend fun navBarSelect(it: ListType) = when (selected.value) {
        it -> scrollToTop.value.send(it)
        else -> selected.value = it
    }
}
