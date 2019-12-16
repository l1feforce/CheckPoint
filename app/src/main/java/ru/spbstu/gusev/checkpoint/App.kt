package ru.spbstu.gusev.checkpoint

import android.app.Application
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import ru.spbstu.gusev.checkpoint.di.DI
import ru.spbstu.gusev.checkpoint.di.module.AppModule
import toothpick.Toothpick
import toothpick.configuration.Configuration

class App : Application(), ViewModelStoreOwner {

    private val appViewModelStore: ViewModelStore by lazy {
        ViewModelStore()
    }

    override fun getViewModelStore() = appViewModelStore

    override fun onCreate() {
        super.onCreate()

        //TODO initialize libs
        initToothpick()
        initAppScope()
    }

    private fun initToothpick() {
        if (BuildConfig.DEBUG) {
            Toothpick.setConfiguration(Configuration.forDevelopment().preventMultipleRootScopes())
        } else {
            Toothpick.setConfiguration(Configuration.forProduction())
        }
    }

    private fun initAppScope() {
        Toothpick.openScope(DI.APP_SCOPE)
            .installModules(AppModule(this))
    }

}