package io.synapse.ai.features.add_pdf.presentation.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.synapse.ai.R
import io.synapse.ai.core.theme.SynapseTheme
import io.synapse.ai.core.theme.synapse
import io.synapse.ai.core.theme.tokens.adp
import io.synapse.ai.core.theme.tokens.toShadow
import io.synapse.ai.core.ui.components.LoadingIndicator
import io.synapse.ai.core.ui.components.PrimaryGradientButton
import io.synapse.ai.core.ui.components.SecondaryButton
import io.synapse.ai.core.ui.components.StatusIconHeader
import io.synapse.ai.core.ui.components.WavyProgressIndicator
import io.synapse.ai.core.ui.state.QuestionUiModel
import io.synapse.ai.core.ui.utils.animatedDashedBorder
import io.synapse.ai.core.ui.utils.localized
import io.synapse.ai.domain.model.QuestionType
import io.synapse.ai.features.add_pdf.presentation.state.AddPdfUiState
import io.synapse.ai.features.add_pdf.presentation.state.SourceTab
import kotlinx.coroutines.runBlocking


const val TEXT_MAX_CHARS = 5_000

private data class GenerationTask(val label: String, val done: Boolean)

@Composable
fun UploadStep(
    uiState: AddPdfUiState,
    onTabSelect: (SourceTab) -> Unit,
    onPickFile: () -> Unit,
    onClearFile: () -> Unit,
    onOcrToggle: () -> Unit,
    onPasteTextChange: (String) -> Unit,
    onWebUrlChange: (String) -> Unit = {},
    onWebTabLockedClick: () -> Unit = {},
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val canProceed by remember(
        uiState.sourceTab, uiState.fileName,
        uiState.pasteText, uiState.webUrl,
        uiState.isPro, uiState.isLoading,
    ) {
        derivedStateOf {
            when (uiState.sourceTab) {
                SourceTab.FILE -> uiState.fileName != null && !uiState.isLoading
                SourceTab.TEXT -> uiState.pasteText.length >= 10
                SourceTab.WEB -> uiState.isPro && uiState.webUrl.isNotBlank()
                SourceTab.YOUTUBE -> uiState.isPro && uiState.webUrl.isNotBlank()
            }
        }
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.adp)) {
        SourceTabRow(selected = uiState.sourceTab, onSelect = onTabSelect)

        AnimatedContent(
            targetState = uiState.sourceTab,
            transitionSpec = { fadeIn() togetherWith fadeOut() }
        ) { tab ->
            when (tab) {
                SourceTab.FILE -> FileTab(
                    fileName = uiState.fileName,
                    isLoading = uiState.isLoading,
                    ocrEnabled = uiState.ocrEnabled,
                    isOcrLocked = uiState.isOcrFeatureLocked,
                    maxPages = uiState.maxPages,
                    maxFileSize = uiState.maxFileSizeMb,
                    onPickFile = onPickFile,
                    onClearFile = onClearFile,
                    onOcrToggle = onOcrToggle,
                )

                SourceTab.YOUTUBE, SourceTab.WEB -> {
                    WebTab(
                        webUrl = uiState.webUrl,
                        isPro = uiState.isPro,
                        onUrlChange = onWebUrlChange,
                        onLockedClick = onWebTabLockedClick,
                    )
                }

                SourceTab.TEXT -> TextTab(
                    text = uiState.pasteText,
                    onTextChange = onPasteTextChange,
                )
            }
        }

        PrimaryGradientButton(
            text = stringResource(R.string.add_pdf_continue),
            icon = Icons.AutoMirrored.Rounded.ArrowForwardIos,
            enabled = canProceed,
            onClick = onContinue,
        )
    }
}

