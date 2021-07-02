package eu.neuhuber.hn

import android.app.Application
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class HNApplication : Application() {
    // AppContainer instance used by the rest of classes to obtain dependencies
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainerImpl(this)
    }
}
