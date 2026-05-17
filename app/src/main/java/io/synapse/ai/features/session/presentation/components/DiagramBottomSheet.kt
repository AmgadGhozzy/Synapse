package io.synapse.ai.features.session.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import io.synapse.ai.R
import io.synapse.ai.core.theme.synapse
import io.synapse.ai.core.theme.tokens.adp
import io.synapse.ai.core.theme.tokens.asp
import io.synapse.ai.core.ui.components.CloseButton
import io.synapse.ai.core.ui.components.LoadingIndicator
import io.synapse.ai.core.ui.components.SecondaryButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagramBottomSheet(
    title: String,
    mermaid: String,
    explanation: String? = null,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    val surfaceArgb = MaterialTheme.colorScheme.surface.toArgb()
    val bgColorHex = "%06X".format(surfaceArgb and 0xFFFFFF)

    val onSurfaceVariantArgb = MaterialTheme.colorScheme.onSurfaceVariant.toArgb()
    val textColorHex = "%06X".format(onSurfaceVariantArgb and 0xFFFFFF)

    val diagramUrl = remember(mermaid, isDark, bgColorHex, textColorHex) {
        buildMermaidInkUrl(
            mermaidCode = mermaid,
            darkTheme = isDark,
            bgColorHex = bgColorHex,
            textColorHex = textColorHex,
        )
    }

    val surfaceColor = MaterialTheme.colorScheme.surface
    val primaryColor = MaterialTheme.colorScheme.primary

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(
            topStart = MaterialTheme.synapse.spacing.s28,
            topEnd = MaterialTheme.synapse.spacing.s28,
        ),
        containerColor = surfaceColor,
        dragHandle = null,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.72f)
                .navigationBarsPadding(),
        ) {
            // ── 1. Zoomable diagram — bottom z-layer ──────────────────────────
            ZoomableDiagramImage(
                url = diagramUrl,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 84.adp, bottom = 116.adp),
            )

            // ── 2. Floating header ────────────────────────────────────────────
            FloatingDiagramHeader(
                title = title,
                onDismiss = onDismiss,
                surfaceColor = surfaceColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter),
            )

            // ── 3. Bottom — explanation + hint ────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            0f to Color.Transparent,
                            0.18f to surfaceColor.copy(alpha = 0.88f),
                            1f to surfaceColor,
                        )
                    )
                    .padding(
                        horizontal = MaterialTheme.synapse.spacing.s20,
                        vertical = MaterialTheme.synapse.spacing.s16,
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.synapse.spacing.s8),
            ) {
                if (!explanation.isNullOrBlank()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(MaterialTheme.synapse.spacing.s12),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                    ) {
                        Text(
                            text = explanation,
                            style = MaterialTheme.typography.bodySmall.copy(
                                lineHeight = 18.asp,
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(
                                horizontal = MaterialTheme.synapse.spacing.s16,
                                vertical = MaterialTheme.synapse.spacing.s12,
                            ),
                        )
                    }
                }

                Text(
                    text = stringResource(R.string.quiz_diagram_pinch_hint),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

// ─── Floating header ──────────────────────────────────────────────────────────

@Composable
private fun FloatingDiagramHeader(
    title: String,
    onDismiss: () -> Unit,
    surfaceColor: Color,
    modifier: Modifier = Modifier,
) {
    val closeButtonSize = 44.adp

    Box(
        modifier = modifier
            .height(84.adp)
            .background(
                Brush.verticalGradient(
                    1f to Color.Transparent,
                    0f to surfaceColor,
                )
            ),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Drag handle pill
            Spacer(Modifier.height(MaterialTheme.synapse.spacing.s12))
            Box(
                modifier = Modifier
                    .size(width = 40.adp, height = 4.adp)
                    .background(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                        shape = MaterialTheme.synapse.radius.pill,
                    )
            )
            Spacer(Modifier.height(MaterialTheme.synapse.spacing.s10))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MaterialTheme.synapse.spacing.s12),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CloseButton(onClick = onDismiss)

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = MaterialTheme.synapse.spacing.s8),
                    maxLines = 1,
                )

                // Symmetrical spacer so title stays centered
                Spacer(modifier = Modifier.size(closeButtonSize))
            }
        }
    }
}