@Composable
fun SourceTabRow(
    selected: SourceTab,
    onSelect: (SourceTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tabs = listOf(
        Triple(SourceTab.FILE, stringResource(R.string.tab_file_image), R.drawable.ic_file_image),
        Triple(SourceTab.WEB, stringResource(R.string.tab_web_youtube), R.drawable.ic_youtube),
        Triple(SourceTab.TEXT, stringResource(R.string.tab_plain_text), R.drawable.ic_type),
    )

    val selectedIndex = tabs.indexOfFirst { it.first == selected }
    val density = LocalDensity.current

    // Row measures itself → pill mirrors its height, no intrinsics needed
    var rowHeightDp by remember { mutableStateOf(0.dp) }

    Surface(
        modifier = modifier
            .padding(horizontal = 4.adp)
            .fillMaxWidth()
            .dropShadow(
                MaterialTheme.shapes.medium,
                MaterialTheme.synapse.shadows.subtle.toShadow()
            ),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.medium,
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .padding(horizontal = 5.adp, vertical = 5.adp)
                .fillMaxWidth(),
        ) {
            val tabWidth = maxWidth / tabs.size

            val pillOffsetX by animateDpAsState(
                targetValue = tabWidth * selectedIndex,
                animationSpec = tween(
                    durationMillis = 400,
                    easing = FastOutSlowInEasing,
                )
            )

            // Pill drawn first (behind Row). Height comes from Row's onSizeChanged.
            Box(
                modifier = Modifier
                    .offset(x = pillOffsetX)
                    .width(tabWidth)
                    .height(rowHeightDp)
                    .dropShadow(
                        MaterialTheme.shapes.small,
                        MaterialTheme.synapse.shadows.subtle.toShadow()
                    )
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = MaterialTheme.shapes.small,
                    ),
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .onSizeChanged() { size ->
                        rowHeightDp = with(density) { size.height.toDp() }
                    },
            ) {
                tabs.forEach { (tab, label, iconRes) ->
                    val isSelected = selected == tab

                    val contentColor by animateColorAsState(
                        targetValue = if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        animationSpec = tween(durationMillis = 200),
                        label = "tabIconColor_${tab.name}",
                    )
                    val textColor by animateColorAsState(
                        targetValue = if (isSelected) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        animationSpec = tween(durationMillis = 200),
                        label = "tabTextColor_${tab.name}",
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(MaterialTheme.shapes.small)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { onSelect(tab) },
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.adp, horizontal = 4.adp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                painter = painterResource(iconRes),
                                contentDescription = null,
                                tint = contentColor,
                                modifier = Modifier.size(15.adp),
                            )
                            Spacer(Modifier.width(5.adp))
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                ),
                                color = textColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FileTab(
    fileName: String?,
    isLoading: Boolean,
    ocrEnabled: Boolean,
    isOcrLocked: Boolean,
    maxPages: Int,
    maxFileSize: Int,
    onPickFile: () -> Unit,
    onClearFile: () -> Unit,
    onOcrToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.adp)) {
//        AnimatedVisibility(
//            visible = !isLoading,
//            enter   = expandVertically() + fadeIn(),
//            exit    = shrinkVertically() + fadeOut(),
//        ) {
//            OcrBanner(
//                enabled    = ocrEnabled,
//                isLocked   = isOcrLocked,
//                onToggle   = onOcrToggle,
//            )
//        }

        DropZone(
            fileName = fileName,
            isLoading = isLoading,
            maxPages = maxPages,
            maxFileSize = maxFileSize,
            onPickFile = onPickFile,
            onClearFile = onClearFile,
        )
    }
}


@Composable
private fun DropZone(
    fileName: String?,
    isLoading: Boolean,
    maxPages: Int,
    maxFileSize: Int,
    onPickFile: () -> Unit,
    onClearFile: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor = when {
        fileName != null -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
    }
    val bgColor = when {
        fileName != null -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.25f)
        else -> Color.Transparent
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(bgColor)
            .animatedDashedBorder(
                width = 2.adp,
                color = borderColor,
                shape = MaterialTheme.shapes.large
            )
            .then(
                if (fileName == null && !isLoading) Modifier.clickable(onClick = onPickFile) else Modifier
            ),
    ) {
        AnimatedContent(
            targetState = when {
                isLoading -> DropZoneState.LOADING
                fileName != null -> DropZoneState.CONFIRMED
                else -> DropZoneState.EMPTY
            },
            transitionSpec = { fadeIn() togetherWith fadeOut() }
        ) { state ->
            when (state) {
                DropZoneState.EMPTY -> DropZoneEmptyState(
                    maxPages = maxPages,
                    maxFileSize = maxFileSize
                )

                DropZoneState.LOADING -> DropZoneLoading()
                DropZoneState.CONFIRMED -> DropZoneFileConfirmed(
                    fileName = fileName ?: "",
                    onClear = onClearFile,
                )
            }
        }
    }
}

