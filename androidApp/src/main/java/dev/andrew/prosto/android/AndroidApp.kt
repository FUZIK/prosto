package dev.andrew.prosto.android

import android.app.Application
import dev.andrew.prosto.ToporObject
import dev.andrew.prosto.database.DriverFactory

class AndroidApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ToporObject.provideSqlDriver(DriverFactory(this))
    }
}