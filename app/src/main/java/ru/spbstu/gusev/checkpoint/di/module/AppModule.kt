package ru.spbstu.gusev.checkpoint.di.module

import android.content.Context
import androidx.room.Room
import ru.spbstu.gusev.checkpoint.database.CheckDatabase
import ru.spbstu.gusev.checkpoint.model.CheckItem
import ru.spbstu.gusev.checkpoint.model.FirestoreRepository
import ru.spbstu.gusev.checkpoint.model.MockRepository
import toothpick.config.Module

class AppModule(context: Context) : Module() {
    init {
        bind(Context::class.java).toInstance(context)
        val localDatabase = Room.databaseBuilder(
            context,
            CheckDatabase::class.java,
            "check_database"
        ).build()

        bind(MockRepository::class.java).toInstance(MockRepository(context))
        bind(CheckItem::class.java).toInstance(CheckItem())
        bind(CheckDatabase::class.java).toInstance(
            localDatabase
        )
        bind(FirestoreRepository::class.java).toInstance(FirestoreRepository(localDatabase))
    }
}