// ─── Zoomable image ───────────────────────────────────────────────────────────

@Composable
private fun ZoomableDiagramImage(
    url: String,
    modifier: Modifier = Modifier,
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var retryTrigger by remember { mutableIntStateOf(0) }

    val animatedScale by animateFloatAsState(
        targetValue = scale,
        animationSpec = spring(stiffness = 300f),
        label = "diagram_scale",
    )
    val animatedOffsetX by animateFloatAsState(
        targetValue = offset.x,
        animationSpec = spring(stiffness = 300f),
        label = "diagram_offset_x",
    )
    val animatedOffsetY by animateFloatAsState(
        targetValue = offset.y,
        animationSpec = spring(stiffness = 300f),
        label = "diagram_offset_y",
    )

    val context = LocalContext.current
    val imageRequest = remember(url, retryTrigger) {
        ImageRequest.Builder(context)
            .data(url)
            .diskCacheKey(url)
            .memoryCacheKey(url)
            .crossfade(true)
            .build()
    }

    Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(0.5f, 6f)
                    offset = Offset(offset.x + pan.x, offset.y + pan.y)
                }
            }
            .pointerInput(Unit) {
                // Double-tap to reset zoom & pan
                detectTapGestures(
                    onDoubleTap = {
                        scale = 1f
                        offset = Offset.Zero
                    }
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        key(retryTrigger) {
            SubcomposeAsyncImage(
                model = imageRequest,
                contentDescription = stringResource(R.string.quiz_diagram_cd),
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = animatedScale,
                        scaleY = animatedScale,
                        translationX = animatedOffsetX,
                        translationY = animatedOffsetY,
                    ),
                loading = {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.adp),
                        ) {
                            LoadingIndicator()
                            Text(
                                text = stringResource(R.string.quiz_diagram_loading),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
                error = {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.adp),
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_alert_triangle),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(32.adp),
                            )
                            Text(
                                text = stringResource(R.string.quiz_diagram_error),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                            )
                            Spacer(modifier = Modifier.height(4.adp))
                            SecondaryButton(
                                text = stringResource(R.string.retry),
                                onClick = { retryTrigger++ },
                                modifier = Modifier.widthIn(max = 180.adp),
                            )
                        }
                    }
                },
            )
        }
    }
}

// ─── URL builder ─────────────────────────────────────────────────────────────

internal fun buildMermaidInkUrl(
    mermaidCode: String,
    darkTheme: Boolean = false,
    bgColorHex: String = "ffffff",
    textColorHex: String = "000000",
): String {
    val cleanCode = mermaidCode
        .removePrefix("```mermaid")
        .removePrefix("```")
        .removeSuffix("```")
        .trim()

    val themeStr = if (darkTheme) "dark" else "default"

    val withLook = if ("init" !in cleanCode) {
        "%%{init: {'theme': '$themeStr', 'themeVariables': { 'primaryTextColor': '#$textColorHex', 'lineColor': '#$textColorHex', 'textColor': '#$textColorHex', 'nodeTextColor': '#$textColorHex' }}}%%\n$cleanCode"
    } else {
        cleanCode
    }

    val encoded = android.util.Base64.encodeToString(
        withLook.toByteArray(Charsets.UTF_8),
        android.util.Base64.URL_SAFE or android.util.Base64.NO_PADDING or android.util.Base64.NO_WRAP,
    )

    return "https://mermaid.ink/img/$encoded" +
            "?type=webp" +
            "&theme=$themeStr" +
            "&bgColor=$bgColorHex" +
            "&width=1200" +
            "&scale=3"
}