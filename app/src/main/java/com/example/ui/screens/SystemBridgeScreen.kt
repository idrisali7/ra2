package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.LaunchLogEntity
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SystemBridgeScreen(
    state: LauncherUiState,
    onRefreshRunners: () -> Unit,
    onLaunchExternalRunner: (String) -> Unit,
    onShareConfigBundle: () -> Unit,
    onClearLogs: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val iniFileName = state.generatedConfigs?.iniFileName ?: "RA2.INI"

    val saveRa2IniLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/plain"),
    ) { uri ->
        val text = state.generatedConfigs?.ra2IniContent ?: return@rememberLauncherForActivityResult
        if (uri != null) {
            runCatching {
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    out.write(text.toByteArray())
                }
            }
        }
    }

    val saveDdrawIniLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/plain"),
    ) { uri ->
        val text = state.generatedConfigs?.ddrawIniContent ?: return@rememberLauncherForActivityResult
        if (uri != null) {
            runCatching {
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    out.write(text.toByteArray())
                }
            }
        }
    }

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
                // Export & Share Bundle
                TacticalCard(accentColor = AmberSecondary) {
                    SectionHeader(
                        title = "حفظ ومشاركة حزمة التشغيل 60 FPS",
                        subtitle = "احفظ ملفات RA2.INI و ddraw.ini أو شارك الحزمة الكاملة لحاوية الويندوز",
                        icon = Icons.Filled.Archive,
                        iconTint = AmberSecondary,
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onShareConfigBundle,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("share_bundle_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AmberSecondary,
                            contentColor = Color(0xFF1A1200),
                        ),
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "مشاركة حزمة الإعدادات المضغوطة (.ZIP)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        OutlinedButton(
                            onClick = { saveRa2IniLauncher.launch(iniFileName) },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("save_ra2_ini_button"),
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            Icon(Icons.Filled.SaveAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("حفظ $iniFileName")
                        }
                        OutlinedButton(
                            onClick = { saveDdrawIniLauncher.launch("ddraw.ini") },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("save_ddraw_ini_button"),
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            Icon(Icons.Filled.SaveAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("حفظ ddraw.ini")
                        }
                    }
                }
            }

            item {
                // External ARM64 Wine / Winlator Bridge
                TacticalCard(accentColor = RadarCyan) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            SectionHeader(
                                title = "جسور تشغيل ARM64 Windows على الجهاز",
                                subtitle = "يكتشف المشغل تلقائياً تطبيقات Winlator أو Mobox المثبتة",
                                icon = Icons.Filled.Terminal,
                                iconTint = RadarCyan,
                            )
                        }
                        IconButton(
                            onClick = onRefreshRunners,
                            modifier = Modifier.minimumInteractiveComponentSize(),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = "تحديث الفحص",
                                tint = RadarCyan,
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        state.externalRunners.forEach { runner ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = CommandSurfaceHigh,
                                border = BorderStroke(
                                    1.dp,
                                    if (runner.isInstalled) TacticalGreen.copy(alpha = 0.5f) else CommandBorder,
                                ),
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        ) {
                                            Text(
                                                text = runner.displayName,
                                                style = MaterialTheme.typography.titleMedium,
                                                color = TextPrimary,
                                            )
                                            StatusPill(
                                                label = if (runner.isInstalled) "مثبت" else "غير مثبت",
                                                color = if (runner.isInstalled) TacticalGreen else TextSecondary,
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Text(
                                            text = runner.descriptionAr,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TextSecondary,
                                        )
                                    }
                                    if (runner.isInstalled) {
                                        Button(
                                            onClick = { onLaunchExternalRunner(runner.packageName) },
                                            modifier = Modifier.height(48.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = TacticalGreen,
                                                contentColor = Color(0xFF001F10),
                                            ),
                                        ) {
                                            Icon(Icons.Filled.Launch, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("فتح")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(Icons.Filled.History, contentDescription = null, tint = TextSecondary)
                        Text(
                            text = "سجل عمليات الحاوية (${state.logs.size})",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                        )
                    }
                    if (state.logs.isNotEmpty()) {
                        IconButton(
                            onClick = onClearLogs,
                            modifier = Modifier.minimumInteractiveComponentSize(),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.DeleteSweep,
                                contentDescription = "مسح السجل",
                                tint = CrimsonPrimary,
                            )
                        }
                    }
                }
            }

            if (state.logs.isEmpty()) {
                item {
                    TacticalCard {
                        Text(
                            text = "لا توجد عمليات مسجلة بعد. سيظهر هنا سجل استيراد الملفات وتفعيل المودات وتوليد إعدادات ARM64.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                        )
                    }
                }
            } else {
                items(state.logs, key = { it.id }) { log ->
                    LogRow(log)
                }
            }
        }
    }
}

@Composable
private fun LogRow(log: LaunchLogEntity) {
    val color = when (log.level) {
        "SUCCESS" -> TacticalGreen
        "WARN" -> AmberSecondary
        else -> RadarCyan
    }
    val timeText = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(log.timestamp))
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = CommandObsidian,
        border = BorderStroke(1.dp, CommandBorder),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            StatusPill(label = timeText, color = color)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = log.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                )
                Text(
                    text = log.detail,
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary,
                )
            }
        }
    }
}
