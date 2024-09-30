package com.brios.miempresa.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Company::class], version = 1)
abstract class MiEmpresaDatabase : RoomDatabase() {
    abstract fun companyDao(): CompanyDao
    companion object {
        @Volatile
        private var INSTANCE: MiEmpresaDatabase? = null
        fun getDatabase(context: Context): MiEmpresaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MiEmpresaDatabase::class.java,
                    "miempresa_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}