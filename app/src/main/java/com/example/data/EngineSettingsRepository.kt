package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.Arm64BridgeMode
import com.example.model.EngineSettings
import com.example.model.GamePreset
import com.example.model.GraphicsRenderer
import com.example.model.ScreenFillMode
import com.example.model.ShaderFilter
import com.example.model.TouchControlMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.engineDataStore: DataStore<Preferences> by preferencesDataStore(name = "ra2_engine_prefs")

class EngineSettingsRepository(private val context: Context) {

    private object Keys {
        val PRESET = stringPreferencesKey("selected_preset")
        val ARM64_MODE = stringPreferencesKey("arm64_bridge_mode")
        val TARGET_FPS = intPreferencesKey("target_fps")
        val ENABLE_VSYNC = booleanPreferencesKey("enable_vsync")
        val RENDERER = stringPreferencesKey("graphics_renderer")
        val SCREEN_FILL = stringPreferencesKey("screen_fill_mode")
        val SHADER = stringPreferencesKey("shader_filter")
        val RENDER_SCALE = intPreferencesKey("render_scale_percent")
        val SINGLE_CPU_FIX = booleanPreferencesKey("single_cpu_fix")
        val DISABLE_VIDEO_BACKBUFFER = booleanPreferencesKey("disable_video_backbuffer")
        val DYNAREC_FAST_MATH = booleanPreferencesKey("dynarec_fast_math")
        val ENABLE_ESYNC = booleanPreferencesKey("enable_esync")
        val EDGE_TO_EDGE_CUTOUT = booleanPreferencesKey("edge_to_edge_cutout")
        val TOUCH_MODE = stringPreferencesKey("touch_control_mode")
        val SHOW_TOUCH_HUD = booleanPreferencesKey("show_touch_hud")
    }

    val settingsFlow: Flow<EngineSettings> = context.engineDataStore.data.map { prefs ->
        EngineSettings(
            selectedPreset = enumOrDefault(prefs[Keys.PRESET], GamePreset.RED_ALERT_2),
            arm64BridgeMode = enumOrDefault(prefs[Keys.ARM64_MODE], Arm64BridgeMode.ARM64_WOW64_BOX64),
            targetFps = prefs[Keys.TARGET_FPS] ?: 60,
            enableVSync = prefs[Keys.ENABLE_VSYNC] ?: true,
            graphicsRenderer = enumOrDefault(prefs[Keys.RENDERER], GraphicsRenderer.CNC_DDRAW_OPENGL),
            screenFillMode = enumOrDefault(prefs[Keys.SCREEN_FILL], ScreenFillMode.FILL_MOBILE_SCREEN),
            shaderFilter = enumOrDefault(prefs[Keys.SHADER], ShaderFilter.BILINEAR),
            renderScalePercent = (prefs[Keys.RENDER_SCALE] ?: 100).coerceIn(50, 100),
            enableSingleCpuFix = prefs[Keys.SINGLE_CPU_FIX] ?: true,
            disableVideoBackBuffer = prefs[Keys.DISABLE_VIDEO_BACKBUFFER] ?: true,
            enableDynarecFastMath = prefs[Keys.DYNAREC_FAST_MATH] ?: true,
            enableEsync = prefs[Keys.ENABLE_ESYNC] ?: true,
            enableEdgeToEdgeCutout = prefs[Keys.EDGE_TO_EDGE_CUTOUT] ?: true,
            touchControlMode = enumOrDefault(prefs[Keys.TOUCH_MODE], TouchControlMode.DIRECT_TOUCH),
            showTouchHudButtons = prefs[Keys.SHOW_TOUCH_HUD] ?: true,
        )
    }

    suspend fun update(transform: (EngineSettings) -> EngineSettings) {
        context.engineDataStore.edit { prefs ->
            val current = EngineSettings(
                selectedPreset = enumOrDefault(prefs[Keys.PRESET], GamePreset.RED_ALERT_2),
                arm64BridgeMode = enumOrDefault(prefs[Keys.ARM64_MODE], Arm64BridgeMode.ARM64_WOW64_BOX64),
                targetFps = prefs[Keys.TARGET_FPS] ?: 60,
                enableVSync = prefs[Keys.ENABLE_VSYNC] ?: true,
                graphicsRenderer = enumOrDefault(prefs[Keys.RENDERER], GraphicsRenderer.CNC_DDRAW_OPENGL),
                screenFillMode = enumOrDefault(prefs[Keys.SCREEN_FILL], ScreenFillMode.FILL_MOBILE_SCREEN),
                shaderFilter = enumOrDefault(prefs[Keys.SHADER], ShaderFilter.BILINEAR),
                renderScalePercent = (prefs[Keys.RENDER_SCALE] ?: 100).coerceIn(50, 100),
                enableSingleCpuFix = prefs[Keys.SINGLE_CPU_FIX] ?: true,
                disableVideoBackBuffer = prefs[Keys.DISABLE_VIDEO_BACKBUFFER] ?: true,
                enableDynarecFastMath = prefs[Keys.DYNAREC_FAST_MATH] ?: true,
                enableEsync = prefs[Keys.ENABLE_ESYNC] ?: true,
                enableEdgeToEdgeCutout = prefs[Keys.EDGE_TO_EDGE_CUTOUT] ?: true,
                touchControlMode = enumOrDefault(prefs[Keys.TOUCH_MODE], TouchControlMode.DIRECT_TOUCH),
                showTouchHudButtons = prefs[Keys.SHOW_TOUCH_HUD] ?: true,
            )
            val next = transform(current)
            prefs[Keys.PRESET] = next.selectedPreset.name
            prefs[Keys.ARM64_MODE] = next.arm64BridgeMode.name
            prefs[Keys.TARGET_FPS] = next.targetFps
            prefs[Keys.ENABLE_VSYNC] = next.enableVSync
            prefs[Keys.RENDERER] = next.graphicsRenderer.name
            prefs[Keys.SCREEN_FILL] = next.screenFillMode.name
            prefs[Keys.SHADER] = next.shaderFilter.name
            prefs[Keys.RENDER_SCALE] = next.renderScalePercent
            prefs[Keys.SINGLE_CPU_FIX] = next.enableSingleCpuFix
            prefs[Keys.DISABLE_VIDEO_BACKBUFFER] = next.disableVideoBackBuffer
            prefs[Keys.DYNAREC_FAST_MATH] = next.enableDynarecFastMath
            prefs[Keys.ENABLE_ESYNC] = next.enableEsync
            prefs[Keys.EDGE_TO_EDGE_CUTOUT] = next.enableEdgeToEdgeCutout
            prefs[Keys.TOUCH_MODE] = next.touchControlMode.name
            prefs[Keys.SHOW_TOUCH_HUD] = next.showTouchHudButtons
        }
    }

    private inline fun <reified T : Enum<T>> enumOrDefault(raw: String?, fallback: T): T {
        if (raw == null) return fallback
        return runCatching { enumValueOf<T>(raw) }.getOrDefault(fallback)
    }
}
