package com.example.model

enum class GamePreset(
    val displayNameAr: String,
    val executableName: String,
    val iniFileName: String,
    val subtitleAr: String,
) {
    RED_ALERT_2(
        displayNameAr = "Red Alert 2 (الأصلية)",
        executableName = "RA2.EXE",
        iniFileName = "RA2.INI",
        subtitleAr = "الإصدار الكلاسيكي v1.006 — دعم cnc-ddraw و 60 FPS",
    ),
    YURIS_REVENGE(
        displayNameAr = "Yuri's Revenge (التوسعة)",
        executableName = "YURI.EXE",
        iniFileName = "RA2MD.INI",
        subtitleAr = "توسعة انتقام يوري — دعم Ares/Phobos والمودات",
    ),
    CUSTOM_WIN32(
        displayNameAr = "لعبة ويندوز مخصصة (.EXE)",
        executableName = "Game.exe",
        iniFileName = "win32_profile.ini",
        subtitleAr = "تشغيل أي لعبة ويندوز 32/64-bit عبر طبقة ARM64",
    ),
}

enum class Arm64BridgeMode(
    val titleAr: String,
    val codeName: String,
    val detailAr: String,
) {
    ARM64_WOW64_BOX64(
        titleAr = "ARM64 WoW64 + Box64 Dynarec",
        codeName = "ARM64_WOW64",
        detailAr = "مُوصى به لألعاب DirectDraw/x86 مثل Red Alert 2 مع مترجم Dynarec السريع.",
    ),
    ARM64_FEX_CORE(
        titleAr = "ARM64 FEX-Emu + Wine 9",
        codeName = "ARM64_FEX",
        detailAr = "ترجمة تعليمات x86 إلى ARM64-v8a مباشرة مع تقليل عنق الزجاجة.",
    ),
}

enum class GraphicsRenderer(
    val titleAr: String,
    val ddrawValue: String,
    val dxvkEnv: String,
    val detailAr: String,
) {
    CNC_DDRAW_OPENGL(
        titleAr = "OpenGL (cnc-ddraw المحسّن)",
        ddrawValue = "opengl",
        dxvkEnv = "0",
        detailAr = "أعلى استقرار لـ Red Alert 2 على أندرويد بسرعة 60 إطار ثابتة.",
    ),
    VULKAN_DXVK(
        titleAr = "Vulkan + DXVK ARM64",
        ddrawValue = "opengl",
        dxvkEnv = "1",
        detailAr = "تحويل DirectX إلى Vulkan لأجهزة المعالجات الحديثة Adreno/Mali.",
    ),
    DIRECT3D9(
        titleAr = "Direct3D 9 Hardware",
        ddrawValue = "direct3d9",
        dxvkEnv = "0",
        detailAr = "تسريع عتادي بديل للشاشات عالية التحديث.",
    ),
    SOFTWARE_GDI(
        titleAr = "GDI التوافق الكامل",
        ddrawValue = "gdi",
        dxvkEnv = "0",
        detailAr = "وضع الأمان في حال حدوث وميض في القوائم.",
    ),
}

enum class ScreenFillMode(
    val titleAr: String,
    val maintainAspect: Boolean,
    val aspectLabel: String,
) {
    FILL_MOBILE_SCREEN(
        titleAr = "ملء شاشة الجوال بالكامل",
        maintainAspect = false,
        aspectLabel = "Stretch Fullscreen",
    ),
    WIDESCREEN_16_9(
        titleAr = "شاشة عريضة 16:9",
        maintainAspect = true,
        aspectLabel = "16:9 HD",
    ),
    CLASSIC_4_3(
        titleAr = "الأبعاد الكلاسيكية 4:3",
        maintainAspect = true,
        aspectLabel = "4:3 Original",
    ),
}

enum class ShaderFilter(val titleAr: String, val configValue: String) {
    BILINEAR("تنعيم ثنائي (Bilinear)", "Shaders\\bilinear.glsl"),
    XBRZ_SHARP("رفع حدة البيكسل (xBRZ)", "Shaders\\xbrz-freescale.glsl"),
    NEAREST("حاد كلاسيكي (Nearest)", "Shaders\\nearest.glsl"),
}

enum class TouchControlMode(val titleAr: String, val subtitleAr: String) {
    DIRECT_TOUCH("اللمس المباشر على الخريطة", "النقر يحدد الوحدات والسحب يرسم مربع التحديد"),
    VIRTUAL_TRACKPAD("مؤشر فأرة تكتيكي", "تحكم دقيق مثل فأرة الكمبيوتر مع أزرار النقر الأيمن والأيسر"),
}

enum class FileCategory(val labelAr: String) {
    EXECUTABLE("ملف تشغيل EXE"),
    MIX_DATA("بيانات MIX أصلية"),
    DLL("مكتبة نظام DLL"),
    CONFIG("ملف إعدادات INI"),
    OTHER("ملف داعم"),
}

enum class ModType(val labelAr: String) {
    MIX_MOD("حزمة مود MIX"),
    INI_PATCH("تعديل قواعد INI"),
    MAP_PACK("خرائط مخصصة"),
    SHADER_MOD("مُحسّن رسوميات"),
    ARCHIVE("أرشيف مود ZIP/7z"),
}

data class EngineSettings(
    val selectedPreset: GamePreset = GamePreset.RED_ALERT_2,
    val arm64BridgeMode: Arm64BridgeMode = Arm64BridgeMode.ARM64_WOW64_BOX64,
    val targetFps: Int = 60,
    val enableVSync: Boolean = true,
    val graphicsRenderer: GraphicsRenderer = GraphicsRenderer.CNC_DDRAW_OPENGL,
    val screenFillMode: ScreenFillMode = ScreenFillMode.FILL_MOBILE_SCREEN,
    val shaderFilter: ShaderFilter = ShaderFilter.BILINEAR,
    val renderScalePercent: Int = 100,
    val enableSingleCpuFix: Boolean = true,
    val disableVideoBackBuffer: Boolean = true,
    val enableDynarecFastMath: Boolean = true,
    val enableEsync: Boolean = true,
    val enableEdgeToEdgeCutout: Boolean = true,
    val touchControlMode: TouchControlMode = TouchControlMode.DIRECT_TOUCH,
    val showTouchHudButtons: Boolean = true,
    val customResolutionWidth: Int = 0, // 0 = Auto match phone display
    val customResolutionHeight: Int = 0,
)

data class DeviceHardwareInfo(
    val deviceModel: String,
    val primaryAbi: String,
    val supportedAbis: List<String>,
    val isArm64Supported: Boolean,
    val cpuCores: Int,
    val totalRamMb: Long,
    val availableRamMb: Long,
    val openGlEsVersion: String,
    val screenWidthPx: Int,
    val screenHeightPx: Int,
    val displayRefreshHz: Float,
    val supports60FpsOrHigher: Boolean,
)

data class ExternalRunnerApp(
    val packageName: String,
    val displayName: String,
    val isInstalled: Boolean,
    val descriptionAr: String,
)
