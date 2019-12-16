package ru.spbstu.gusev.checkpoint.di.module

import android.content.Context
import androidx.room.Room
import ru.spbstu.gusev.checkpoint.model.CheckItem
import ru.spbstu.gusev.checkpoint.model.MockRepository
import ru.spbstu.gusev.checkpoint.model.database.CheckDatabase
import toothpick.config.Module

class AppModule(context: Context) : Module() {
    init {
        bind(Context::class.java).toInstance(context)

        bind(MockRepository::class.java).toInstance(MockRepository(context))
        bind(CheckItem::class.java).toInstance(CheckItem())
        bind(CheckDatabase::class.java).toInstance(
            Room.databaseBuilder(
                context,
                CheckDatabase::class.java,
                "check_database"
            ).build()
        )
    }
}