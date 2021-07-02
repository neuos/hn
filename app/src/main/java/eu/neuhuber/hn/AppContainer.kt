package eu.neuhuber.hn

import android.content.Context
import eu.neuhuber.hn.data.repo.FakeNewsRepository
import eu.neuhuber.hn.data.repo.NewsRepository

interface AppContainer {
    val newsRepository: NewsRepository
}

class AppContainerImpl(private val context: Context): AppContainer{
    override val newsRepository: NewsRepository by lazy {
        FakeNewsRepository()
    }
}

