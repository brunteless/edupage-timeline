package edu.brunteless.timeline.di

import android.app.Application
import edu.brunteless.timeline.network.createHttpClient
import edu.brunteless.timeline.repositories.EdupageRepository
import edu.brunteless.timeline.viewmodels.LoginViewModel
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module


val appModule = module {
    single<HttpClient> {
        createHttpClient(OkHttp.create())
    }
    singleOf(::EdupageRepository)
    viewModelOf(::LoginViewModel)
}


class KoinApplication : Application(){
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@KoinApplication)
            modules(appModule)
        }
    }
}