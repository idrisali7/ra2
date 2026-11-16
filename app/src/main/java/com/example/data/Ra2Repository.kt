package com.example.data

import android.content.Context
import android.net.Uri
import com.example.data.local.GameFileEntity
import com.example.data.local.LaunchLogEntity
import com.example.data.local.ModPackageEntity
import com.example.data.local.Ra2Database
import com.example.model.DeviceHardwareInfo
import com.example.model.EngineSettings
import com.example.model.ExternalRunnerApp
import kotlinx.coroutines.flow.Flow
import java.io.File

class Ra2Repository(context: Context) {
    private val db = Ra2Database.getInstance(context)
    private val dao = db.ra2Dao()
    private val settingsRepo = EngineSettingsRepository(context)
    private val containerManager = GameContainerManager(context)
    private val deviceInspector = DeviceInspector(context)

    val settingsFlow: Flow<EngineSettings> = settingsRepo.settingsFlow
    val gameFilesFlow: Flow<List<GameFileEntity>> = dao.observeGameFiles()
    val modsFlow: Flow<List<ModPackageEntity>> = dao.observeMods()
    val logsFlow: Flow<List<LaunchLogEntity>> = dao.observeLogs()

    fun getHardwareInfo(): DeviceHardwareInfo = deviceInspector.inspectHardware()

    fun getExternalRunners(): List<ExternalRunnerApp> = deviceInspector.detectExternalRunners()

    suspend fun updateSettings(transform: (EngineSettings) -> EngineSettings) {
        settingsRepo.update(transform)
    }

    suspend fun importGameFiles(uris: List<Uri>): Int {
        var successCount = 0
        for (uri in uris) {
            val result = containerManager.importGameFile(uri)
            result.onSuccess { entity ->
                dao.deleteGameFileByName(entity.fileName)
                dao.insertGameFile(entity)
                dao.insertLog(
                    LaunchLogEntity(
                        title = "استيراد ملف لعبة أصلي",
                        detail = "${entity.fileName} (${formatBytes(entity.sizeBytes)})",
                        level = "SUCCESS",
                    )
                )
                successCount++
            }.onFailure { err ->
                dao.insertLog(
                    LaunchLogEntity(
                        title = "فشل استيراد ملف",
                        detail = err.message ?: "خطأ غير معروف",
                        level = "WARN",
                    )
                )
            }
        }
        return successCount
    }

    suspend fun importModFiles(uris: List<Uri>): Int {
        val currentMods = dao.getModsSnapshot()
        var order = (currentMods.maxOfOrNull { it.loadOrder } ?: 0) + 1
        var successCount = 0
        for (uri in uris) {
            val result = containerManager.importModFile(uri, order)
            result.onSuccess { mod ->
                dao.insertMod(mod)
                dao.insertLog(
                    LaunchLogEntity(
                        title = "إضافة مود جديد",
                        detail = "${mod.fileName} — مُفعّل في الحاوية",
                        level = "SUCCESS",
                    )
                )
                order++
                successCount++
            }.onFailure { err ->
                dao.insertLog(
                    LaunchLogEntity(
                        title = "تعذر إضافة المود",
                        detail = err.message ?: "خطأ في قراءة الملف",
                        level = "WARN",
                    )
                )
            }
        }
        return successCount
    }

    suspend fun toggleMod(mod: ModPackageEntity, enabled: Boolean) {
        containerManager.syncModActivation(mod, enabled)
        dao.setModEnabled(mod.id, enabled)
        dao.insertLog(
            LaunchLogEntity(
                title = if (enabled) "تفعيل مود" else "تعطيل مود",
                detail = mod.fileName,
                level = "INFO",
            )
        )
    }

    suspend fun moveModPriority(mod: ModPackageEntity, moveUp: Boolean) {
        val mods = dao.getModsSnapshot()
        val idx = mods.indexOfFirst { it.id == mod.id }
        if (idx == -1) return
        val swapIdx = if (moveUp) idx - 1 else idx + 1
        if (swapIdx !in mods.indices) return
        val other = mods[swapIdx]
        dao.updateModOrder(mod.id, other.loadOrder)
        dao.updateModOrder(other.id, mod.loadOrder)
    }

    suspend fun deleteGameFile(file: GameFileEntity) {
        containerManager.deletePhysicalFile(file.storedPath)
        dao.deleteGameFileById(file.id)
        dao.insertLog(
            LaunchLogEntity(
                title = "حذف ملف لعبة",
                detail = file.fileName,
                level = "INFO",
            )
        )
    }

    suspend fun deleteMod(mod: ModPackageEntity) {
        containerManager.deletePhysicalFile(mod.storedPath, mod.fileName)
        dao.deleteModById(mod.id)
        dao.insertLog(
            LaunchLogEntity(
                title = "إزالة مود",
                detail = mod.fileName,
                level = "INFO",
            )
        )
    }

    suspend fun prepareAndWriteConfigs(
        settings: EngineSettings,
        hardware: DeviceHardwareInfo,
    ): GeneratedConfigs {
        // Ensure all enabled mods are synced into gameDriveDir
        val mods = dao.getModsSnapshot()
        mods.forEach { mod ->
            containerManager.syncModActivation(mod, mod.isEnabled)
        }
        val generated = containerManager.writeEngineConfigs(settings, hardware)
        val activeModsCount = mods.count { it.isEnabled }
        dao.insertLog(
            LaunchLogEntity(
                title = "تجهيز حاوية ARM64 (${settings.targetFps} FPS)",
                detail = "${generated.iniFileName} + ddraw.ini | المودات النشطة: $activeModsCount",
                level = "SUCCESS",
            )
        )
        return generated
    }

    fun previewConfigs(settings: EngineSettings, hardware: DeviceHardwareInfo): GeneratedConfigs {
        return GeneratedConfigs(
            iniFileName = settings.selectedPreset.iniFileName,
            ra2IniContent = containerManager.buildRa2Ini(settings, hardware),
            ddrawIniContent = containerManager.buildDdrawIni(settings, hardware),
            launchScriptContent = containerManager.buildArm64LaunchScript(settings, hardware),
            containerPath = containerManager.gameDriveDir.absolutePath,
        )
    }

    suspend fun createBundleZip(): File = containerManager.createConfigBundleZip()

    suspend fun clearLogs() {
        dao.clearLogs()
    }

    companion object {
        fun formatBytes(bytes: Long): String {
            return when {
                bytes >= 1024L * 1024L * 1024L -> "%.2f GB".format(bytes / (1024.0 * 1024.0 * 1024.0))
                bytes >= 1024L * 1024L -> "%.1f MB".format(bytes / (1024.0 * 1024.0))
                bytes >= 1024L -> "%.1f KB".format(bytes / 1024.0)
                else -> "$bytes B"
            }
        }
    }
}