private enum class DropZoneState { EMPTY, LOADING, CONFIRMED }

@Composable
private fun DropZoneEmptyState(
    maxPages: Int,
    maxFileSize: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.adp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.adp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(64.adp)
                .clip(MaterialTheme.synapse.radius.xl)
                .background(MaterialTheme.colorScheme.primaryContainer),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_upload),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.adp),
            )
        }
        Spacer(Modifier.height(4.adp))
        Text(
            text = stringResource(R.string.drop_zone_title),
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = stringResource(R.string.drop_zone_subtitle),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(4.adp))
        Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = CircleShape) {
            Text(
                text = stringResource(R.string.drop_zone_hint, maxFileSize, maxPages),
                modifier = Modifier.padding(horizontal = 14.adp, vertical = 6.adp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun DropZoneLoading(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(40.adp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.adp),
    ) {
        LoadingIndicator()
        Text(
            text = stringResource(R.string.drop_zone_reading),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun DropZoneFileConfirmed(
    fileName: String,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.adp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.adp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(44.adp)
                .clip(MaterialTheme.synapse.radius.sm)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.synapse.semantic.success,
                            MaterialTheme.synapse.semantic.success.copy(alpha = 0.8f) // Gradient as fallback
                        )
                    )
                ),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_file_text),
                contentDescription = null,
                tint = Color.White.copy(0.9f),
                modifier = Modifier.size(19.adp),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = fileName,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = stringResource(R.string.file_type_pdf_ready),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.padding(top = 2.adp),
            )
        }
        IconButton(onClick = onClear) {
            Icon(
                painter = painterResource(R.drawable.ic_x),
                contentDescription = stringResource(R.string.cd_remove_file),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.adp),
            )
        }
    }
}

@Composable
private fun TextFieldTrailingIcons(
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean = true,
    size: Dp = 16.dp,
) {
    if (value.isNotBlank()) {
        IconButton(onClick = { onValueChange("") }) {
            Icon(
                painter = painterResource(R.drawable.ic_x),
                contentDescription = stringResource(R.string.cd_remove_file),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(size),
            )
        }
    } else {
        val clipboardManager = LocalClipboard.current
        val clipData = runBlocking { clipboardManager.getClipEntry()?.clipData }
        if (clipData != null && clipData.itemCount > 0) {
            IconButton(onClick = {
                val clipText = clipData.getItemAt(0).text.toString()
                if (enabled) onValueChange(clipText)
            }) {
                Icon(
                    painter = painterResource(R.drawable.icon_paste),
                    contentDescription = stringResource(R.string.cd_paste),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(size),
                )
            }
        }
    }
}

@Composable
fun WebTab(
    webUrl: String,
    isPro: Boolean,
    onUrlChange: (String) -> Unit,
    onLockedClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .dropShadow(
                MaterialTheme.shapes.large,
                MaterialTheme.synapse.shadows.subtle.toShadow()
            ),
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.large,
    ) {
        Column(
            modifier = Modifier.padding(20.adp),
            verticalArrangement = Arrangement.spacedBy(16.adp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.adp),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(36.adp)
                        .clip(MaterialTheme.shapes.medium)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_globe),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.adp),
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.web_tab_title),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = stringResource(R.string.web_tab_subtitle),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = webUrl,
                    onValueChange = { if (isPro) onUrlChange(it) },
                    enabled = isPro,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    placeholder = {
                        Text(
                            text = stringResource(R.string.web_tab_placeholder),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_link),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.adp),
                        )
                    },
                    trailingIcon = {
                        TextFieldTrailingIcons(
                            value = webUrl,
                            onValueChange = onUrlChange,
                            enabled = isPro,
                        )
                    },
                    shape = MaterialTheme.shapes.medium,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(0.8f),
                    ),
                    minLines = 2,
                    maxLines = 2,
                )

                if (!isPro) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onLockedClick
                            )
                    )
                }
            }

            // ── Supported formats chips ────────────────────────────────────
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.adp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                WebFormatChip(
                    iconRes = R.drawable.ic_youtube,
                    label = stringResource(R.string.web_format_youtube),
                    tint = MaterialTheme.colorScheme.error,
                    isSelected = webUrl.contains("youtu"),
                )
                WebFormatChip(
                    iconRes = R.drawable.ic_globe,
                    label = stringResource(R.string.web_format_article),
                    tint = MaterialTheme.colorScheme.primary,
                    isSelected = !webUrl.contains("youtu") && webUrl.isNotBlank(),
                )
            }
        }
    }
}

