package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface Ra2Dao {
    @Query("SELECT * FROM game_files ORDER BY isCoreRa2File DESC, fileName ASC")
    fun observeGameFiles(): Flow<List<GameFileEntity>>

    @Query("SELECT * FROM game_files")
    suspend fun getGameFilesSnapshot(): List<GameFileEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGameFile(file: GameFileEntity): Long

    @Query("DELETE FROM game_files WHERE id = :id")
    suspend fun deleteGameFileById(id: Long)

    @Query("DELETE FROM game_files WHERE fileName = :fileName")
    suspend fun deleteGameFileByName(fileName: String)

    @Query("SELECT * FROM mod_packages ORDER BY loadOrder ASC, importedAt DESC")
    fun observeMods(): Flow<List<ModPackageEntity>>

    @Query("SELECT * FROM mod_packages ORDER BY loadOrder ASC")
    suspend fun getModsSnapshot(): List<ModPackageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMod(mod: ModPackageEntity): Long

    @Update
    suspend fun updateMod(mod: ModPackageEntity)

    @Query("UPDATE mod_packages SET isEnabled = :enabled WHERE id = :id")
    suspend fun setModEnabled(id: Long, enabled: Boolean)

    @Query("UPDATE mod_packages SET loadOrder = :loadOrder WHERE id = :id")
    suspend fun updateModOrder(id: Long, loadOrder: Int)

    @Query("DELETE FROM mod_packages WHERE id = :id")
    suspend fun deleteModById(id: Long)

    @Query("SELECT * FROM launch_logs ORDER BY timestamp DESC LIMIT 40")
    fun observeLogs(): Flow<List<LaunchLogEntity>>

    @Insert
    suspend fun insertLog(log: LaunchLogEntity)

    @Query("DELETE FROM launch_logs")
    suspend fun clearLogs()
}
