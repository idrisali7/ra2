package com.example.data

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.example.data.local.GameFileEntity
import com.example.data.local.ModPackageEntity
import com.example.model.Arm64BridgeMode
import com.example.model.DeviceHardwareInfo
import com.example.model.EngineSettings
import com.example.model.FileCategory
import com.example.model.GamePreset
import com.example.model.ModType
import com.example.model.ScreenFillMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class GameContainerManager(private val context: Context) {

    val containerRoot: File
        get() = File(context.filesDir, "ra2_arm64_container").apply { mkdirs() }

    val gameDriveDir: File
        get() = File(containerRoot, "drive_c/Westwood/RA2").apply { mkdirs() }

    val modsLibraryDir: File
        get() = File(containerRoot, "mods_library").apply { mkdirs() }

    val configDir: File
        get() = File(containerRoot, "config").apply { mkdirs() }

    suspend fun importGameFile(uri: Uri): Result<GameFileEntity> = withContext(Dispatchers.IO) {
        runCatching {
            val fileName = resolveFileName(uri)
            val safeName = fileName.replace(Regex("[^a-zA-Z0-9._-]"), "_")
            val destFile = File(gameDriveDir, safeName)
            val (size, shaShort) = copyUriToFile(uri, destFile)
            val lower = safeName.lowercase()
            val category = classifyGameFile(lower)
            val isCore = isCoreRedAlertFile(lower)

            GameFileEntity(
                fileName = safeName,
                storedPath = destFile.absolutePath,
                category = category.name,
                sizeBytes = size,
                sha256Short = shaShort,
                isCoreRa2File = isCore,
            )
        }
    }

    suspend fun importModFile(uri: Uri, nextOrder: Int): Result<ModPackageEntity> = withContext(Dispatchers.IO) {
        runCatching {
            val fileName = resolveFileName(uri)
            val safeName = fileName.replace(Regex("[^a-zA-Z0-9._-]"), "_")
            val libraryFile = File(modsLibraryDir, safeName)
            val (size, _) = copyUriToFile(uri, libraryFile)
            val lower = safeName.lowercase()
            val modType = classifyModFile(lower)

            // Copy directly into active game directory because enabled = true by default
            val activeDest = File(gameDriveDir, safeName)
            libraryFile.copyTo(activeDest, overwrite = true)

            val prettyName = safeName.substringBeforeLast('.')
                .replace('_', ' ')
                .replace('-', ' ')
                .trim()
                .ifBlank { safeName }

            ModPackageEntity(
                name = prettyName,
                fileName = safeName,
                storedPath = libraryFile.absolutePath,
                modType = modType.name,
                sizeBytes = size,
                isEnabled = true,
                loadOrder = nextOrder,
                notes = when (modType) {
                    ModType.MIX_MOD -> "حزمة موارد MIX تُحمّل داخل مجلد اللعبة عند التشغيل"
                    ModType.INI_PATCH -> "تعديل قواعد أو وحدات اللعبة (Rules / Art INI)"
                    ModType.MAP_PACK -> "خرائط مواجهات إضافية (Skirmish / Multiplayer)"
                    ModType.SHADER_MOD -> "فلتر رسوميات GLSL لتحسين الحدة والألوان"
                    ModType.ARCHIVE -> "حزمة مود مضغوطة جاهزة للاستخدام داخل الحاوية"
                },
            )
        }
    }

    suspend fun syncModActivation(mod: ModPackageEntity, enabled: Boolean) = withContext(Dispatchers.IO) {
        val libraryFile = File(mod.storedPath)
        val activeFile = File(gameDriveDir, mod.fileName)
        if (enabled) {
            if (libraryFile.exists()) {
                libraryFile.copyTo(activeFile, overwrite = true)
            }
        } else {
            if (activeFile.exists()) {
                activeFile.delete()
            }
        }
    }

    suspend fun deletePhysicalFile(path: String, alsoDeleteFromGameDirName: String? = null) = withContext(Dispatchers.IO) {
        runCatching { File(path).delete() }
        if (alsoDeleteFromGameDirName != null) {
            runCatching { File(gameDriveDir, alsoDeleteFromGameDirName).delete() }
        }
    }

    suspend fun writeEngineConfigs(
        settings: EngineSettings,
        hardware: DeviceHardwareInfo,
    ): GeneratedConfigs = withContext(Dispatchers.IO) {
        val ra2IniText = buildRa2Ini(settings, hardware)
        val ddrawIniText = buildDdrawIni(settings, hardware)
        val launchScriptText = buildArm64LaunchScript(settings, hardware)

        val iniName = settings.selectedPreset.iniFileName
        val gameIniFile = File(gameDriveDir, iniName)
        val ddrawFile = File(gameDriveDir, "ddraw.ini")
        val scriptFile = File(configDir, "arm64_launch.sh")

        gameIniFile.writeText(ra2IniText)
        ddrawFile.writeText(ddrawIniText)
        scriptFile.writeText(launchScriptText)

        // Also mirror into configDir for clean export
        File(configDir, iniName).writeText(ra2IniText)
        File(configDir, "ddraw.ini").writeText(ddrawIniText)

        GeneratedConfigs(
            iniFileName = iniName,
            ra2IniContent = ra2IniText,
            ddrawIniContent = ddrawIniText,
            launchScriptContent = launchScriptText,
            containerPath = gameDriveDir.absolutePath,
        )
    }

    suspend fun createConfigBundleZip(): File = withContext(Dispatchers.IO) {
        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val zipFile = File(exportDir, "ra2_arm64_60fps_bundle.zip")
        ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
            configDir.listFiles()?.forEach { file ->
                if (file.isFile) {
                    zos.putNextEntry(ZipEntry(file.name))
                    file.inputStream().use { it.copyTo(zos) }
                    zos.closeEntry()
                }
            }
        }
        zipFile
    }

    fun buildRa2Ini(settings: EngineSettings, hardware: DeviceHardwareInfo): String {
        val (targetW, targetH) = computeEffectiveResolution(settings, hardware)
        val backBuffer = if (settings.disableVideoBackBuffer) "no" else "yes"
        val stretchMovies = if (settings.screenFillMode == ScreenFillMode.FILL_MOBILE_SCREEN) "yes" else "no"
        val isYuri = settings.selectedPreset == GamePreset.YURIS_REVENGE
        return buildString {
            appendLine("; Generated by RA2 ARM64 Launcher")
            appendLine("[Video]")
            appendLine("ScreenWidth=$targetW")
            appendLine("ScreenHeight=$targetH")
            appendLine("StretchMovies=$stretchMovies")
            appendLine("AllowHiResModes=yes")
            appendLine("AllowVRAMSidebar=no")
            appendLine("VideoBackBuffer=$backBuffer")
            appendLine()
            appendLine("[Options]")
            appendLine("GameSpeed=0")
            appendLine("Difficulty=1")
            appendLine("ScrollMethod=0")
            appendLine("ScrollRate=5")
            appendLine("DetailLevel=2")
            appendLine("ToolTips=yes")
            appendLine("UnitActionLines=yes")
            appendLine("ShowHidden=yes")
            appendLine()
            appendLine("[Audio]")
            appendLine("SoundVolume=0.800000")
            appendLine("VoiceVolume=0.850000")
            appendLine("ScoreVolume=0.550000")
            appendLine("IsScoreRepeat=yes")
            appendLine("IsScoreShuffle=yes")
            if (isYuri) {
                appendLine()
                appendLine("[Phobos]")
                appendLine("ShowUnitHealth=yes")
            }
        }.trimEnd()
    }

    fun buildDdrawIni(settings: EngineSettings, hardware: DeviceHardwareInfo): String {
        val (targetW, targetH) = computeEffectiveResolution(settings, hardware)
        val maintainAspect = settings.screenFillMode.maintainAspect
        return buildString {
            appendLine("; cnc-ddraw optimized profile for Android ARM64")
            appendLine("[ddraw]")
            appendLine("width=$targetW")
            appendLine("height=$targetH")
            appendLine("fullscreen=true")
            appendLine("windowed=false")
            appendLine("maintas=$maintainAspect")
            appendLine("maxfps=${settings.targetFps}")
            appendLine("vsync=${settings.enableVSync}")
            appendLine("renderer=${settings.graphicsRenderer.ddrawValue}")
            appendLine("shader=${settings.shaderFilter.configValue}")
            appendLine("singlecpu=${settings.enableSingleCpuFix}")
            appendLine("adjmouse=true")
            appendLine("devmode=false")
            appendLine("maxgameticks=${settings.targetFps}")
            appendLine("minfps=${minOf(settings.targetFps, 60)}")
            appendLine("nonexclusive=true")
            appendLine("noactivateapp=true")
        }.trimEnd()
    }

    fun buildArm64LaunchScript(settings: EngineSettings, hardware: DeviceHardwareInfo): String {
        val (targetW, targetH) = computeEffectiveResolution(settings, hardware)
        val dynarecFast = if (settings.enableDynarecFastMath) "1" else "0"
        val esync = if (settings.enableEsync) "1" else "0"
        val bridgeBinary = when (settings.arm64BridgeMode) {
            Arm64BridgeMode.ARM64_WOW64_BOX64 -> "box64 wine"
            Arm64BridgeMode.ARM64_FEX_CORE -> "FEXInterpreter wine"
        }
        return buildString {
            appendLine("#!/system/bin/sh")
            appendLine("# ARM64-v8a Runtime Profile (${hardware.primaryAbi})")
            appendLine("export BOX64_DYNAREC=1")
            appendLine("export BOX64_DYNAREC_FASTNAN=$dynarecFast")
            appendLine("export BOX64_DYNAREC_FASTROUND=$dynarecFast")
            appendLine("export BOX64_DYNAREC_BIGBLOCK=2")
            appendLine("export BOX64_DYNAREC_STRONGMEM=1")
            appendLine("export WINEESYNC=$esync")
            appendLine("export WINE_LARGE_ADDRESS_AWARE=1")
            appendLine("export DXVK_HUD=fps")
            appendLine("export DXVK_FRAME_RATE=${settings.targetFps}")
            appendLine("export PROTON_ENABLE_NVAPI=0")
            appendLine("export MESA_GL_VERSION_OVERRIDE=4.5")
            appendLine("export WINEDLLOVERRIDES=\"ddraw=n,b\"")
            appendLine("export RA2_TARGET_RES=\"${targetW}x${targetH}\"")
            appendLine("export RA2_FULLSCREEN_FILL=\"${!settings.screenFillMode.maintainAspect}\"")
            appendLine()
            appendLine("cd \"${gameDriveDir.absolutePath}\"")
            appendLine("$bridgeBinary \"${settings.selectedPreset.executableName}\" -win -speedcontrol")
        }.trimEnd()
    }

    fun computeEffectiveResolution(
        settings: EngineSettings,
        hardware: DeviceHardwareInfo,
    ): Pair<Int, Int> {
        val scale = settings.renderScalePercent.coerceIn(50, 100) / 100f
        val baseH = (720 * scale).toInt().coerceAtLeast(480)
        val rawW = when (settings.screenFillMode) {
            ScreenFillMode.FILL_MOBILE_SCREEN -> {
                val ratio = if (hardware.screenHeightPx > 0) {
                    hardware.screenWidthPx.toFloat() / hardware.screenHeightPx.toFloat()
                } else {
                    20f / 9f
                }
                (baseH * ratio).toInt()
            }
            ScreenFillMode.WIDESCREEN_16_9 -> (baseH * 16f / 9f).toInt()
            ScreenFillMode.CLASSIC_4_3 -> (baseH * 4f / 3f).toInt()
        }
        // Align width/height to even numbers for DirectDraw compatibility
        val evenW = (rawW / 2) * 2
        val evenH = (baseH / 2) * 2
        return evenW.coerceAtLeast(640) to evenH.coerceAtLeast(480)
    }

    private fun resolveFileName(uri: Uri): String {
        context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            ?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (idx >= 0) {
                        val name = cursor.getString(idx)
                        if (!name.isNullOrBlank()) return name
                    }
                }
            }
        return uri.lastPathSegment?.substringAfterLast('/') ?: "imported_file.bin"
    }

    private fun copyUriToFile(uri: Uri, dest: File): Pair<Long, String> {
        val digest = MessageDigest.getInstance("SHA-256")
        var total = 0L
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(dest).use { output ->
                val buffer = ByteArray(16 * 1024)
                while (true) {
                    val read = input.read(buffer)
                    if (read <= 0) break
                    output.write(buffer, 0, read)
                    digest.update(buffer, 0, read)
                    total += read
                }
            }
        } ?: error("تعذر قراءة الملف المختار")
        val shaShort = digest.digest().take(4).joinToString("") { "%02x".format(it) }
        return total to shaShort
    }

    private fun classifyGameFile(lowerName: String): FileCategory = when {
        lowerName.endsWith(".exe") -> FileCategory.EXECUTABLE
        lowerName.endsWith(".mix") -> FileCategory.MIX_DATA
        lowerName.endsWith(".dll") -> FileCategory.DLL
        lowerName.endsWith(".ini") || lowerName.endsWith(".cfg") -> FileCategory.CONFIG
        else -> FileCategory.OTHER
    }

    private fun isCoreRedAlertFile(lowerName: String): Boolean {
        val coreSet = setOf(
            "ra2.exe",
            "game.exe",
            "yuri.exe",
            "ra2md.exe",
            "gamemd.exe",
            "ra2.mix",
            "ra2md.mix",
            "language.mix",
            "langmd.mix",
            "multi.mix",
            "multimd.mix",
            "binkw32.dll",
            "blowfish.dll",
            "ddraw.dll",
        )
        return lowerName in coreSet
    }

    private fun classifyModFile(lowerName: String): ModType = when {
        lowerName.endsWith(".mix") -> ModType.MIX_MOD
        lowerName.endsWith(".ini") || lowerName.endsWith(".csf") -> ModType.INI_PATCH
        lowerName.endsWith(".map") || lowerName.endsWith(".yrm") || lowerName.endsWith(".mpr") -> ModType.MAP_PACK
        lowerName.endsWith(".glsl") || lowerName.endsWith(".fx") -> ModType.SHADER_MOD
        else -> ModType.ARCHIVE
    }
}

data class GeneratedConfigs(
    val iniFileName: String,
    val ra2IniContent: String,
    val ddrawIniContent: String,
    val launchScriptContent: String,
    val containerPath: String,
)