@Composable
private fun WebFormatChip(
    iconRes: Int,
    label: String,
    tint: Color,
    isSelected: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = if (isSelected) tint.copy(alpha = 0.15f) else tint.copy(alpha = 0.05f),
        shape = CircleShape,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.adp, vertical = 5.adp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.adp),
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(11.adp),
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = tint,
            )
        }
    }
}

@Composable
fun TextTab(
    text: String,
    onTextChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .dropShadow(
                MaterialTheme.shapes.large,
                MaterialTheme.synapse.shadows.subtle.toShadow()
            ),
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.large,
    ) {
        Column(
            modifier = Modifier.padding(16.adp),
            verticalArrangement = Arrangement.spacedBy(8.adp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.adp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_type),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.adp),
                )
                Text(
                    text = stringResource(R.string.text_tab_title),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            OutlinedTextField(
                value = text,
                onValueChange = { if (it.length <= TEXT_MAX_CHARS) onTextChange(it) },
                modifier = Modifier
                    .fillMaxWidth(),
                placeholder = {
                    Text(
                        text = stringResource(R.string.text_tab_placeholder),
                        style = MaterialTheme.typography.bodySmall,
                    )
                },
                trailingIcon = {
                    TextFieldTrailingIcons(
                        value = text,
                        onValueChange = onTextChange,
                        size = 24.dp,
                    )
                },
                shape = MaterialTheme.shapes.medium,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(0.8f),
                ),
                minLines = 6,
                maxLines = 14,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val atLimit = text.length >= TEXT_MAX_CHARS
                Text(
                    text = stringResource(R.string.text_tab_char_count, text.length),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (atLimit) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (text.length >= 10 && !atLimit) {
                    Text(
                        text = stringResource(R.string.text_tab_ready),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.tertiary,
                    )
                }
            }
        }
    }
}

@Composable
fun ConfigureStep(
    uiState: AddPdfUiState,
    onQuestionCountChange: (Int) -> Unit,
    onTypeToggle: (QuestionType) -> Unit,
    onFocusNotesChange: (String) -> Unit,
    onLanguageClick: () -> Unit,
    onThinkingToggle: () -> Unit,
    onNavigateToPremium: () -> Unit,
    onGenerate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedLang = remember(uiState.language) {
        LANGUAGES.find { it.code == uiState.language } ?: LANGUAGES[0]
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.adp)) {

        // Pill showing which source was confirmed in Step 1.
        SourceConfirmedPill(
            text = when (uiState.sourceTab) {
                SourceTab.FILE ->
                    stringResource(R.string.configure_source_file, uiState.fileName ?: "File")

                SourceTab.TEXT ->
                    stringResource(R.string.configure_source_text, uiState.pasteText.length)

                SourceTab.WEB, SourceTab.YOUTUBE ->
                    stringResource(R.string.configure_source_web)
            }
        )

        ThinkingBanner(
            enabled = uiState.thinkingEnabled,
            isLocked = uiState.isThinkingLocked,
            onToggle = onThinkingToggle,
            onNavigateToPremium = onNavigateToPremium,
        )

        // Question count slider.
        QuestionCountCard(count = uiState.questionCount, onChange = onQuestionCountChange)

        // Question type filter chips.
        QuestionTypesCard(selected = uiState.selectedTypes, onToggle = onTypeToggle)

        // Optional AI focus notes field.
        AiFocusNotesCard(notes = uiState.focusNotes, onNotesChange = onFocusNotesChange)

        // Language picker.
        LanguagePickerCard(selectedLanguage = selectedLang, onClick = onLanguageClick)

        // Deep Thinking toggle — replaces the old OCR banner placement here.
        // Lock state comes from AppConfigProvider.isThinkingLocked (set in ViewModel init).

        // Primary CTA — starts generation.
        PrimaryGradientButton(
            text = stringResource(R.string.configure_generate),
            iconRes = R.drawable.ic_sparkles,
            enabled = true,
            onClick = onGenerate,
        )
    }
}

