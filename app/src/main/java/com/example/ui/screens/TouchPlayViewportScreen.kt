package com.example.ui.screens

import android.app.Activity
import android.os.Build
import android.view.Choreographer
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mouse
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.model.TouchControlMode
import com.example.ui.LauncherUiState
import com.example.ui.components.StatusPill
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.CommandBorder
import com.example.ui.theme.CommandObsidian
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.RadarCyan
import com.example.ui.theme.TacticalGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.hypot
import kotlin.math.roundToInt

private data class TacticalUnit(
    val id: Int,
    val squad: Int,
    val label: String,
    var xNorm: Float,
    var yNorm: Float,
    var targetXNorm: Float,
    var targetYNorm: Float,
    var selected: Boolean = false,
)

private data class CommandPulse(
    val xNorm: Float,
    val yNorm: Float,
    val isAttack: Boolean,
    val createdNanos: Long,
)

@Composable
fun TouchPlayViewportScreen(
    state: LauncherUiState,
    onToggleTouchMode: () -> Unit,
    onExitViewport: () -> Unit,
) {
    BackHandler { onExitViewport() }

    val view = LocalView.current
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val settings = state.settings

    // Enforce real immersive fullscreen + display cutout fill + 60Hz refresh rate request
    DisposableEffect(settings.targetFps, settings.enableEdgeToEdgeCutout) {
        val activity = context as? Activity
        val window = activity?.window
        val controller = window?.let { WindowCompat.getInsetsController(it, view) }
        val prevCutoutMode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window?.attributes?.layoutInDisplayCutoutMode
        } else null
        val prevRefreshRate = window?.attributes?.preferredRefreshRate ?: 0f

        controller?.let {
            it.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            it.hide(WindowInsetsCompat.Type.systemBars())
        }
        window?.let { win ->
            val lp = win.attributes
            lp.preferredRefreshRate = settings.targetFps.toFloat()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                lp.layoutInDisplayCutoutMode = if (settings.enableEdgeToEdgeCutout) {
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                } else {
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT
                }
            }
            win.attributes = lp
            win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }

        onDispose {
            controller?.show(WindowInsetsCompat.Type.systemBars())
            window?.let { win ->
                val lp = win.attributes
                lp.preferredRefreshRate = prevRefreshRate
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && prevCutoutMode != null) {
                    lp.layoutInDisplayCutoutMode = prevCutoutMode
                }
                win.attributes = lp
                win.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }
    }

    // Real Choreographer 60 FPS frame pacer & unit kinematics
    var measuredFps by remember { mutableIntStateOf(settings.targetFps) }
    var frameTimeMs by remember { mutableFloatStateOf(16.6f) }
    var frameTick by remember { mutableLongStateOf(0L) }
    var rightClickMode by remember { mutableStateOf(false) }
    var activeCommandLabel by remember { mutableStateOf("LMB_READY") }
    var lastTouchNorm by remember { mutableStateOf(Offset(0.5f, 0.5f)) }
    var dragStart by remember { mutableStateOf<Offset?>(null) }
    var dragCurrent by remember { mutableStateOf<Offset?>(null) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }
    var zoomScale by remember { mutableFloatStateOf(1f) }
    var canvasSize by remember { mutableStateOf(Size(1f, 1f)) }

    val units = remember {
        mutableStateListOf(
            TacticalUnit(1, 1, "RHINO-1", 0.34f, 0.45f, 0.34f, 0.45f, selected = true),
            TacticalUnit(2, 1, "RHINO-2", 0.41f, 0.52f, 0.41f, 0.52f, selected = true),
            TacticalUnit(3, 2, "APOC-1", 0.58f, 0.40f, 0.58f, 0.40f, selected = false),
            TacticalUnit(4, 2, "KIROV-1", 0.65f, 0.58f, 0.65f, 0.58f, selected = false),
            TacticalUnit(5, 3, "MCV-HQ", 0.25f, 0.62f, 0.25f, 0.62f, selected = false),
        )
    }
    val pulses = remember { mutableStateListOf<CommandPulse>() }

    DisposableEffect(settings.targetFps) {
        val choreographer = Choreographer.getInstance()
        var lastNanos = 0L
        var fpsWindowStart = 0L
        var frameCounter = 0

        val callback = object : Choreographer.FrameCallback {
            override fun doFrame(frameTimeNanos: Long) {
                if (lastNanos != 0L) {
                    val dtSec = ((frameTimeNanos - lastNanos) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
                    frameTimeMs = (frameTimeNanos - lastNanos) / 1_000_000f

                    // Smooth unit movement toward ordered waypoint at real frame dt
                    units.forEachIndexed { idx, unit ->
                        val dx = unit.targetXNorm - unit.xNorm
                        val dy = unit.targetYNorm - unit.yNorm
                        val dist = hypot(dx, dy)
                        if (dist > 0.004f) {
                            val speed = 0.22f * dtSec
                            val step = minOf(speed, dist)
                            units[idx] = unit.copy(
                                xNorm = unit.xNorm + (dx / dist) * step,
                                yNorm = unit.yNorm + (dy / dist) * step,
                            )
                        }
                    }

                    // Expire old command pulses (> 900ms)
                    pulses.removeAll { frameTimeNanos - it.createdNanos > 900_000_000L }
                }
                lastNanos = frameTimeNanos

                if (fpsWindowStart == 0L) fpsWindowStart = frameTimeNanos
                frameCounter++
                val elapsed = frameTimeNanos - fpsWindowStart
                if (elapsed >= 500_000_000L) {
                    measuredFps = ((frameCounter * 1_000_000_000.0) / elapsed).roundToInt()
                    frameCounter = 0
                    fpsWindowStart = frameTimeNanos
                }
                frameTick = frameTimeNanos
                choreographer.postFrameCallback(this)
            }
        }
        choreographer.postFrameCallback(callback)
        onDispose { choreographer.removeFrameCallback(callback) }
    }

    fun issueMoveOrSelectAt(posPx: Offset, forceRightClick: Boolean) {
        val w = canvasSize.width.coerceAtLeast(1f)
        val h = canvasSize.height.coerceAtLeast(1f)
        val nx = (posPx.x / w).coerceIn(0.03f, 0.97f)
        val ny = (posPx.y / h).coerceIn(0.08f, 0.92f)
        lastTouchNorm = Offset(nx, ny)

        if (forceRightClick || rightClickMode) {
            var movedAny = false
            units.forEachIndexed { idx, u ->
                if (u.selected) {
                    val spread = (idx - 1) * 0.025f
                    units[idx] = u.copy(
                        targetXNorm = (nx + spread).coerceIn(0.05f, 0.95f),
                        targetYNorm = (ny + spread * 0.6f).coerceIn(0.10f, 0.90f),
                    )
                    movedAny = true
                }
            }
            if (movedAny) {
                pulses.add(CommandPulse(nx, ny, isAttack = false, createdNanos = System.nanoTime()))
                activeCommandLabel = "RMB_MOVE (${(nx * 100).roundToInt()}%, ${(ny * 100).roundToInt()}%)"
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }
        } else {
            // Check if tapped near a unit to select it, otherwise order selected units in Direct Touch mode
            val hitIndex = units.indexOfFirst { hypot(it.xNorm - nx, it.yNorm - ny) < 0.06f }
            if (hitIndex >= 0) {
                units.forEachIndexed { idx, u ->
                    units[idx] = u.copy(selected = idx == hitIndex)
                }
                activeCommandLabel = "SELECT ${units[hitIndex].label}"
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            } else {
                // Direct Touch RTS order to move selected squad
                var ordered = false
                units.forEachIndexed { idx, u ->
                    if (u.selected) {
                        val offset = ((idx % 3) - 1) * 0.03f
                        units[idx] = u.copy(
                            targetXNorm = (nx + offset).coerceIn(0.05f, 0.95f),
                            targetYNorm = (ny + offset * 0.5f).coerceIn(0.10f, 0.90f),
                        )
                        ordered = true
                    }
                }
                if (ordered) {
                    pulses.add(CommandPulse(nx, ny, isAttack = false, createdNanos = System.nanoTime()))
                    activeCommandLabel = "TOUCH_ORDER -> (${(nx * 100).roundToInt()}%, ${(ny * 100).roundToInt()}%)"
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CommandObsidian)
            .testTag("touch_play_viewport"),
    ) {
        // Real 60FPS Interactive RTS Tactical Canvas
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(rightClickMode, settings.touchControlMode) {
                    detectTapGestures(
                        onTap = { pos ->
                            canvasSize = Size(size.width.toFloat(), size.height.toFloat())
                            issueMoveOrSelectAt(pos, forceRightClick = false)
                        },
                        onLongPress = { pos ->
                            canvasSize = Size(size.width.toFloat(), size.height.toFloat())
                            issueMoveOrSelectAt(pos, forceRightClick = true)
                        },
                    )
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { start ->
                            canvasSize = Size(size.width.toFloat(), size.height.toFloat())
                            dragStart = start
                            dragCurrent = start
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            dragCurrent = change.position
                        },
                        onDragEnd = {
                            val s = dragStart
                            val e = dragCurrent
                            if (s != null && e != null && canvasSize.width > 1f) {
                                val rect = Rect(
                                    left = minOf(s.x, e.x) / canvasSize.width,
                                    top = minOf(s.y, e.y) / canvasSize.height,
                                    right = maxOf(s.x, e.x) / canvasSize.width,
                                    bottom = maxOf(s.y, e.y) / canvasSize.height,
                                )
                                if (rect.width > 0.02f || rect.height > 0.02f) {
                                    var selectedCount = 0
                                    units.forEachIndexed { idx, u ->
                                        val inside = u.xNorm in rect.left..rect.right &&
                                            u.yNorm in rect.top..rect.bottom
                                        units[idx] = u.copy(selected = inside)
                                        if (inside) selectedCount++
                                    }
                                    activeCommandLabel = "MARQUEE_BOX ($selectedCount وحدات)"
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                            }
                            dragStart = null
                            dragCurrent = null
                        },
                        onDragCancel = {
                            dragStart = null
                            dragCurrent = null
                        },
                    )
                }
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        if (zoom != 1f) {
                            zoomScale = (zoomScale * zoom).coerceIn(0.8f, 1.6f)
                            panOffset += pan
                            activeCommandLabel = "ZOOM ${(zoomScale * 100).roundToInt()}%"
                        }
                    }
                },
        ) {
            // Read frameTick so Canvas invalidates smoothly at display Choreographer cadence
            @Suppress("UNUSED_VARIABLE")
            val tick = frameTick

            val gridStep = 56.dp.toPx() * zoomScale
            val offsetX = panOffset.x % gridStep
            val offsetY = panOffset.y % gridStep

            var gx = offsetX
            while (gx < size.width) {
                drawLine(
                    color = Color(0xFF17202E),
                    start = Offset(gx, 0f),
                    end = Offset(gx, size.height),
                    strokeWidth = 1f,
                )
                gx += gridStep
            }
            var gy = offsetY
            while (gy < size.height) {
                drawLine(
                    color = Color(0xFF17202E),
                    start = Offset(0f, gy),
                    end = Offset(size.width, gy),
                    strokeWidth = 1f,
                )
                gy += gridStep
            }

            // Radar center crosshair
            val center = Offset(size.width / 2f, size.height / 2f)
            drawCircle(
                color = CrimsonPrimary.copy(alpha = 0.14f),
                radius = minOf(size.width, size.height) * 0.34f * zoomScale,
                center = center,
                style = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f))),
            )

            // Draw command waypoint pulses
            pulses.forEach { pulse ->
                val p = Offset(pulse.xNorm * size.width, pulse.yNorm * size.height)
                val pulseColor = if (pulse.isAttack) CrimsonPrimary else TacticalGreen
                drawCircle(
                    color = pulseColor.copy(alpha = 0.7f),
                    radius = 22.dp.toPx(),
                    center = p,
                    style = Stroke(width = 2.5.dp.toPx()),
                )
                drawCircle(
                    color = pulseColor,
                    radius = 5.dp.toPx(),
                    center = p,
                )
            }

            // Draw tactical units + selection brackets
            units.forEach { unit ->
                val pos = Offset(unit.xNorm * size.width, unit.yNorm * size.height)
                val target = Offset(unit.targetXNorm * size.width, unit.targetYNorm * size.height)

                if (hypot(target.x - pos.x, target.y - pos.y) > 8f) {
                    drawLine(
                        color = TacticalGreen.copy(alpha = 0.45f),
                        start = pos,
                        end = target,
                        strokeWidth = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f)),
                    )
                }

                // Unit body
                val bodyColor = when (unit.squad) {
                    1 -> CrimsonPrimary
                    2 -> AmberSecondary
                    else -> RadarCyan
                }
                drawRect(
                    color = bodyColor,
                    topLeft = Offset(pos.x - 12.dp.toPx(), pos.y - 12.dp.toPx()),
                    size = Size(24.dp.toPx(), 24.dp.toPx()),
                )

                if (unit.selected) {
                    drawRect(
                        color = TacticalGreen,
                        topLeft = Offset(pos.x - 17.dp.toPx(), pos.y - 17.dp.toPx()),
                        size = Size(34.dp.toPx(), 34.dp.toPx()),
                        style = Stroke(width = 2.dp.toPx()),
                    )
                }
            }

            // Selection Marquee Box
            val ds = dragStart
            val dc = dragCurrent
            if (ds != null && dc != null) {
                val topLeft = Offset(minOf(ds.x, dc.x), minOf(ds.y, dc.y))
                val boxSize = Size(kotlin.math.abs(dc.x - ds.x), kotlin.math.abs(dc.y - ds.y))
                drawRect(
                    color = TacticalGreen.copy(alpha = 0.14f),
                    topLeft = topLeft,
                    size = boxSize,
                )
                drawRect(
                    color = TacticalGreen,
                    topLeft = topLeft,
                    size = boxSize,
                    style = Stroke(width = 2.dp.toPx()),
                )
            }
        }

        // Top Telemetry Bar (respects display cutout safe padding)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.displayCutout)
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                StatusPill(
                    label = "$measuredFps FPS (${"%.1f".format(frameTimeMs)}ms)",
                    color = TacticalGreen,
                )
                StatusPill(
                    label = "ARM64 • ${settings.selectedPreset.executableName}",
                    color = RadarCyan,
                )
                StatusPill(
                    label = activeCommandLabel,
                    color = AmberSecondary,
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    onClick = onToggleTouchMode,
                    shape = RoundedCornerShape(50),
                    color = CommandObsidian.copy(alpha = 0.85f),
                    border = BorderStroke(1.dp, CommandBorder),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            imageVector = if (settings.touchControlMode == TouchControlMode.DIRECT_TOUCH) {
                                Icons.Filled.TouchApp
                            } else {
                                Icons.Filled.Mouse
                            },
                            contentDescription = null,
                            tint = RadarCyan,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            text = if (settings.touchControlMode == TouchControlMode.DIRECT_TOUCH) "لمس مباشر" else "مؤشر فأرة",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextPrimary,
                        )
                    }
                }

                IconButton(
                    onClick = onExitViewport,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .background(CrimsonPrimary.copy(alpha = 0.85f), RoundedCornerShape(50))
                        .testTag("exit_touch_viewport_button"),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "إغلاق وضع اللعب باللمس",
                        tint = Color.White,
                    )
                }
            }
        }

        // Bottom RTS Touch Controls HUD
        if (settings.showTouchHudButtons) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.displayCutout)
                    .padding(12.dp)
                    .align(Alignment.BottomCenter),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = CommandObsidian.copy(alpha = 0.84f),
                    border = BorderStroke(1.dp, CommandBorder),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        listOf(1, 2, 3).forEach { squadNum ->
                            HudKeyButton(
                                label = "فريق $squadNum",
                                accent = RadarCyan,
                                onClick = {
                                    units.forEachIndexed { idx, u ->
                                        units[idx] = u.copy(selected = u.squad == squadNum)
                                    }
                                    activeCommandLabel = "CTRL+$squadNum"
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                },
                            )
                        }
                        HudKeyButton(
                            label = "هجوم (A)",
                            accent = CrimsonPrimary,
                            onClick = {
                                pulses.add(
                                    CommandPulse(
                                        lastTouchNorm.x,
                                        lastTouchNorm.y,
                                        isAttack = true,
                                        createdNanos = System.nanoTime(),
                                    ),
                                )
                                activeCommandLabel = "KEY_A (Attack-Move)"
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            },
                        )
                        HudKeyButton(
                            label = "نشر (D)",
                            accent = AmberSecondary,
                            onClick = {
                                activeCommandLabel = "KEY_D (Deploy)"
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            },
                        )
                        HudKeyButton(
                            label = if (rightClickMode) "أيمن: نشط" else "نقر أيمن",
                            accent = if (rightClickMode) TacticalGreen else TextSecondary,
                            onClick = {
                                rightClickMode = !rightClickMode
                                activeCommandLabel = if (rightClickMode) "RMB_STICKY_ON" else "LMB_MODE"
                            },
                        )
                    }
                }

                // Helpful container status footer
                Text(
                    text = "اسحب لتحديد الوحدات بالمربع الأخضر • انقر للتوجيه • اضغط مطولاً للأمر التكتيكي • ${state.activeModsCount} مود نشط • ${state.generatedConfigs?.iniFileName ?: "RA2.INI"} مكتوب وجاهز",
                    fontFamily = JetBrainsMonoFontFamily,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
            }
        }
    }
}

@Composable
private fun HudKeyButton(
    label: String,
    accent: Color,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = Modifier.height(48.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = accent.copy(alpha = 0.18f),
            contentColor = accent,
        ),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.6f)),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
        )
    }
}
