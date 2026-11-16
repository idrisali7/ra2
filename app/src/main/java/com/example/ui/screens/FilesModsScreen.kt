package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.Ra2Repository
import com.example.data.local.GameFileEntity
import com.example.data.local.ModPackageEntity
import com.example.model.FileCategory
import com.example.model.ModType
import com.example.ui.LauncherUiState
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatusPill
import com.example.ui.components.TacticalCard
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.CommandBorder
import com.example.ui.theme.CommandSurfaceHigh
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.RadarCyan
import com.example.ui.theme.TacticalGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FilesModsScreen(
    state: LauncherUiState,
    onImportGameFiles: (List<Uri>) -> Unit,
    onImportModFiles: (List<Uri>) -> Unit,
    onDeleteGameFile: (GameFileEntity) -> Unit,
    onToggleMod: (ModPackageEntity, Boolean) -> Unit,
    onMoveModPriority: (ModPackageEntity, Boolean) -> Unit,
    onDeleteMod: (ModPackageEntity) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    val gameFilesLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments(),
    ) { uris ->
        if (uris.isNotEmpty()) onImportGameFiles(uris)
    }

    val modFilesLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments(),
    ) { uris ->
        if (uris.isNotEmpty()) onImportModFiles(uris)
    }

    val coreNames = setOf("RA2.EXE", "ra2.mix", "language.mix", "Game.exe", "binkw32.dll", "ddraw.dll")
    val importedLower = state.gameFiles.map { it.fileName.lowercase() }.toSet()

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 760.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    SegmentedButton(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                        modifier = Modifier.testTag("tab_original_files"),
                    ) {
                        Text("ملفات اللعبة الأصلية (${state.gameFiles.size})")
                    }
                    SegmentedButton(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                        modifier = Modifier.testTag("tab_mods"),
                    ) {
                        Text("ملف المودات (${state.mods.size})")
                    }
                }
            }

            if (selectedTab == 0) {
                item {
                    TacticalCard(accentColor = AmberSecondary) {
                        SectionHeader(
                            title = "استيراد ملفات Red Alert 2 الأصلية",
                            subtitle = "يتم نسخ الملفات بأمان إلى حاوية اللعبة الداخلية (بدون الحاجة لصلاحيات التخزين)",
                            icon = Icons.Filled.FolderOpen,
                            iconTint = AmberSecondary,
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { gameFilesLauncher.launch(arrayOf("*/*")) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("import_original_files_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CrimsonPrimary,
                                contentColor = Color.White,
                            ),
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "اختيار ملفات اللعبة (.EXE / .MIX / .DLL)",
                                style = MaterialTheme.typography.titleMedium,
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "الملفات الأساسية المتوقعة لـ Red Alert 2:",
                            style = MaterialTheme.typography.labelLarge,
                            color = TextSecondary,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            coreNames.forEach { core ->
                                val present = core.lowercase() in importedLower
                                StatusPill(
                                    label = core,
                                    color = if (present) TacticalGreen else TextSecondary,
                                    icon = if (present) Icons.Filled.CheckCircle else null,
                                )
                            }
                        }
                    }
                }

                if (state.gameFiles.isEmpty()) {
                    item {
                        EmptyFilesCard(
                            title = "لا توجد ملفات لعبة مستوردة بعد",
                            subtitle = "اضغط على زر الاستيراد بالأعلى لتحديد ملفات RA2.EXE و ra2.mix و language.mix أو أي ملف لعبة ويندوز.",
                        )
                    }
                } else {
                    itemsIndexed(state.gameFiles, key = { _, item -> item.id }) { _, file ->
                        OriginalFileItemCard(
                            file = file,
                            onDelete = { onDeleteGameFile(file) },
                        )
                    }
                }
            } else {
                item {
                    TacticalCard(accentColor = RadarCyan) {
                        SectionHeader(
                            title = "إدارة وإضافة ملفات المودات",
                            subtitle = "أضف ملفات .mix أو تعديلات rulesmd.ini أو .zip وتحكم بترتيب التحميل",
                            icon = Icons.Filled.Extension,
                            iconTint = RadarCyan,
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { modFilesLauncher.launch(arrayOf("*/*")) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("import_mod_file_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = RadarCyan,
                                contentColor = Color(0xFF001F24),
                            ),
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "إضافة ملف مود (.MIX / .INI / .ZIP / .MAP)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }

                if (state.mods.isEmpty()) {
                    item {
                        EmptyFilesCard(
                            title = "مكتبة المودات فارغة",
                            subtitle = "أضف مودات Mental Omega أو Apocalypse أو خرائط Skirmish أو ملفات rules.ini المعدّلة لتفعيلها بضغطة زر.",
                        )
                    }
                } else {
                    itemsIndexed(state.mods, key = { _, item -> item.id }) { index, mod ->
                        ModPackageItemCard(
                            mod = mod,
                            isFirst = index == 0,
                            isLast = index == state.mods.lastIndex,
                            onToggle = { enabled -> onToggleMod(mod, enabled) },
                            onMoveUp = { onMoveModPriority(mod, true) },
                            onMoveDown = { onMoveModPriority(mod, false) },
                            onDelete = { onDeleteMod(mod) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyFilesCard(title: String, subtitle: String) {
    TacticalCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = Icons.Filled.InsertDriveFile,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(40.dp),
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )
        }
    }
}

@Composable
private fun OriginalFileItemCard(
    file: GameFileEntity,
    onDelete: () -> Unit,
) {
    val categoryLabel = runCatching { FileCategory.valueOf(file.category).labelAr }
        .getOrDefault(file.category)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = CommandSurfaceHigh,
        border = BorderStroke(1.dp, if (file.isCoreRa2File) TacticalGreen.copy(alpha = 0.45f) else CommandBorder),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = file.fileName,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (file.isCoreRa2File) {
                        StatusPill(label = "أساسي", color = TacticalGreen)
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$categoryLabel • ${Ra2Repository.formatBytes(file.sizeBytes)} • SHA:${file.sha256Short}",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary,
                )
            }
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("delete_game_file_${file.id}"),
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "حذف ${file.fileName}",
                    tint = CrimsonPrimary,
                )
            }
        }
    }
}

@Composable
private fun ModPackageItemCard(
    mod: ModPackageEntity,
    isFirst: Boolean,
    isLast: Boolean,
    onToggle: (Boolean) -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onDelete: () -> Unit,
) {
    val typeLabel = runCatching { ModType.valueOf(mod.modType).labelAr }.getOrDefault(mod.modType)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = CommandSurfaceHigh,
        border = BorderStroke(1.dp, if (mod.isEnabled) RadarCyan.copy(alpha = 0.5f) else CommandBorder),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = mod.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "${mod.fileName} • ${Ra2Repository.formatBytes(mod.sizeBytes)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary,
                    )
                }
                Switch(
                    checked = mod.isEnabled,
                    onCheckedChange = onToggle,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("toggle_mod_${mod.id}"),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = RadarCyan,
                    ),
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                StatusPill(
                    label = typeLabel,
                    color = if (mod.isEnabled) RadarCyan else TextSecondary,
                )
                Row {
                    IconButton(
                        onClick = onMoveUp,
                        enabled = !isFirst,
                        modifier = Modifier.minimumInteractiveComponentSize(),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ArrowUpward,
                            contentDescription = "رفع أولوية المود",
                            tint = if (!isFirst) TextPrimary else TextSecondary.copy(alpha = 0.35f),
                        )
                    }
                    IconButton(
                        onClick = onMoveDown,
                        enabled = !isLast,
                        modifier = Modifier.minimumInteractiveComponentSize(),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ArrowDownward,
                            contentDescription = "خفض أولوية المود",
                            tint = if (!isLast) TextPrimary else TextSecondary.copy(alpha = 0.35f),
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("delete_mod_${mod.id}"),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "إزالة المود ${mod.name}",
                            tint = CrimsonPrimary,
                        )
                    }
                }
            }
        }
    }
}
