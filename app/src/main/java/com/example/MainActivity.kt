package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.FolderZip
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.TouchControlMode
import com.example.ui.MainViewModel
import com.example.ui.screens.FilesModsScreen
import com.example.ui.screens.GraphicsArm64Screen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SystemBridgeScreen
import com.example.ui.screens.TouchPlayViewportScreen
import com.example.ui.theme.CommandObsidian
import com.example.ui.theme.CommandSurface
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.Ra2LauncherTheme
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

enum class AppDestination(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
) {
    HOME("home", "الرئيسية", Icons.Filled.Home, Icons.Outlined.Home),
    FILES("files", "الملفات والمودات", Icons.Filled.FolderZip, Icons.Outlined.FolderZip),
    GRAPHICS("graphics", "الرسوميات و ARM64", Icons.Filled.Tune, Icons.Outlined.Tune),
    SYSTEM("system", "الجسر والتصدير", Icons.Filled.Terminal, Icons.Outlined.Terminal),
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Ra2LauncherTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Ra2LauncherApp()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Ra2LauncherApp(
    viewModel: MainViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    var currentRoute by rememberSaveable { mutableStateOf(AppDestination.HOME.route) }
    var isTouchViewportActive by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(state.statusMessage) {
        val msg = state.statusMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusMessage()
        }
    }

    fun launchExternalPackage(packageName: String) {
        viewModel.prepareContainerAndThen {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                context.startActivity(launchIntent)
            }
        }
    }

    fun shareZipBundle() {
        viewModel.exportBundleZip { zipFile ->
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                zipFile,
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/zip"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "مشاركة حزمة إعدادات RA2 ARM64"))
        }
    }

    if (isTouchViewportActive) {
        TouchPlayViewportScreen(
            state = state,
            onToggleTouchMode = {
                val next = if (state.settings.touchControlMode == TouchControlMode.DIRECT_TOUCH) {
                    TouchControlMode.VIRTUAL_TRACKPAD
                } else {
                    TouchControlMode.DIRECT_TOUCH
                }
                viewModel.setTouchControlMode(next)
            },
            onExitViewport = { isTouchViewportActive = false },
        )
        return
    }

    val currentDestination = AppDestination.entries.firstOrNull { it.route == currentRoute }
        ?: AppDestination.HOME

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useRail = maxWidth >= 600.dp

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = CommandObsidian,
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = currentDestination.label,
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary,
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = CommandObsidian,
                    ),
                )
            },
            bottomBar = {
                if (!useRail) {
                    NavigationBar(
                        containerColor = CommandSurface,
                        windowInsets = WindowInsets.navigationBars,
                    ) {
                        AppDestination.entries.forEach { dest ->
                            val selected = currentRoute == dest.route
                            NavigationBarItem(
                                selected = selected,
                                onClick = { currentRoute = dest.route },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                        contentDescription = dest.label,
                                    )
                                },
                                label = { Text(dest.label, maxLines = 1) },
                                modifier = Modifier.testTag("nav_${dest.route}"),
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = CrimsonPrimary,
                                    selectedTextColor = CrimsonPrimary,
                                    indicatorColor = CrimsonPrimary.copy(alpha = 0.16f),
                                    unselectedIconColor = TextSecondary,
                                    unselectedTextColor = TextSecondary,
                                ),
                            )
                        }
                    }
                }
            },
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                if (useRail) {
                    NavigationRail(
                        containerColor = CommandSurface,
                    ) {
                        AppDestination.entries.forEach { dest ->
                            val selected = currentRoute == dest.route
                            NavigationRailItem(
                                selected = selected,
                                onClick = { currentRoute = dest.route },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                        contentDescription = dest.label,
                                    )
                                },
                                label = { Text(dest.label) },
                                modifier = Modifier.testTag("rail_${dest.route}"),
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = CrimsonPrimary,
                                    selectedTextColor = CrimsonPrimary,
                                    indicatorColor = CrimsonPrimary.copy(alpha = 0.16f),
                                    unselectedIconColor = TextSecondary,
                                    unselectedTextColor = TextSecondary,
                                ),
                            )
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    when (currentDestination) {
                        AppDestination.HOME -> HomeScreen(
                            state = state,
                            onSelectPreset = viewModel::selectPreset,
                            onLaunchTouchPlay = {
                                viewModel.prepareContainerAndThen {
                                    isTouchViewportActive = true
                                }
                            },
                            onApplyMaxSmoothness = viewModel::applyMaximumSmoothnessPreset,
                            onNavigateToFiles = { currentRoute = AppDestination.FILES.route },
                            onNavigateToGraphics = { currentRoute = AppDestination.GRAPHICS.route },
                            onLaunchExternalRunner = ::launchExternalPackage,
                        )
                        AppDestination.FILES -> FilesModsScreen(
                            state = state,
                            onImportGameFiles = viewModel::importGameFiles,
                            onImportModFiles = viewModel::importModFiles,
                            onDeleteGameFile = viewModel::deleteGameFile,
                            onToggleMod = viewModel::toggleMod,
                            onMoveModPriority = viewModel::moveModPriority,
                            onDeleteMod = viewModel::deleteMod,
                        )
                        AppDestination.GRAPHICS -> GraphicsArm64Screen(
                            state = state,
                            onSetTargetFps = viewModel::setTargetFps,
                            onSetVSync = viewModel::setVSync,
                            onSelectRenderer = viewModel::selectRenderer,
                            onSelectScreenFillMode = viewModel::selectScreenFillMode,
                            onSelectShaderFilter = viewModel::selectShaderFilter,
                            onSetRenderScale = viewModel::setRenderScale,
                            onSetSingleCpuFix = viewModel::setSingleCpuFix,
                            onSetDisableVideoBackBuffer = viewModel::setDisableVideoBackBuffer,
                            onSetEdgeToEdgeCutout = viewModel::setEdgeToEdgeCutout,
                            onSelectArm64Bridge = viewModel::selectArm64Bridge,
                            onSetDynarecFastMath = viewModel::setDynarecFastMath,
                            onSetEsync = viewModel::setEsync,
                            onSelectTouchMode = viewModel::setTouchControlMode,
                            onSetShowTouchHud = viewModel::setShowTouchHudButtons,
                        )
                        AppDestination.SYSTEM -> SystemBridgeScreen(
                            state = state,
                            onRefreshRunners = viewModel::refreshDeviceAndRunners,
                            onLaunchExternalRunner = ::launchExternalPackage,
                            onShareConfigBundle = ::shareZipBundle,
                            onClearLogs = viewModel::clearLogs,
                        )
                    }
                }
            }
        }
    }
}
