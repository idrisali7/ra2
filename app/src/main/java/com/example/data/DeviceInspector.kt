package com.example.data

import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.display.DisplayManager
import android.os.Build
import android.view.Display
import android.view.WindowManager
import com.example.model.DeviceHardwareInfo
import com.example.model.ExternalRunnerApp

class DeviceInspector(private val context: Context) {

    fun inspectHardware(): DeviceHardwareInfo {
        val supportedAbis = Build.SUPPORTED_ABIS.toList()
        val isArm64 = Build.SUPPORTED_64_BIT_ABIS.any { it.contains("arm64", ignoreCase = true) } ||
            supportedAbis.any { it.contains("arm64", ignoreCase = true) }

        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memInfo)

        val totalRamMb = memInfo.totalMem / (1024L * 1024L)
        val availRamMb = memInfo.availMem / (1024L * 1024L)
        val glEsVersion = activityManager.deviceConfigurationInfo.glEsVersion ?: "3.0"

        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val (widthPx, heightPx) = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val bounds = windowManager.currentWindowMetrics.bounds
            bounds.width() to bounds.height()
        } else {
            val metrics = context.resources.displayMetrics
            metrics.widthPixels to metrics.heightPixels
        }

        val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        val defaultDisplay = displayManager.getDisplay(Display.DEFAULT_DISPLAY)
        val refreshRate = defaultDisplay?.refreshRate ?: 60f

        val model = listOf(Build.MANUFACTURER, Build.MODEL)
            .filter { it.isNotBlank() }
            .joinToString(" ")

        return DeviceHardwareInfo(
            deviceModel = model,
            primaryAbi = supportedAbis.firstOrNull() ?: "arm64-v8a",
            supportedAbis = supportedAbis,
            isArm64Supported = isArm64,
            cpuCores = Runtime.getRuntime().availableProcessors().coerceAtLeast(1),
            totalRamMb = totalRamMb,
            availableRamMb = availRamMb,
            openGlEsVersion = glEsVersion,
            screenWidthPx = maxOf(widthPx, heightPx),
            screenHeightPx = minOf(widthPx, heightPx),
            displayRefreshHz = refreshRate,
            supports60FpsOrHigher = refreshRate >= 59.5f,
        )
    }

    fun detectExternalRunners(): List<ExternalRunnerApp> {
        val candidates = listOf(
            Triple("com.winlator", "Winlator (ARM64 Wine/Box64)", "تشغيل مباشر لحاويات Winlator المثبتة على جهازك"),
            Triple("com.winlator.cmod", "Winlator CMod / Frost", "نسخة Winlator المعدّلة بدعم Bionic ARM64"),
            Triple("com.termux.x11", "Mobox / Termux-X11", "بيئة تشغيل Box64 + Wine عبر Termux X11"),
            Triple("com.eltechs.ed", "ExaGear Windows Emulator", "محاكي Windows الكلاسيكي لألعاب DirectDraw"),
            Triple("com.horizon.emu", "Horizon Emu", "بيئة Wine/Box64 لأجهزة ARM64"),
        )
        val pm = context.packageManager
        return candidates.map { (pkg, name, desc) ->
            val installed = runCatching {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    pm.getPackageInfo(pkg, PackageManager.PackageInfoFlags.of(0))
                } else {
                    @Suppress("DEPRECATION")
                    pm.getPackageInfo(pkg, 0)
                }
                true
            }.getOrDefault(false)
            ExternalRunnerApp(
                packageName = pkg,
                displayName = name,
                isInstalled = installed,
                descriptionAr = desc,
            )
        }
    }
}
