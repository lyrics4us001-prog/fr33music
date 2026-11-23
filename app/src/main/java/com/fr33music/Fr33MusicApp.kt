package com.fr33music

import android.app.Application
import coil.Coil
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.fr33music.crash.CrashHandler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class Fr33MusicApp : Application(), ImageLoaderFactory {

    @Inject
    lateinit var imageLoader: ImageLoader

    override fun onCreate() {
        super.onCreate()
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler(CrashHandler(this, defaultHandler))
        Coil.setImageLoader(imageLoader)
    }

    override fun newImageLoader(): ImageLoader = imageLoader
}
