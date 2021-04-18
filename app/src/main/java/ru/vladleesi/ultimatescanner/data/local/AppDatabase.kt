package ru.vladleesi.ultimatescanner.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import ru.vladleesi.ultimatescanner.data.local.dao.HistoryDao
import ru.vladleesi.ultimatescanner.data.local.entity.HistoryEntity
import java.lang.ref.WeakReference

@Database(
    entities = [HistoryEntity::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: WeakReference<Context>) = instance ?: synchronized(LOCK) {
            instance ?: context.get()
                ?.let { buildDatabase(it).also { appDatabase -> instance = appDatabase } }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context,
                AppDatabase::class.java, "scanner.db"
            )
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}