package com.example.whatsappcontacts

import android.app.Application

class App : Application() {
    companion object {
        var firstLaunch  =  true
    }
    override fun onCreate() {
        super.onCreate()
        firstLaunch = true
    }
}