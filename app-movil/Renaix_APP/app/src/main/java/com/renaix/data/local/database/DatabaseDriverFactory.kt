package com.renaix.data.local.database

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.renaix.util.Constants

/**
 * Factory para crear el driver de SQLDelight
 * Permite la inyección de dependencias y facilita el testing
 */
class DatabaseDriverFactory(private val context: Context) {

    /**
     * Crea el driver de base de datos para Android
     */
    fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(
            schema = RenaixDatabase.Schema,
            context = context,
            name = Constants.DATABASE_NAME
        )
    }
}

/**
 * Helper class para gestionar la base de datos
 */
class DatabaseHelper(driverFactory: DatabaseDriverFactory) {
    private val driver: SqlDriver = driverFactory.createDriver()
    val database: RenaixDatabase = RenaixDatabase(driver)

    /**
     * Cierra la conexión a la base de datos
     */
    fun close() {
        driver.close()
    }
}
