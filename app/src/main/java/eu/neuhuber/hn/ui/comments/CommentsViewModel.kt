package eu.neuhuber.hn.ui.comments

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eu.neuhuber.hn.data.repo.NewsRepository
import eu.neuhuber.hn.data.repo.HackerNewsRepository
import eu.neuhuber.hn.data.model.CommentTree
import eu.neuhuber.hn.data.model.Id
import kotlinx.coroutines.launch
import java.util.concurrent.Semaphore

class CommentsViewModel : ViewModel() {
    private val newsRepository: NewsRepository = HackerNewsRepository()
    var errorMessage: String? = null

    val comments: MutableState<List<CommentTree>?> = mutableStateOf(null)


    private val sem = Semaphore(1)

    fun loadComments(itemId: Id) {
        if(comments.value!=null) return

        Log.i("cmv", "trying to load comments, sem: ${sem.availablePermits()}")
        if (sem.tryAcquire()) {
            viewModelScope.launch {
                Log.i("cmv", "starting to load comments, sem: ${sem.availablePermits()}")
                comments.value = null
                    newsRepository.getComments(itemId).onSuccess {
                        comments.value = it
                    }.onFailure {
                        comments.value = null
                        errorMessage = it.message
                    }
                sem.release()
                Log.i("cmv", "done loading comments, sem: ${sem.availablePermits()}")
            }
        }
    }

}
