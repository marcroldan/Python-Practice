package com.marcroldan.rimemba

import android.app.Application
import com.marcroldan.rimemba.di.AppContainer

class RimembaApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}

