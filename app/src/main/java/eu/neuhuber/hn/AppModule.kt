package eu.neuhuber.hn

import eu.neuhuber.hn.data.repo.HackerNewsRepository
import eu.neuhuber.hn.data.repo.NewsRepository
import eu.neuhuber.hn.ui.comments.CommentsViewModel
import eu.neuhuber.hn.ui.newsList.BestNewsListViewModel
import eu.neuhuber.hn.ui.newsList.NewNewsListViewModel
import eu.neuhuber.hn.ui.newsList.TopNewsListViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val appModule = module {
    singleOf(::HackerNewsRepository) { bind<NewsRepository>() }
    viewModel { CommentsViewModel(get()) }
    viewModelOf(::CommentsViewModel)
    viewModelOf(::TopNewsListViewModel)
    viewModelOf(::NewNewsListViewModel)
    viewModelOf(::BestNewsListViewModel)

}