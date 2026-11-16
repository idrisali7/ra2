package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.Arm64BridgeMode
import com.example.model.GraphicsRenderer
import com.example.model.ScreenFillMode
import com.example.model.ShaderFilter
import com.example.model.TouchControlMode
import com.example.ui.LauncherUiState
import com.example.ui.components.SectionHeader
import com.example.ui.components.SettingSwitchRow
import com.example.ui.components.StatusPill
import com.example.ui.components.TacticalCard
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.CommandBorder
import com.example.ui.theme.CommandObsidian
import com.example.ui.theme.CommandSurfaceHigh
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.RadarCyan
import com.example.ui.theme.TacticalGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.roundToInt

@Composable
fun GraphicsArm64Screen(
    state: LauncherUiState,
    onSetTargetFps: (Int) -> Unit,
    onSetVSync: (Boolean) -> Unit,
    onSelectRenderer: (GraphicsRenderer) -> Unit,
    onSelectScreenFillMode: (ScreenFillMode) -> Unit,
    onSelectShaderFilter: (ShaderFilter) -> Unit,
    onSetRenderScale: (Int) -> Unit,
    onSetSingleCpuFix: (Boolean) -> Unit,
    onSetDisableVideoBackBuffer: (Boolean) -> Unit,
    onSetEdgeToEdgeCutout: (Boolean) -> Unit,
    onSelectArm64Bridge: (Arm64BridgeMode) -> Unit,
    onSetDynarecFastMath: (Boolean) -> Unit,
    onSetEsync: (Boolean) -> Unit,
    onSelectTouchMode: (TouchControlMode) -> Unit,
    onSetShowTouchHud: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val settings = state.settings
    val hw = state.hardware
    var previewTab by rememberSaveable { mutableIntStateOf(0) }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 760.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                // 60 FPS & Smoothness Section
                TacticalCard(accentColor = TacticalGreen) {
                    SectionHeader(
                        title = "معدل الإطارات وسلاسة التشغيل (60 FPS)",
                        subtitle = "تردد شاشة جهازك الفعلي: ${hw?.displayRefreshHz?.roundToInt() ?: 60} Hz",
                        icon = Icons.Filled.Speed,
                        iconTint = TacticalGreen,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        listOf(30, 60, 120).forEach { fps ->
                            val selected = settings.targetFps == fps
                            FilterChip(
                                selected = selected,
                                onClick = { onSetTargetFps(fps) },
                                label = {
                                    Text(
                                        text = if (fps == 60) "60 FPS (مثالي)" else "$fps FPS",
                                        style = MaterialTheme.typography.labelLarge,
                                    )
                                },
                                modifier = Modifier.testTag("fps_chip_$fps"),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = TacticalGreen.copy(alpha = 0.2f),
                                    selectedLabelColor = TacticalGreen,
                                ),
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    SettingSwitchRow(
                        title = "تفعيل المزامنة العمودية (VSync)",
                        description = "يمنع تمزق الإطارات ويضمن سلاسة حركة الوحدات والخريطة",
                        checked = settings.enableVSync,
                        onCheckedChange = onSetVSync,
                        testTag = "switch_vsync",
                    )
                }
            }

            item {
                // Graphics Optimization Section
                TacticalCard(accentColor = AmberSecondary) {
                    SectionHeader(
                        title = "تحسين أداء الرسوميات (RA2 + DirectDraw)",
                        subtitle = "إعدادات cnc-ddraw ومحرك العرض لضمان عدم التقطيع",
                        icon = Icons.Filled.Tune,
                        iconTint = AmberSecondary,
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        GraphicsRenderer.entries.forEach { renderer ->
                            val selected = settings.graphicsRenderer == renderer
                            Surface(
                                onClick = { onSelectRenderer(renderer) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("renderer_${renderer.name}"),
                                shape = RoundedCornerShape(12.dp),
                                color = if (selected) AmberSecondary.copy(alpha = 0.14f) else CommandSurfaceHigh,
                                border = BorderStroke(
                                    1.dp,
                                    if (selected) AmberSecondary else CommandBorder,
                                ),
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = renderer.titleAr,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = if (selected) AmberSecondary else TextPrimary,
                                    )
                                    Text(
                                        text = renderer.detailAr,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextSecondary,
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "فلتر تحسين الصورة: ${settings.shaderFilter.titleAr}",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextPrimary,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        ShaderFilter.entries.forEach { shader ->
                            FilterChip(
                                selected = settings.shaderFilter == shader,
                                onClick = { onSelectShaderFilter(shader) },
                                label = { Text(shader.titleAr) },
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "دقة التصيير الداخلية: ${settings.renderScalePercent}%",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextPrimary,
                    )
                    Slider(
                        value = settings.renderScalePercent.toFloat(),
                        onValueChange = { onSetRenderScale(it.roundToInt()) },
                        valueRange = 50f..100f,
                        steps = 4,
                        colors = SliderDefaults.colors(
                            thumbColor = AmberSecondary,
                            activeTrackColor = AmberSecondary,
                        ),
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    SettingSwitchRow(
                        title = "إصلاح تعدد الأنوية لـ Red Alert 2 (SingleCPU)",
                        description = "يمنع تجمد القوائم وانهيار اللعبة على معالجات أندرويد الحديثة",
                        checked = settings.enableSingleCpuFix,
                        onCheckedChange = onSetSingleCpuFix,
                        testTag = "switch_single_cpu",
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    SettingSwitchRow(
                        title = "تعطيل VideoBackBuffer في RA2.INI",
                        description = "أهم خيار لرفع سرعة الإطارات ومنع البطء في المعارك الكبيرة",
                        checked = settings.disableVideoBackBuffer,
                        onCheckedChange = onSetDisableVideoBackBuffer,
                        testTag = "switch_backbuffer",
                    )
                }
            }

            item {
                // Mobile Fullscreen Section
                TacticalCard(accentColor = CrimsonPrimary) {
                    SectionHeader(
                        title = "ملء شاشة الجوال (Fullscreen Fill)",
                        subtitle = "ضبط أبعاد العرض لتملأ شاشة الهاتف بالكامل بدون حواف سوداء",
                        icon = Icons.Filled.Fullscreen,
                        iconTint = CrimsonPrimary,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ScreenFillMode.entries.forEach { mode ->
                            val selected = settings.screenFillMode == mode
                            Surface(
                                onClick = { onSelectScreenFillMode(mode) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("screen_fill_${mode.name}"),
                                shape = RoundedCornerShape(12.dp),
                                color = if (selected) CrimsonPrimary.copy(alpha = 0.16f) else CommandSurfaceHigh,
                                border = BorderStroke(1.dp, if (selected) CrimsonPrimary else CommandBorder),
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text(
                                        text = mode.titleAr,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = TextPrimary,
                                    )
                                    StatusPill(
                                        label = mode.aspectLabel,
                                        color = if (selected) CrimsonPrimary else TextSecondary,
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    SettingSwitchRow(
                        title = "تمديد العرض حتى حافة الكاميرا الأمامية",
                        description = "تفعيل العرض الكامل عبر مناطق القطع (Display Cutout)",
                        checked = settings.enableEdgeToEdgeCutout,
                        onCheckedChange = onSetEdgeToEdgeCutout,
                        testTag = "switch_cutout_fill",
                    )
                }
            }

            item {
                // ARM64 Architecture Bridge Section
                TacticalCard(accentColor = RadarCyan) {
                    SectionHeader(
                        title = "بنية التشغيل ARM64 ومُسرّع WoW64",
                        subtitle = "فحص مباشر لعتاد الهاتف وإعدادات مترجم تعليمات الويندوز",
                        icon = Icons.Filled.Memory,
                        iconTint = RadarCyan,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    if (hw != null) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = CommandObsidian,
                            border = BorderStroke(1.dp, RadarCyan.copy(alpha = 0.35f)),
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Text(
                                    text = "الجهاز: ${hw.deviceModel}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = RadarCyan,
                                )
                                Text(
                                    text = "ABI: ${hw.supportedAbis.joinToString(", ")} | Cores: ${hw.cpuCores}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = TextPrimary,
                                )
                                Text(
                                    text = "RAM: ${hw.availableRamMb}/${hw.totalRamMb} MB | OpenGL ES ${hw.openGlEsVersion} | ${hw.screenWidthPx}x${hw.screenHeightPx}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = TextSecondary,
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Arm64BridgeMode.entries.forEach { bridge ->
                            val selected = settings.arm64BridgeMode == bridge
                            Surface(
                                onClick = { onSelectArm64Bridge(bridge) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("arm64_${bridge.name}"),
                                shape = RoundedCornerShape(12.dp),
                                color = if (selected) RadarCyan.copy(alpha = 0.14f) else CommandSurfaceHigh,
                                border = BorderStroke(1.dp, if (selected) RadarCyan else CommandBorder),
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = bridge.titleAr,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = if (selected) RadarCyan else TextPrimary,
                                    )
                                    Text(
                                        text = bridge.detailAr,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextSecondary,
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    SettingSwitchRow(
                        title = "BOX64_DYNAREC_FASTNAN & FASTROUND",
                        description = "تسريع حسابات الفاصلة العائمة على أنوية ARM64 بنسبة تصل إلى 35%",
                        checked = settings.enableDynarecFastMath,
                        onCheckedChange = onSetDynarecFastMath,
                        testTag = "switch_dynarec_fastmath",
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    SettingSwitchRow(
                        title = "تفعيل مزامنة Wine ESYNC",
                        description = "تقليل تأخير خيوط المعالج عند تشغيل محركات الألعاب الكلاسيكية",
                        checked = settings.enableEsync,
                        onCheckedChange = onSetEsync,
                        testTag = "switch_esync",
                    )
                }
            }

            item {
                // Touch Control Mode Preferences
                TacticalCard {
                    SectionHeader(
                        title = "وضع اللعب باللمس (Touch Controls)",
                        subtitle = "تخصيص طريقة التحديد وإعطاء الأوامر للوحدات",
                        icon = Icons.Filled.TouchApp,
                        iconTint = TacticalGreen,
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        TouchControlMode.entries.forEach { mode ->
                            val selected = settings.touchControlMode == mode
                            Surface(
                                onClick = { onSelectTouchMode(mode) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = if (selected) TacticalGreen.copy(alpha = 0.14f) else CommandSurfaceHigh,
                                border = BorderStroke(1.dp, if (selected) TacticalGreen else CommandBorder),
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = mode.titleAr,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = if (selected) TacticalGreen else TextPrimary,
                                    )
                                    Text(
                                        text = mode.subtitleAr,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextSecondary,
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    SettingSwitchRow(
                        title = "إظهار أزرار الأوامر السريعة على الشاشة",
                        description = "أزرار نشر الوحدات (D) والهجوم (A) والمجموعات (1-3)",
                        checked = settings.showTouchHudButtons,
                        onCheckedChange = onSetShowTouchHud,
                        testTag = "switch_touch_hud",
                    )
                }
            }

            item {
                // Live Generated INI & Script Preview
                val configs = state.generatedConfigs
                if (configs != null) {
                    TacticalCard {
                        SectionHeader(
                            title = "معاينة ملفات التشغيل المولّدة",
                            subtitle = "تُكتب تلقائياً داخل مجلد اللعبة قبل الإطلاق",
                            icon = Icons.Filled.Code,
                            iconTint = TextPrimary,
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(configs.iniFileName, "ddraw.ini", "arm64_launch.sh").forEachIndexed { idx, label ->
                                FilterChip(
                                    selected = previewTab == idx,
                                    onClick = { previewTab = idx },
                                    label = { Text(label) },
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        val content = when (previewTab) {
                            0 -> configs.ra2IniContent
                            1 -> configs.ddrawIniContent
                            else -> configs.launchScriptContent
                        }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = CommandObsidian,
                            border = BorderStroke(1.dp, CommandBorder),
                        ) {
                            Text(
                                text = content,
                                fontFamily = JetBrainsMonoFontFamily,
                                style = MaterialTheme.typography.labelMedium,
                                color = TacticalGreen,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                                    .padding(12.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}
