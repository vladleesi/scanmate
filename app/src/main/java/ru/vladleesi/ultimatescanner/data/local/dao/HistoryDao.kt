package ru.vladleesi.ultimatescanner.data.local.dao

import androidx.room.*
import ru.vladleesi.ultimatescanner.data.local.entity.HistoryEntity

@Dao
interface HistoryDao {

    @Query("SELECT * FROM history_entity ORDER BY id DESC")
    fun getAll(): List<HistoryEntity>

    @Insert
    fun insert(entity: HistoryEntity)

    @Update
    fun update(entity: HistoryEntity)

    @Delete
    fun delete(entity: HistoryEntity)

    @Query("DELETE FROM history_entity")
    fun nukeTable()
}