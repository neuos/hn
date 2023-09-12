package eu.neuhuber.hn

import android.app.Application
import org.koin.core.context.startKoin

class HNApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin { modules(appModule) }
    }
}
