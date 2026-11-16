package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.GameContainerManager
import com.example.model.DeviceHardwareInfo
import com.example.model.EngineSettings
import com.example.model.ScreenFillMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read app name string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("مشغل RA2", appName)
    }

    @Test
    fun `generated ini and arm64 launch script contain 60fps fullscreen and arm64 flags`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val manager = GameContainerManager(context)
        val settings = EngineSettings(
            targetFps = 60,
            screenFillMode = ScreenFillMode.FILL_MOBILE_SCREEN,
            enableSingleCpuFix = true,
            disableVideoBackBuffer = true,
            enableDynarecFastMath = true,
        )
        val hw = DeviceHardwareInfo(
            deviceModel = "Test ARM64 Device",
            primaryAbi = "arm64-v8a",
            supportedAbis = listOf("arm64-v8a"),
            isArm64Supported = true,
            cpuCores = 8,
            totalRamMb = 8192,
            availableRamMb = 4096,
            openGlEsVersion = "3.2",
            screenWidthPx = 2400,
            screenHeightPx = 1080,
            displayRefreshHz = 60f,
            supports60FpsOrHigher = true,
        )

        val ra2Ini = manager.buildRa2Ini(settings, hw)
        val ddrawIni = manager.buildDdrawIni(settings, hw)
        val launchSh = manager.buildArm64LaunchScript(settings, hw)

        assertTrue(ra2Ini.contains("VideoBackBuffer=no"))
        assertTrue(ra2Ini.contains("StretchMovies=yes"))
        assertTrue(ddrawIni.contains("maxfps=60"))
        assertTrue(ddrawIni.contains("fullscreen=true"))
        assertTrue(ddrawIni.contains("maintas=false"))
        assertTrue(launchSh.contains("BOX64_DYNAREC=1"))
        assertTrue(launchSh.contains("RA2.EXE"))
    }
}
