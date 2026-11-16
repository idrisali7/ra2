package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.GeneratedConfigs
import com.example.data.Ra2Repository
import com.example.data.local.GameFileEntity
import com.example.data.local.LaunchLogEntity
import com.example.data.local.ModPackageEntity
import com.example.model.Arm64BridgeMode
import com.example.model.DeviceHardwareInfo
import com.example.model.EngineSettings
import com.example.model.ExternalRunnerApp
import com.example.model.GamePreset
import com.example.model.GraphicsRenderer
import com.example.model.ScreenFillMode
import com.example.model.ShaderFilter
import com.example.model.TouchControlMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

data class LauncherUiState(
    val settings: EngineSettings = EngineSettings(),
    val hardware: DeviceHardwareInfo? = null,
    val gameFiles: List<GameFileEntity> = emptyList(),
    val mods: List<ModPackageEntity> = emptyList(),
    val logs: List<LaunchLogEntity> = emptyList(),
    val externalRunners: List<ExternalRunnerApp> = emptyList(),
    val generatedConfigs: GeneratedConfigs? = null,
    val statusMessage: String? = null,
    val isPreparingLaunch: Boolean = false,
) {
    val activeModsCount: Int get() = mods.count { it.isEnabled }
    val hasExecutable: Boolean
        get() = gameFiles.any { it.fileName.lowercase().endsWith(".exe") }
    val hasCoreMix: Boolean
        get() = gameFiles.any {
            val lower = it.fileName.lowercase()
            lower == "ra2.mix" || lower == "ra2md.mix" || lower.endsWith(".mix")
        }
    val coreFilesReadyCount: Int
        get() = gameFiles.count { it.isCoreRa2File }
    val totalContainerBytes: Long
        get() = gameFiles.sumOf { it.sizeBytes } + mods.sumOf { it.sizeBytes }
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = Ra2Repository(application)

    private val hardwareState = MutableStateFlow(repository.getHardwareInfo())
    private val externalRunnersState = MutableStateFlow(repository.getExternalRunners())
    private val statusMessageState = MutableStateFlow<String?>(null)
    private val isPreparingState = MutableStateFlow(false)

    val uiState: StateFlow<LauncherUiState> = combine(
        repository.settingsFlow,
        repository.gameFilesFlow,
        repository.modsFlow,
        repository.logsFlow,
        hardwareState,
        externalRunnersState,
        statusMessageState,
        isPreparingState,
    ) { values ->
        val settings = values[0] as EngineSettings
        @Suppress("UNCHECKED_CAST")
        val gameFiles = values[1] as List<GameFileEntity>
        @Suppress("UNCHECKED_CAST")
        val mods = values[2] as List<ModPackageEntity>
        @Suppress("UNCHECKED_CAST")
        val logs = values[3] as List<LaunchLogEntity>
        val hardware = values[4] as DeviceHardwareInfo
        @Suppress("UNCHECKED_CAST")
        val runners = values[5] as List<ExternalRunnerApp>
        val statusMsg = values[6] as String?
        val preparing = values[7] as Boolean

        val configs = repository.previewConfigs(settings, hardware)

        LauncherUiState(
            settings = settings,
            hardware = hardware,
            gameFiles = gameFiles,
            mods = mods,
            logs = logs,
            externalRunners = runners,
            generatedConfigs = configs,
            statusMessage = statusMsg,
            isPreparingLaunch = preparing,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LauncherUiState(
            hardware = hardwareState.value,
            externalRunners = externalRunnersState.value,
        ),
    )

    fun refreshDeviceAndRunners() {
        hardwareState.value = repository.getHardwareInfo()
        externalRunnersState.value = repository.getExternalRunners()
    }

    fun clearStatusMessage() {
        statusMessageState.value = null
    }

    fun selectPreset(preset: GamePreset) {
        viewModelScope.launch {
            repository.updateSettings { it.copy(selectedPreset = preset) }
        }
    }

    fun selectArm64Bridge(mode: Arm64BridgeMode) {
        viewModelScope.launch {
            repository.updateSettings { it.copy(arm64BridgeMode = mode) }
        }
    }

    fun setTargetFps(fps: Int) {
        viewModelScope.launch {
            repository.updateSettings { it.copy(targetFps = fps) }
        }
    }

    fun setVSync(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateSettings { it.copy(enableVSync = enabled) }
        }
    }

    fun selectRenderer(renderer: GraphicsRenderer) {
        viewModelScope.launch {
            repository.updateSettings { it.copy(graphicsRenderer = renderer) }
        }
    }

    fun selectScreenFillMode(mode: ScreenFillMode) {
        viewModelScope.launch {
            repository.updateSettings { it.copy(screenFillMode = mode) }
        }
    }

    fun selectShaderFilter(shader: ShaderFilter) {
        viewModelScope.launch {
            repository.updateSettings { it.copy(shaderFilter = shader) }
        }
    }

    fun setRenderScale(percent: Int) {
        viewModelScope.launch {
            repository.updateSettings { it.copy(renderScalePercent = percent.coerceIn(50, 100)) }
        }
    }

    fun setSingleCpuFix(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateSettings { it.copy(enableSingleCpuFix = enabled) }
        }
    }

    fun setDisableVideoBackBuffer(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateSettings { it.copy(disableVideoBackBuffer = enabled) }
        }
    }

    fun setDynarecFastMath(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateSettings { it.copy(enableDynarecFastMath = enabled) }
        }
    }

    fun setEsync(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateSettings { it.copy(enableEsync = enabled) }
        }
    }

    fun setEdgeToEdgeCutout(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateSettings { it.copy(enableEdgeToEdgeCutout = enabled) }
        }
    }

    fun setTouchControlMode(mode: TouchControlMode) {
        viewModelScope.launch {
            repository.updateSettings { it.copy(touchControlMode = mode) }
        }
    }

    fun setShowTouchHudButtons(show: Boolean) {
        viewModelScope.launch {
            repository.updateSettings { it.copy(showTouchHudButtons = show) }
        }
    }

    fun applyMaximumSmoothnessPreset() {
        viewModelScope.launch {
            repository.updateSettings {
                it.copy(
                    targetFps = 60,
                    enableVSync = true,
                    graphicsRenderer = GraphicsRenderer.CNC_DDRAW_OPENGL,
                    screenFillMode = ScreenFillMode.FILL_MOBILE_SCREEN,
                    shaderFilter = ShaderFilter.BILINEAR,
                    renderScalePercent = 100,
                    enableSingleCpuFix = true,
                    disableVideoBackBuffer = true,
                    enableDynarecFastMath = true,
                    enableEsync = true,
                    enableEdgeToEdgeCutout = true,
                )
            }
            statusMessageState.value = "تم تفعيل وضع السلاسة القصوى: 60 FPS + ملء الشاشة + ARM64 Dynarec"
        }
    }

    fun importGameFiles(uris: List<Uri>) {
        if (uris.isEmpty()) return
        viewModelScope.launch {
            val count = repository.importGameFiles(uris)
            statusMessageState.value = if (count > 0) {
                "تم استيراد $count من ملفات اللعبة الأصلية إلى حاوية ARM64"
            } else {
                "لم يتم استيراد أي ملف، تأكد من صلاحية الوصول للملف"
            }
        }
    }

    fun importModFiles(uris: List<Uri>) {
        if (uris.isEmpty()) return
        viewModelScope.launch {
            val count = repository.importModFiles(uris)
            statusMessageState.value = if (count > 0) {
                "تمت إضافة وتفعيل $count مود في مجلد اللعبة"
            } else {
                "تعذر استيراد ملف المود"
            }
        }
    }

    fun toggleMod(mod: ModPackageEntity, enabled: Boolean) {
        viewModelScope.launch {
            repository.toggleMod(mod, enabled)
        }
    }

    fun moveModPriority(mod: ModPackageEntity, moveUp: Boolean) {
        viewModelScope.launch {
            repository.moveModPriority(mod, moveUp)
        }
    }

    fun deleteGameFile(file: GameFileEntity) {
        viewModelScope.launch {
            repository.deleteGameFile(file)
            statusMessageState.value = "تم حذف ${file.fileName}"
        }
    }

    fun deleteMod(mod: ModPackageEntity) {
        viewModelScope.launch {
            repository.deleteMod(mod)
            statusMessageState.value = "تم إزالة المود ${mod.fileName}"
        }
    }

    fun prepareContainerAndThen(onReady: (GeneratedConfigs) -> Unit) {
        viewModelScope.launch {
            isPreparingState.value = true
            val current = uiState.value
            val hw = current.hardware ?: repository.getHardwareInfo()
            val configs = repository.prepareAndWriteConfigs(current.settings, hw)
            isPreparingState.value = false
            onReady(configs)
        }
    }

    fun exportBundleZip(onZipReady: (File) -> Unit) {
        viewModelScope.launch {
            val current = uiState.value
            val hw = current.hardware ?: repository.getHardwareInfo()
            repository.prepareAndWriteConfigs(current.settings, hw)
            val zip = repository.createBundleZip()
            onZipReady(zip)
        }
    }

    fun clearLogs() {
        viewModelScope.launch {
            repository.clearLogs()
        }
    }
}
