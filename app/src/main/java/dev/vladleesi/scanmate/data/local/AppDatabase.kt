package dev.vladleesi.scanmate.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import dev.vladleesi.scanmate.data.local.dao.HistoryDao
import dev.vladleesi.scanmate.data.local.entity.HistoryEntity
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

        private const val DB_NAME = "scanner.db"

        operator fun invoke(weakContext: WeakReference<Context>) = instance ?: synchronized(LOCK) {
            instance ?: weakContext.get()?.let { context ->
                buildDatabase(context).also { appDatabase ->
                    instance = appDatabase
                }
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(context, AppDatabase::class.java, DB_NAME)
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}
