package dev.andrew.prosto.database

import com.squareup.sqldelight.db.SqlDriver
import dev.andrew.prosto.AppDatabase

expect class DriverFactory {
    fun createDriver(): SqlDriver
}

fun createDatabase(driverFactory: DriverFactory): AppDatabase {
    val driver = driverFactory.createDriver()
    val database = AppDatabase(driver)
    return database
}