@Composable
fun SourceConfirmedPill(text: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = MaterialTheme.shapes.medium,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.adp, vertical = 10.adp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.adp),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_check_circle_2),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(14.adp),
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
fun QuestionCountCard(count: Int, onChange: (Int) -> Unit, modifier: Modifier = Modifier) {
    SectionCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SectionLabel(text = stringResource(R.string.configure_questions_label))
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    text = "$count",
                    modifier = Modifier.padding(horizontal = 12.adp, vertical = 4.adp),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
        Slider(
            value = count.toFloat(),
            onValueChange = { onChange(it.toInt()) },
            valueRange = 5f..50f,
            steps = 44,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.adp),
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuestionTypesCard(
    selected: Set<QuestionType>,
    onToggle: (QuestionType) -> Unit,
    modifier: Modifier = Modifier
) {
    SectionCard(modifier = modifier) {
        SectionLabel(
            text = stringResource(R.string.configure_types_label)
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.adp),
            verticalArrangement = Arrangement.spacedBy(8.adp),
        ) {
            listOf(
                Triple(QuestionType.MCQ, stringResource(R.string.type_mcq), R.drawable.ic_brain),
                Triple(
                    QuestionType.TRUE_FALSE,
                    stringResource(R.string.type_true_false),
                    R.drawable.ic_toggle_left
                ),
                Triple(
                    QuestionType.FLASHCARD,
                    stringResource(R.string.type_flashcard),
                    R.drawable.ic_book_open
                ),
            ).forEach { (type, label, iconRes) ->
                val isOn = type in selected
                FilterChip(
                    selected = isOn,
                    onClick = { onToggle(type) },
                    label = {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = if (isOn) FontWeight.Bold else FontWeight.Normal
                            ),
                        )
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(iconRes),
                            contentDescription = null,
                            modifier = Modifier.size(13.adp)
                        )
                    },
                    shape = CircleShape,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.primary,
                        selectedLeadingIconColor = MaterialTheme.colorScheme.primary,
                    ),
                )
            }
        }
    }
}

@Composable
fun AiFocusNotesCard(
    notes: String,
    onNotesChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    SectionCard(modifier = modifier) {

        SectionLabel(text = stringResource(R.string.configure_focus_optional))

        OutlinedTextField(
            value = notes,
            onValueChange = onNotesChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = stringResource(R.string.configure_focus_placeholder),
                    style = MaterialTheme.typography.bodySmall
                )
            },
            trailingIcon = {
                if (notes.isNotBlank()) {
                    IconButton(onClick = { onNotesChange("") }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_x),
                            contentDescription = stringResource(R.string.cd_remove_file),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.adp),
                        )
                    }
                } else {
                    val clipboardManager = LocalClipboard.current
                    val clipData = runBlocking { clipboardManager.getClipEntry()?.clipData }
                    if (clipData != null && clipData.itemCount > 0) {
                        val clipText = clipData.getItemAt(0).text.toString()
                        IconButton(onClick = {
                            onNotesChange(clipText)
                        }) {
                            Icon(
                                painter = painterResource(R.drawable.icon_paste),
                                contentDescription = stringResource(R.string.cd_paste),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.adp),
                            )
                        }
                    }
                }
            },
            shape = MaterialTheme.shapes.medium,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(0.8f),
            ),
            minLines = 3,
            maxLines = 8,
        )
    }
}

