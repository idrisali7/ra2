package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.FileCategory
import com.example.model.ModType

@Entity(tableName = "game_files")
data class GameFileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fileName: String,
    val storedPath: String,
    val category: String = FileCategory.OTHER.name,
    val sizeBytes: Long,
    val sha256Short: String,
    val isCoreRa2File: Boolean,
    val importedAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "mod_packages")
data class ModPackageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val fileName: String,
    val storedPath: String,
    val modType: String = ModType.MIX_MOD.name,
    val sizeBytes: Long,
    val isEnabled: Boolean = true,
    val loadOrder: Int = 0,
    val notes: String = "",
    val importedAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "launch_logs")
data class LaunchLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val detail: String,
    val level: String = "INFO", // INFO, SUCCESS, WARN
    val timestamp: Long = System.currentTimeMillis(),
)
