package com.brios.miempresa.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Company::class, Category::class], version = 2)
abstract class MiEmpresaDatabase : RoomDatabase() {
    abstract fun companyDao(): CompanyDao

    abstract fun categoryDao(): CategoryDao

    companion object {
        @Volatile
        private var INSTANCE: MiEmpresaDatabase? = null

        fun getDatabase(context: Context): MiEmpresaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance =
                    Room.databaseBuilder(
                        context.applicationContext,
                        MiEmpresaDatabase::class.java,
                        "miempresa_database",
                    ).fallbackToDestructiveMigration()
                        .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