@Composable
fun LanguagePickerCard(
    selectedLanguage: LanguageOption,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    SectionCard(modifier = modifier) {
        SectionLabel(
            text = stringResource(R.string.configure_language_label)
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = MaterialTheme.shapes.medium,
            onClick = onClick
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.adp, vertical = 12.adp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.adp),
            ) {
                Text(
                    text = selectedLanguage.label(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_chevron_down),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.adp)
                    )
                }
            }
        }
    }
}

@Composable
fun GeneratingStep(
    progress: Float,
    questionCount: Int,
    language: String,
    focusNotesActive: Boolean,
    modifier: Modifier = Modifier,
) {
    val animatedProgress by animateFloatAsState(targetValue = progress, animationSpec = tween(280))

    val taskAnalysing = stringResource(R.string.generating_task_analysing)
    val taskGenerating = stringResource(R.string.generating_task_generating)
    val taskAnswers = stringResource(R.string.generating_task_answers)
    val taskSrs = stringResource(R.string.generating_task_srs)

    val tasks = remember(progress) {
        listOf(
            GenerationTask(taskAnalysing, progress > 0.20f),
            GenerationTask(taskGenerating, progress > 0.50f),
            GenerationTask(taskAnswers, progress > 0.75f),
            GenerationTask(taskSrs, progress >= 1.00f),
        )
    }

    Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(24.adp))
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(120.adp)) {
            LoadingIndicator(size = 120.adp)
        }
        Spacer(Modifier.height(28.adp))
        Text(
            text = stringResource(R.string.generating_title),
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.adp))
        val primaryColor = MaterialTheme.colorScheme.primary
        val subtitleColor = MaterialTheme.colorScheme.onSurfaceVariant
        Text(
            text = buildAnnotatedString {
                append(stringResource(R.string.generating_subtitle_prefix))
                withStyle(
                    SpanStyle(
                        color = primaryColor,
                        fontWeight = FontWeight.SemiBold
                    )
                ) { append("$questionCount") }
                append(stringResource(R.string.generating_subtitle_in))
                withStyle(
                    SpanStyle(
                        color = primaryColor,
                        fontWeight = FontWeight.SemiBold
                    )
                ) { append(language) }
                if (focusNotesActive) append(stringResource(R.string.generating_subtitle_focus))
            },
            style = MaterialTheme.typography.bodyMedium,
            color = subtitleColor,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(28.adp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.adp), horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.generating_progress_label),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${
                    (animatedProgress * 100).toInt().localized()
                } ${stringResource(R.string.percent_mark)}",
                style = MaterialTheme.typography
                    .bodySmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
        }
        WavyProgressIndicator(animatedProgress)
        Spacer(Modifier.height(16.adp))
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.adp)
        ) {
            tasks.forEach { task -> GeneratingTaskRow(label = task.label, done = task.done) }
        }
    }
}

