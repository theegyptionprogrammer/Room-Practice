package com.example.realmpractise


import android.app.Application
import io.realm.Realm

class AppApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Realm.init(this)
    }
}