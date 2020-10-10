package com.example.realmpractise


import android.app.Application
import com.mongodb.stitch.android.core.Stitch
import io.realm.Realm
import io.realm.RealmConfiguration

class AppApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Realm.init(this)
        val config = RealmConfiguration.Builder().build()
        Realm.setDefaultConfiguration(config)
        Stitch.initializeDefaultAppClient("clientAppId")
    }
}