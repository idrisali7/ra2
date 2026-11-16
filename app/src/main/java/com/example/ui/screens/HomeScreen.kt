package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.Ra2Repository
import com.example.model.GamePreset
import com.example.ui.LauncherUiState
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatusPill
import com.example.ui.components.TacticalCard
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.CommandBorder
import com.example.ui.theme.CommandObsidian
import com.example.ui.theme.CommandSurfaceHigh
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.RadarCyan
import com.example.ui.theme.TacticalGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun HomeScreen(
    state: LauncherUiState,
    onSelectPreset: (GamePreset) -> Unit,
    onLaunchTouchPlay: () -> Unit,
    onApplyMaxSmoothness: () -> Unit,
    onNavigateToFiles: () -> Unit,
    onNavigateToGraphics: () -> Unit,
    onLaunchExternalRunner: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val installedRunner = state.externalRunners.firstOrNull { it.isInstalled }
    val hardware = state.hardware

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
                // Hero Tactical Command Banner
                Surface(
                    shape = MaterialTheme.shapes.large,
                    border = BorderStroke(1.dp, CrimsonPrimary.copy(alpha = 0.45f)),
                    color = CommandSurfaceHigh,
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(215.dp),
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_ra2_command_banner),
                            contentDescription = stringResource(id = R.string.hero_banner_desc),
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            CommandObsidian.copy(alpha = 0.25f),
                                            CommandObsidian.copy(alpha = 0.85f),
                                            CommandObsidian.copy(alpha = 0.96f),
                                        ),
                                    ),
                                ),
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                StatusPill(
                                    label = if (hardware?.isArm64Supported == true) "ARM64-v8a" else "ARM",
                                    color = RadarCyan,
                                    icon = Icons.Filled.Memory,
                                )
                                StatusPill(
                                    label = "${state.settings.targetFps} FPS",
                                    color = TacticalGreen,
                                    icon = Icons.Filled.Speed,
                                )
                                StatusPill(
                                    label = "ملء الشاشة",
                                    color = AmberSecondary,
                                    icon = Icons.Filled.Fullscreen,
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.img_launcher_emblem),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(RoundedCornerShape(14.dp)),
                                    contentScale = ContentScale.Crop,
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "مشغل ويندوز & Red Alert 2",
                                        style = MaterialTheme.typography.headlineSmall,
                                        color = TextPrimary,
                                    )
                                    Text(
                                        text = state.settings.selectedPreset.subtitleAr,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextSecondary,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                // Primary Launch CTA + Quick Actions
                TacticalCard(accentColor = CrimsonPrimary) {
                    Button(
                        onClick = onLaunchTouchPlay,
                        enabled = !state.isPreparingLaunch,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("launch_touch_play_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CrimsonPrimary,
                            contentColor = Color.White,
                        ),
                    ) {
                        if (state.isPreparingLaunch) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.5.dp,
                                color = Color.White,
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("جارٍ تجهيز حاوية ARM64...")
                        } else {
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "تشغيل بوضع اللمس 60 FPS (ملء الشاشة)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        OutlinedButton(
                            onClick = onApplyMaxSmoothness,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("max_smoothness_button"),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, AmberSecondary.copy(alpha = 0.55f)),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Bolt,
                                contentDescription = null,
                                tint = AmberSecondary,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "تحسين السلاسة تلقائياً",
                                style = MaterialTheme.typography.labelLarge,
                                color = AmberSecondary,
                            )
                        }

                        if (installedRunner != null) {
                            OutlinedButton(
                                onClick = { onLaunchExternalRunner(installedRunner.packageName) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("launch_external_runner_button"),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, RadarCyan.copy(alpha = 0.55f)),
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.SportsEsports,
                                    contentDescription = null,
                                    tint = RadarCyan,
                                    modifier = Modifier.size(18.dp),
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "فتح Winlator",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = RadarCyan,
                                )
                            }
                        }
                    }
                }
            }

            item {
                // Game Preset Selector
                TacticalCard {
                    SectionHeader(
                        title = "إصدار اللعبة المستهدف",
                        subtitle = "اختر ملف التشغيل وملف الإعدادات التلقائي",
                        icon = Icons.Filled.SportsEsports,
                        iconTint = AmberSecondary,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        GamePreset.entries.forEach { preset ->
                            val selected = state.settings.selectedPreset == preset
                            FilterChip(
                                selected = selected,
                                onClick = { onSelectPreset(preset) },
                                label = {
                                    Text(
                                        text = when (preset) {
                                            GamePreset.RED_ALERT_2 -> "Red Alert 2"
                                            GamePreset.YURIS_REVENGE -> "Yuri's Revenge"
                                            GamePreset.CUSTOM_WIN32 -> "Windows EXE"
                                        },
                                        style = MaterialTheme.typography.labelLarge,
                                    )
                                },
                                modifier = Modifier.testTag("preset_${preset.name}"),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CrimsonPrimary.copy(alpha = 0.22f),
                                    selectedLabelColor = CrimsonPrimary,
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = selected,
                                    borderColor = CommandBorder,
                                    selectedBorderColor = CrimsonPrimary,
                                ),
                            )
                        }
                    }
                }
            }

            item {
                // Readiness Checklist
                TacticalCard {
                    SectionHeader(
                        title = "حالة جاهزية الحاوية والملفات",
                        subtitle = "اضغط على أي عنصر لإدارته مباشرة",
                        icon = Icons.Filled.CheckCircle,
                        iconTint = TacticalGreen,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ReadinessRow(
                            icon = Icons.Filled.FolderZip,
                            title = "ملفات اللعبة الأصلية (${state.gameFiles.size} ملف)",
                            subtitle = if (state.hasExecutable && state.hasCoreMix) {
                                "تم استيراد ملفات التشغيل والبيانات الأساسية بنجاح"
                            } else if (state.gameFiles.isNotEmpty()) {
                                "حجم الحاوية: ${Ra2Repository.formatBytes(state.totalContainerBytes)} — تأكد من وجود RA2.EXE و ra2.mix"
                            } else {
                                "اضغط لاستيراد ملفات RA2.EXE و ra2.mix من جهازك"
                            },
                            isReady = state.hasExecutable && state.hasCoreMix,
                            onClick = onNavigateToFiles,
                            testTag = "readiness_original_files",
                        )
                        ReadinessRow(
                            icon = Icons.Filled.Extension,
                            title = "ملفات المودات (${state.activeModsCount} مُفعّل من ${state.mods.size})",
                            subtitle = if (state.activeModsCount > 0) {
                                "المودات النشطة مدمجة تلقائياً في مسار التشغيل"
                            } else {
                                "أضف ملفات .mix أو .ini أو .zip لتعديل اللعبة"
                            },
                            isReady = state.activeModsCount > 0,
                            onClick = onNavigateToFiles,
                            testTag = "readiness_mods",
                        )
                        ReadinessRow(
                            icon = Icons.Filled.Speed,
                            title = "أداء الرسوميات و ${state.settings.targetFps} FPS",
                            subtitle = "${state.settings.graphicsRenderer.titleAr} • ${state.settings.screenFillMode.titleAr}",
                            isReady = true,
                            onClick = onNavigateToGraphics,
                            testTag = "readiness_graphics",
                        )
                        ReadinessRow(
                            icon = Icons.Filled.Memory,
                            title = "بنية المعالج: ${hardware?.primaryAbi ?: "arm64-v8a"}",
                            subtitle = "${state.settings.arm64BridgeMode.titleAr} • RAM ${hardware?.totalRamMb ?: 0} MB",
                            isReady = hardware?.isArm64Supported == true,
                            onClick = onNavigateToGraphics,
                            testTag = "readiness_arm64",
                        )
                        ReadinessRow(
                            icon = Icons.Filled.TouchApp,
                            title = "التحكم باللمس الشاشة الكاملة",
                            subtitle = state.settings.touchControlMode.titleAr,
                            isReady = true,
                            onClick = onLaunchTouchPlay,
                            testTag = "readiness_touch",
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReadinessRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isReady: Boolean,
    onClick: () -> Unit,
    testTag: String,
) {
    val badgeColor = if (isReady) TacticalGreen else AmberSecondary
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag),
        shape = RoundedCornerShape(12.dp),
        color = CommandSurfaceHigh.copy(alpha = 0.65f),
        border = BorderStroke(1.dp, CommandBorder),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = badgeColor,
                modifier = Modifier.size(22.dp),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                )
            }
            Icon(
                imageVector = if (isReady) Icons.Filled.CheckCircle else Icons.Outlined.Info,
                contentDescription = if (isReady) "جاهز" else "يتطلب إجراء",
                tint = badgeColor,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}