@Composable
fun GeneratingTaskRow(
    label: String,
    done: Boolean,
    modifier: Modifier = Modifier
) {
    val semantic = MaterialTheme.synapse.semantic
    val cs = MaterialTheme.colorScheme

    val bgColor = if (done) semantic.primaryBg else cs.surface
    val borderColor = if (done) semantic.primaryBorder else cs.outlineVariant.copy(alpha = 0.5f)
    val contentColor = if (done) semantic.primary else cs.onSurfaceVariant

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = bgColor,
        shape = MaterialTheme.shapes.medium,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.adp, vertical = 12.adp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.adp)
        ) {
            if (done) {
                Icon(
                    painter = painterResource(R.drawable.ic_badge_check),
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(MaterialTheme.synapse.spacing.icon_sm)
                )
            } else {
                Box(
                    modifier = Modifier.size(MaterialTheme.synapse.spacing.icon_sm),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingIndicator(size = 18.adp)
                }
            }

            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (done) FontWeight.Bold else FontWeight.Medium
                ),
                color = contentColor,
            )
        }
    }
}
@Composable
fun DoneStep(
    packName: String,
    sourceDescription: String,
    questionCount: Int,
    language: LanguageOption,
    generatedQuestions: List<QuestionUiModel>,
    onStartStudying: () -> Unit,
    onBackToDashboard: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.adp)) {
        Spacer(Modifier.height(8.adp))

        DoneSuccessHeader(packName = packName, sourceDescription = sourceDescription, questionCount = questionCount, language = language.label())
        DoneStatsRow(questionCount = questionCount, language = language.label())

        // Preview only rendered with real data — no mock fallback
        if (generatedQuestions.isNotEmpty()) {
            Text(
                text = stringResource(R.string.done_preview_label),
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = modifier.padding(start = 2.adp)
            )
            generatedQuestions.take(3).forEachIndexed { i, q ->
                PreviewQuestionCard(
                    typeLabel = q.type.name.replace("_", "/"),
                    emoji = when (q.type) {
                        QuestionType.MCQ -> "🧠"
                        QuestionType.TRUE_FALSE -> "✅"
                        QuestionType.FLASHCARD -> "📚"
                    },
                    questionText = q.questionText,
                    index = i,
                )
            }

            if (questionCount > 3) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animatedDashedBorder(
                            MaterialTheme
                                .colorScheme.primary.copy(alpha = 0.8f),
                            shape = MaterialTheme.shapes.medium
                        ),
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Text(
                        text = stringResource(R.string.done_more_questions, questionCount - 3),
                        modifier = Modifier.padding(14.adp),
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }

        PrimaryGradientButton(
            text = stringResource(R.string.done_start_studying),
            iconRes = R.drawable.ic_zap,
            enabled = true,
            onClick = onStartStudying
        )
        SecondaryButton(
            text = stringResource(R.string.done_back_to_dashboard),
            onClick = onBackToDashboard
        )
        Spacer(Modifier.height(8.adp))
    }
}

@Composable
fun DoneSuccessHeader(
    packName: String,
    sourceDescription: String,
    questionCount: Int,
    language: String,
    modifier: Modifier = Modifier
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        AnimatedVisibility(
            visible = visible,
            enter = scaleIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + fadeIn()
        ) {
            StatusIconHeader(
                iconRes = R.drawable.ic_check,
                iconCd = R.string.done_title,
                accentColor = MaterialTheme.synapse.semantic.success
            )
        }
        Spacer(Modifier.height(16.adp))
        Text(
            text = stringResource(R.string.done_title),
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(8.adp))
        val primaryColor = MaterialTheme.colorScheme.primary
        val subtitleColor = MaterialTheme.colorScheme.onSurfaceVariant
        val source = sourceDescription.takeIf { it.isNotBlank() }
        Text(
            text = buildAnnotatedString {
                withStyle(
                    SpanStyle(
                        color = primaryColor,
                        fontWeight = FontWeight.Bold
                    )
                ) { append(" $packName ") }
                append(stringResource(R.string.done_subtitle_ready))
                source?.let {
                    append(" ")
                    withStyle(SpanStyle(fontWeight = FontWeight.Normal, color = subtitleColor)) {
                        append("($it)")
                    }
                }
                withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) {
                    append(
                        " ${
                            stringResource(
                                R.string.done_subtitle_questions,
                                questionCount
                            )
                        }"
                    )
                }
                append(" ${stringResource(R.string.done_subtitle_in, language)}")
            },
            style = MaterialTheme.typography.bodyMedium,
            color = subtitleColor,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun DoneStatsRow(questionCount: Int, language: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.adp)) {
        StatChip(
            value = "$questionCount",
            label = stringResource(R.string.stat_questions),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )
        StatChip(
            value = language,
            label = stringResource(R.string.stat_language),
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun StatChip(value: String, label: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = color.copy(alpha = 0.12f),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(12.adp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.adp)
            )
        }
    }
}

@Composable
fun PreviewQuestionCard(
    typeLabel: String,
    emoji: String,
    questionText: String,
    index: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .dropShadow(
                MaterialTheme.shapes.medium,
                MaterialTheme.synapse.shadows.subtle.toShadow()
            ), shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(16.adp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.adp),
                modifier = Modifier.padding(bottom = 8.adp)
            ) {
                Text(text = emoji)
                Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = CircleShape) {
                    Text(
                        text = typeLabel.uppercase(),
                        modifier = Modifier.padding(horizontal = 8.adp, vertical = 2.adp),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                Text(
                    text = "Q${index + 1}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = questionText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageBottomSheet(
    selectedCode: String,
    isPro: Boolean,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
    onUpgrade: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val freeCount = 5
    val visibleLanguages = if (isPro) LANGUAGES else LANGUAGES.take(freeCount)
    val lockedLanguages = if (isPro) emptyList() else LANGUAGES.drop(freeCount)
    val hasLocked = !isPro && lockedLanguages.any { it.code != selectedCode }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier,
        shape = MaterialTheme.synapse.radius.xl,
        containerColor = MaterialTheme.colorScheme.surface,
        scrimColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.adp)
                .padding(bottom = 12.adp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.adp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_globe),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.adp)
            )
            Text(
                text = stringResource(R.string.language_sheet_title),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Surface(
                onClick = onDismiss,
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(30.adp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(R.drawable.ic_x),
                        contentDescription = stringResource(R.string.cd_close),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.adp)
                    )
                }
            }
        }
        HorizontalDivider()
        LazyColumn(modifier = Modifier.navigationBarsPadding()) {
            items(visibleLanguages, key = { it.code }) { lang ->
                val isSelected = selectedCode == lang.code
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                    onClick = { onSelect(lang.code) }) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.adp, vertical = 14.adp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.adp)
                    ) {
                        Text(
                            text = lang.label(),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        if (isSelected) Icon(
                            painter = painterResource(R.drawable.ic_check),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(15.adp)
                        )
                    }
                }
            }
            if (hasLocked) {
                item(key = "locked_header") {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(0.5f)
                    ) {
                        Text(
                            text = stringResource(R.string.language_premium_locked),
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 20.adp, vertical = 12.adp),
                        )
                    }
                }
                items(lockedLanguages, key = { "locked_${it.code}" }) { lang ->
                    val isSelected = selectedCode == lang.code
                    val isLocked = !isPro
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                        onClick = { if (isLocked) onUpgrade() else onSelect(lang.code) },
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 20.adp, vertical = 14.adp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.adp),
                        ) {
                            Text(
                                text = lang.label(),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal),
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                    0.6f
                                ),
                                modifier = Modifier.weight(1f),
                            )
                            Icon(
                                painter = painterResource(R.drawable.ic_lock),
                                contentDescription = null,
                                tint = MaterialTheme.synapse.semantic.gold,
                                modifier = Modifier.size(18.adp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SectionCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .dropShadow(
                MaterialTheme.shapes.large,
                MaterialTheme.synapse.shadows.subtle.toShadow()
            ), shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(20.adp), content = content)
    }
}

@Composable
private fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(end = 2.adp, bottom = 12.adp)
    )
}

@Preview(name = "Header Light", showBackground = true)
@Composable
private fun AddPdfHeaderPreview() {
    SynapseTheme { AddPdfHeader(onBack = {}, modifier = Modifier.padding(20.adp)) }
}

@Preview(name = "StepIndicator Light", showBackground = true)
@Composable
private fun StepIndicatorPreview() {
    SynapseTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            StepIndicator(
                currentIndex = 1, modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.adp)
            )
        }
    }
}

@Preview(name = "WebTab", showBackground = true)
@Composable
private fun WebTabPreview() {
    SynapseTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            WebTab(
                webUrl = "",
                isPro = true,
                onLockedClick = {},
                onUrlChange = {},
                modifier = Modifier.padding(16.adp)
            )
        }
    }
}

@Preview(name = "GeneratingStep Light", showBackground = true)
@Composable
private fun GeneratingStepPreview() {
    SynapseTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            GeneratingStep(
                progress = 0.65f,
                questionCount = 20,
                language = "English",
                focusNotesActive = true,
                modifier = Modifier.padding(20.adp)
            )
        }
    }
}