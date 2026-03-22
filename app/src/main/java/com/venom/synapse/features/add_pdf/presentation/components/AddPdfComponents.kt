package com.venom.synapse.features.add_pdf.presentation.components

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.venom.synapse.R
import com.venom.synapse.core.theme.SynapseTheme
import com.venom.synapse.core.theme.synapse
import com.venom.synapse.core.theme.tokens.BrandColors
import com.venom.synapse.core.theme.tokens.Elevation
import com.venom.synapse.core.theme.tokens.Radius
import com.venom.synapse.core.theme.tokens.Radius.RadiusXXL
import com.venom.synapse.core.ui.components.PrimaryGradientButton
import com.venom.synapse.core.ui.components.ProBadge
import com.venom.synapse.core.ui.components.SecondaryButton
import com.venom.synapse.core.ui.state.QuestionUiModel
import com.venom.synapse.core.ui.utils.animatedDashedBorder
import com.venom.synapse.domain.model.QuestionType
import com.venom.synapse.features.add_pdf.presentation.state.AddPdfUiState
import com.venom.synapse.features.add_pdf.presentation.state.SourceTab
import com.venom.ui.components.common.adp



data class LanguageOption(val code: String, val label: String, val flag: String)

val LANGUAGES = listOf(
    LanguageOption("en", "English",    "🇺🇸"),
    LanguageOption("es", "Spanish",    "🇪🇸"),
    LanguageOption("fr", "French",     "🇫🇷"),
    LanguageOption("de", "German",     "🇩🇪"),
    LanguageOption("pt", "Portuguese", "🇧🇷"),
    LanguageOption("it", "Italian",    "🇮🇹"),
    LanguageOption("ja", "Japanese",   "🇯🇵"),
    LanguageOption("ko", "Korean",     "🇰🇷"),
    LanguageOption("zh", "Chinese",    "🇨🇳"),
    LanguageOption("ar", "Arabic",     "🇸🇦"),
)

data class MockQuestion(val type: String, val emoji: String, val text: String)

val MOCK_PREVIEW = listOf(
    MockQuestion("MCQ",        "🧠", "What is the primary mechanism behind backpropagation in neural networks?"),
    MockQuestion("Flashcard",  "📚", "Define 'gradient descent' and explain its role in model training."),
    MockQuestion("True/False", "✅", "Convolutional layers share weights across spatial positions, reducing parameter count."),
)

// Moved outside composables to avoid per-recomposition allocation
private data class GenerationTask(val label: String, val done: Boolean)

fun LanguageOption.shortLabel() = label.take(3).uppercase()

@Composable
fun AddPdfHeader(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.adp),
    ) {
        Surface(
            onClick = onBack,
            shape = Radius.ShapeMedium,
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.size(40.adp),
            shadowElevation = Elevation.Level1,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = stringResource(R.string.cd_back),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.adp),
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.add_pdf_title),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = stringResource(R.string.add_pdf_subtitle),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // "AI Ready" pill
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.adp, vertical = 6.adp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.adp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_sparkles),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(12.adp),
                )
                Text(
                    text = stringResource(R.string.add_pdf_ai_ready),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
fun StepIndicator(
    currentIndex: Int,
    modifier: Modifier = Modifier,
) {
    // Step labels via string resources — supports localisation/RTL
    val stepLabels = listOf(
        stringResource(R.string.step_upload),
        stringResource(R.string.step_configure),
        stringResource(R.string.step_generate),
        stringResource(R.string.step_done),
    )

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        stepLabels.forEachIndexed { index, label ->
            val isActive = index == currentIndex
            val isDone   = index < currentIndex

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.adp),
            ) {
                // Step bubble
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(22.adp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isDone   -> MaterialTheme.colorScheme.primary
                                isActive -> Color.Transparent
                                else     -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        )
                        .then(
                            if (isActive) Modifier.border(2.adp, MaterialTheme.colorScheme.primary, CircleShape)
                            else Modifier
                        ),
                ) {
                    if (isDone) {
                        Icon(
                            painter = painterResource(R.drawable.ic_check),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(11.adp),
                        )
                    } else {
                        Text(
                            text = "${index + 1}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                            ),
                            color = if (isActive) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                    ),
                    color = when {
                        isActive -> MaterialTheme.colorScheme.primary
                        isDone   -> MaterialTheme.colorScheme.onSurfaceVariant
                        else     -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                    },
                )
            }

            // Connector line between steps
            if (index < stepLabels.lastIndex) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.adp)
                        .height(1.5.adp)
                        .clip(CircleShape)
                        .background(
                            if (index < currentIndex) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outlineVariant
                        ),
                )
            }
        }
    }
}

@Composable
fun UploadStep(
    uiState: AddPdfUiState,
    onTabSelect: (SourceTab) -> Unit,
    onPickFile: () -> Unit,
    onClearFile: () -> Unit,
    onOcrToggle: () -> Unit,
    onPasteTextChange: (String) -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val canProceed by remember(uiState.sourceTab, uiState.fileName, uiState.pasteText, uiState.isLoading) {
        derivedStateOf {
            when (uiState.sourceTab) {
                SourceTab.FILE -> uiState.fileName != null && !uiState.isLoading
                SourceTab.TEXT -> uiState.pasteText.length >= 10
                SourceTab.WEB  -> false // Pro-locked: always disabled
            }
        }
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.adp),
    ) {
        SourceTabRow(selected = uiState.sourceTab, onSelect = onTabSelect)

        AnimatedContent(
            targetState = uiState.sourceTab,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "source_tab",
        ) { tab ->
            when (tab) {
                SourceTab.FILE -> FileTab(
                    fileName      = uiState.fileName,
                    isImageUpload = uiState.isImageUpload,
                    isLoading     = uiState.isLoading,
                    ocrEnabled    = uiState.ocrEnabled,
                    onPickFile    = onPickFile,
                    onClearFile   = onClearFile,
                    onOcrToggle   = onOcrToggle,
                )
                SourceTab.WEB  -> WebTab()
                SourceTab.TEXT -> TextTab(
                    text         = uiState.pasteText,
                    onTextChange = onPasteTextChange,
                )
            }
        }

        PrimaryGradientButton(
            text    = stringResource(R.string.add_pdf_continue),
            icon    = Icons.AutoMirrored.Rounded.ArrowForwardIos,
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
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = Radius.ShapeLarge,
        shadowElevation = Elevation.Level1,
    ) {
        Row(modifier = Modifier.padding(4.adp)) {
            listOf(
                Triple(SourceTab.FILE, stringResource(R.string.tab_file_image),  R.drawable.ic_file_image),
                Triple(SourceTab.WEB,  stringResource(R.string.tab_web_youtube), R.drawable.ic_youtube),
                Triple(SourceTab.TEXT, stringResource(R.string.tab_plain_text),  R.drawable.ic_type),
            ).forEach { (tab, label, iconRes) ->
                val isSelected = selected == tab
                Surface(
                    modifier        = Modifier.weight(1f),
                    color           = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
                    shape           = Radius.ShapeMedium,
                    shadowElevation = if (isSelected) Elevation.Level2 else Elevation.Level0,
                    onClick         = { onSelect(tab) },
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 10.adp, horizontal = 2.adp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            painter = painterResource(iconRes),
                            contentDescription = null,
                            tint = if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(13.adp),
                        )
                        Spacer(Modifier.width(4.adp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                fontSize = 11.sp,
                            ),
                            color = if (isSelected) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FileTab(
    fileName: String?,
    isImageUpload: Boolean,
    isLoading: Boolean,
    ocrEnabled: Boolean,
    onPickFile: () -> Unit,
    onClearFile: () -> Unit,
    onOcrToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.adp)) {
        DropZone(
            fileName      = fileName,
            isImageUpload = isImageUpload,
            isLoading     = isLoading,
            onPickFile    = onPickFile,
            onClearFile   = onClearFile,
        )

        // OCR banner — only shown for image uploads
        AnimatedVisibility(
            visible = isImageUpload,
            enter   = expandVertically() + fadeIn(),
            exit    = shrinkVertically() + fadeOut(),
        ) {
            OcrBanner(enabled = ocrEnabled, onToggle = onOcrToggle)
        }
    }
}

@Composable
private fun DropZone(
    fileName: String?,
    isImageUpload: Boolean,
    isLoading: Boolean,
    onPickFile: () -> Unit,
    onClearFile: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor = when {
        fileName != null -> MaterialTheme.colorScheme.tertiary
        else             -> MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
    }
    val bgColor = when {
        fileName != null -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.25f)
        else             -> Color.Transparent
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(Radius.ShapeXXL)
            .background(bgColor)
            .animatedDashedBorder(
                width  = 2.adp,
                color  = borderColor,
                radius = RadiusXXL,
            )
            .then(
                if (fileName == null && !isLoading)
                    Modifier.clickable(onClick = onPickFile)
                else Modifier
            ),
    ) {
        AnimatedContent(
            targetState = when {
                isLoading        -> DropZoneState.LOADING
                fileName != null -> DropZoneState.CONFIRMED
                else             -> DropZoneState.EMPTY
            },
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "drop_zone_state",
        ) { state ->
            when (state) {
                DropZoneState.EMPTY     -> DropZoneEmptyState()
                DropZoneState.LOADING   -> DropZoneLoading()
                DropZoneState.CONFIRMED -> DropZoneFileConfirmed(
                    fileName      = fileName ?: "",
                    isImageUpload = isImageUpload,
                    onClear       = onClearFile,
                )
            }
        }
    }
}

private enum class DropZoneState { EMPTY, LOADING, CONFIRMED }

@Composable
private fun DropZoneEmptyState(modifier: Modifier = Modifier) {
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
                .clip(Radius.ShapeXL)
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

        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = CircleShape,
        ) {
            Text(
                text = stringResource(R.string.drop_zone_hint),
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
        CircularProgressIndicator(
            modifier    = Modifier.size(36.adp),
            color       = MaterialTheme.colorScheme.primary,
            strokeWidth = 3.adp,
        )
        Text(
            text  = stringResource(R.string.drop_zone_reading),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun DropZoneFileConfirmed(
    fileName: String,
    isImageUpload: Boolean,
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
        // File icon — uses brand gradient from design token system (not raw hex)
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(44.adp)
                .clip(Radius.ShapeMedium)
                .background(
                    Brush.linearGradient(
                        colors = if (isImageUpload)
                            listOf(BrandColors.BrandPrimaryDeep, BrandColors.BrandPrimaryBright)
                        else
                            listOf(BrandColors.BrandSuccessLight, BrandColors.BrandSuccessDark),
                    )
                ),
        ) {
            Icon(
                painter = painterResource(
                    if (isImageUpload) R.drawable.ic_file_image else R.drawable.ic_file_text
                ),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(19.adp),
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text     = fileName,
                style    = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color    = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text     = if (isImageUpload) stringResource(R.string.file_type_image_ready)
                else stringResource(R.string.file_type_pdf_ready),
                style    = MaterialTheme.typography.labelSmall,
                color    = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.padding(top = 2.adp),
            )
        }

        IconButton(
            onClick  = onClear,
            modifier = Modifier
                .size(28.adp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_x),
                contentDescription = stringResource(R.string.cd_remove_file),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(13.adp),
            )
        }
    }
}

@Composable
fun OcrBanner(
    enabled: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color    = MaterialTheme.colorScheme.secondaryContainer,
        shape    = Radius.ShapeLarge,
    ) {
        Row(
            modifier = Modifier.padding(12.adp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.adp),
        ) {
            // Scan icon with brand primary gradient
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(36.adp)
                    .clip(Radius.ShapeMedium)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                BrandColors.BrandPrimaryDeep,
                                BrandColors.BrandPrimaryBright,
                            )
                        )
                    ),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_scan_text),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(16.adp),
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text  = stringResource(R.string.ocr_title),
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.adp),
                    modifier = Modifier.padding(top = 2.adp),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_crown),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(9.adp),
                    )
                    Text(
                        text  = stringResource(R.string.ocr_subtitle),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.adp),
            ) {
                Switch(checked = enabled, onCheckedChange = { onToggle() })
                Text(
                    text  = if (enabled) stringResource(R.string.toggle_on)
                    else stringResource(R.string.toggle_off),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                    ),
                    color = if (enabled) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
fun WebTab(modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.adp)) {
        LockedWebCard(
            title       = stringResource(R.string.web_youtube_title),
            placeholder = stringResource(R.string.web_youtube_placeholder),
            iconRes     = R.drawable.ic_youtube,
            iconTint    = MaterialTheme.colorScheme.error,
            description = stringResource(R.string.web_youtube_description),
        )
        LockedWebCard(
            title       = stringResource(R.string.web_url_title),
            placeholder = stringResource(R.string.web_url_placeholder),
            iconRes     = R.drawable.ic_globe,
            iconTint    = MaterialTheme.colorScheme.primary,
            description = stringResource(R.string.web_url_description),
        )
    }
}

@Composable
private fun LockedWebCard(
    title: String,
    placeholder: String,
    iconRes: Int,
    iconTint: Color,
    description: String,
    modifier: Modifier = Modifier,
) {
    // Amber border signals premium/locked using M3 tertiary role (gold in Synapse)
    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
        shape    = Radius.ShapeXXL,
        border   = BorderStroke(1.adp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f)),
    ) {
        Column(
            modifier = Modifier.padding(20.adp),
            verticalArrangement = Arrangement.spacedBy(12.adp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.adp),
            ) {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(15.adp),
                )
                Text(
                    text     = title,
                    style    = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color    = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                ProBadge()
            }

            // Disabled input row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(Radius.ShapeMedium)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 16.adp, vertical = 12.adp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text     = placeholder,
                    style    = MaterialTheme.typography.bodySmall,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    painter = painterResource(R.drawable.ic_lock),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(14.adp),
                )
            }

            Text(
                text  = description,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
    ElevatedCard(
        modifier  = modifier.fillMaxWidth(),
        shape     = Radius.ShapeXXL,
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = Elevation.Level1),
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
                    text  = stringResource(R.string.text_tab_title),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            OutlinedTextField(
                value         = text,
                onValueChange = onTextChange,
                modifier      = Modifier.fillMaxWidth(),
                placeholder   = {
                    Text(
                        text  = stringResource(R.string.text_tab_placeholder),
                        style = MaterialTheme.typography.bodySmall,
                    )
                },
                minLines = 8,
                shape    = Radius.ShapeMedium,
                colors   = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                ),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text  = stringResource(R.string.text_tab_char_count, text.length),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (text.length >= 10) {
                    Text(
                        text  = stringResource(R.string.text_tab_ready),
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
    onGenerate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedLang = remember(uiState.language) {
        LANGUAGES.find { it.code == uiState.language } ?: LANGUAGES[0]
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.adp),
    ) {
        SourceConfirmedPill(
            text = when (uiState.sourceTab) {
                SourceTab.FILE -> stringResource(R.string.configure_source_file, uiState.fileName ?: "File")
                SourceTab.TEXT -> stringResource(R.string.configure_source_text, uiState.pasteText.length)
                SourceTab.WEB  -> stringResource(R.string.configure_source_web, uiState.webUrl.ifBlank { "—" })
            }
        )

        QuestionCountCard(count = uiState.questionCount, onChange = onQuestionCountChange)
        QuestionTypesCard(selected = uiState.selectedTypes, onToggle = onTypeToggle)
        AiFocusNotesCard(notes = uiState.focusNotes, onNotesChange = onFocusNotesChange)
        LanguagePickerCard(selectedLanguage = selectedLang, onClick = onLanguageClick)

        PrimaryGradientButton(
            text    = stringResource(R.string.configure_generate),
            iconRes = R.drawable.ic_sparkles,
            enabled = true,
            onClick = onGenerate,
        )
    }
}

@Composable
fun SourceConfirmedPill(
    text: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color    = MaterialTheme.colorScheme.tertiaryContainer,
        shape    = Radius.ShapeLarge,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.adp, vertical = 10.adp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.adp),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_check_circle_2),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(14.adp),
            )
            Text(
                text     = text,
                style    = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                color    = MaterialTheme.colorScheme.tertiary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
fun QuestionCountCard(
    count: Int,
    onChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    SectionCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SectionLabel(text = stringResource(R.string.configure_questions_label))
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = Radius.ShapeMedium,
            ) {
                Text(
                    text     = "$count",
                    modifier = Modifier.padding(horizontal = 12.adp, vertical = 4.adp),
                    style    = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color    = MaterialTheme.colorScheme.primary,
                )
            }
        }

        Slider(
            value         = count.toFloat(),
            onValueChange = { onChange(it.toInt()) },
            valueRange    = 5f..50f,
            steps         = 44,
            modifier      = Modifier
                .fillMaxWidth()
                .padding(top = 8.adp),
            colors        = SliderDefaults.colors(
                thumbColor         = MaterialTheme.colorScheme.primary,
                activeTrackColor   = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text  = stringResource(R.string.configure_questions_min),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text  = stringResource(R.string.configure_questions_max),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuestionTypesCard(
    selected: Set<QuestionType>,
    onToggle: (QuestionType) -> Unit,
    modifier: Modifier = Modifier,
) {
    SectionCard(modifier = modifier) {
        SectionLabel(
            text     = stringResource(R.string.configure_types_label),
            modifier = Modifier.padding(bottom = 12.adp),
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.adp),
            verticalArrangement   = Arrangement.spacedBy(8.adp),
        ) {
            listOf(
                Triple(QuestionType.MCQ,       stringResource(R.string.type_mcq),        R.drawable.ic_brain),
                Triple(QuestionType.TRUE_FALSE, stringResource(R.string.type_true_false), R.drawable.ic_toggle_left),
                Triple(QuestionType.FLASHCARD,  stringResource(R.string.type_flashcard),  R.drawable.ic_book_open),
            ).forEach { (type, label, iconRes) ->
                val isOn = type in selected
                FilterChip(
                    selected = isOn,
                    onClick  = { onToggle(type) },
                    label    = {
                        Text(
                            text  = label,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = if (isOn) FontWeight.Bold else FontWeight.Normal,
                            ),
                        )
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(iconRes),
                            contentDescription = null,
                            modifier = Modifier.size(13.adp),
                        )
                    },
                    shape  = CircleShape,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor   = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor       = MaterialTheme.colorScheme.primary,
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
    modifier: Modifier = Modifier,
) {
    SectionCard(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.adp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.adp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_graduation_cap),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(15.adp),
                )
                SectionLabel(text = stringResource(R.string.configure_focus_label))
            }
            Text(
                text  = stringResource(R.string.configure_focus_optional),
                style = MaterialTheme.typography.labelSmall.copy(fontStyle = FontStyle.Italic),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        OutlinedTextField(
            value         = notes,
            onValueChange = onNotesChange,
            modifier      = Modifier.fillMaxWidth(),
            placeholder   = {
                Text(
                    text  = stringResource(R.string.configure_focus_placeholder),
                    style = MaterialTheme.typography.bodySmall,
                )
            },
            minLines = 3,
            shape    = Radius.ShapeMedium,
            colors   = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            ),
        )

        Text(
            text     = stringResource(R.string.configure_focus_hint),
            style    = MaterialTheme.typography.labelSmall,
            color    = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 6.adp),
        )
    }
}

@Composable
fun LanguagePickerCard(
    selectedLanguage: LanguageOption,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SectionCard(modifier = modifier) {
        SectionLabel(
            text     = stringResource(R.string.configure_language_label),
            modifier = Modifier.padding(bottom = 10.adp),
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color    = MaterialTheme.colorScheme.surfaceVariant,
            shape    = Radius.ShapeMedium,
            onClick  = onClick,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.adp, vertical = 12.adp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.adp),
            ) {
                Text(text = selectedLanguage.flag, fontSize = 18.sp)
                Text(
                    text     = selectedLanguage.label,
                    style    = MaterialTheme.typography.bodyMedium,
                    color    = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.adp),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_globe),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(12.adp),
                    )
                    Icon(
                        painter = painterResource(R.drawable.ic_chevron_down),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.adp),
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
    val animatedProgress by animateFloatAsState(
        targetValue   = progress,
        animationSpec = tween(durationMillis = 280),
        label         = "gen_progress",
    )

    val infiniteTransition = rememberInfiniteTransition(label = "gen_anim")
    val rotation by infiniteTransition.animateFloat(
        initialValue  = 0f,
        targetValue   = 360f,
        animationSpec = infiniteRepeatable(tween(7_000, easing = LinearEasing)),
        label         = "icon_rotation",
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue  = 0.25f,
        targetValue   = 0f,
        animationSpec = infiniteRepeatable(
            animation  = tween(2_500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "glow_alpha",
    )

    // Resolve string resources before remember — can't call @Composable inside remember lambda
    val taskAnalysing = stringResource(R.string.generating_task_analysing)
    val taskGenerating = stringResource(R.string.generating_task_generating)
    val taskAnswers = stringResource(R.string.generating_task_answers)
    val taskSrs = stringResource(R.string.generating_task_srs)

    val tasks = remember(progress) {
        listOf(
            GenerationTask(label = taskAnalysing,  done = progress > 0.20f),
            GenerationTask(label = taskGenerating, done = progress > 0.50f),
            GenerationTask(label = taskAnswers,    done = progress > 0.75f),
            GenerationTask(label = taskSrs,        done = progress >= 1.00f),
        )
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(24.adp))

        // Animated Sparkles icon with radial glow
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(120.adp)) {
            Box(
                modifier = Modifier
                    .size(120.adp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = pulseAlpha),
                                Color.Transparent,
                            )
                        )
                    ),
            )
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(88.adp)
                    .rotate(rotation)
                    .clip(Radius.ShapeXXL)
                    .background(MaterialTheme.synapse.gradients.accent),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_sparkles),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(38.adp),
                )
            }
        }

        Spacer(Modifier.height(28.adp))

        Text(
            text      = stringResource(R.string.generating_title),
            style     = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
            color     = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(8.adp))

        val primaryColor  = MaterialTheme.colorScheme.primary
        val subtitleColor = MaterialTheme.colorScheme.onSurfaceVariant
        Text(
            text = buildAnnotatedString {
                append(stringResource(R.string.generating_subtitle_prefix))
                withStyle(SpanStyle(color = primaryColor, fontWeight = FontWeight.SemiBold)) {
                    append("$questionCount")
                }
                append(stringResource(R.string.generating_subtitle_in))
                withStyle(SpanStyle(color = primaryColor, fontWeight = FontWeight.SemiBold)) {
                    append(language)
                }
                if (focusNotesActive) {
                    append(stringResource(R.string.generating_subtitle_focus))
                }
            },
            style      = MaterialTheme.typography.bodyMedium,
            color      = subtitleColor,
            textAlign  = TextAlign.Center,
            lineHeight = 22.sp,
        )

        Spacer(Modifier.height(28.adp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.adp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text  = stringResource(R.string.generating_progress_label),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text  = "${(animatedProgress * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
            )
        }

        LinearProgressIndicator(
            progress   = { animatedProgress },
            modifier   = Modifier
                .fillMaxWidth()
                .height(8.adp)
                .clip(CircleShape),
            color      = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )

        Spacer(Modifier.height(16.adp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.adp),
        ) {
            tasks.forEach { task ->
                GeneratingTaskRow(label = task.label, done = task.done)
            }
        }
    }
}

@Composable
fun GeneratingTaskRow(
    label: String,
    done: Boolean,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color    = if (done) MaterialTheme.colorScheme.tertiaryContainer
        else MaterialTheme.colorScheme.surfaceVariant,
        shape    = Radius.ShapeMedium,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.adp, vertical = 10.adp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.adp),
        ) {
            if (done) {
                Icon(
                    painter = painterResource(R.drawable.ic_check),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(13.adp),
                )
            } else {
                CircularProgressIndicator(
                    modifier    = Modifier.size(13.adp),
                    color       = MaterialTheme.colorScheme.onSurfaceVariant,
                    strokeWidth = 1.5.adp,
                )
            }
            Text(
                text  = label,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = if (done) FontWeight.SemiBold else FontWeight.Normal,
                ),
                color = if (done) MaterialTheme.colorScheme.tertiary
                else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// DoneStep
// ══════════════════════════════════════════════════════════════════════════════

@Composable
fun DoneStep(
    packName: String,
    questionCount: Int,
    language: String,
    languageFlag: String,
    languageShort: String,
    generatedQuestions: List<QuestionUiModel>,
    onStartStudying: () -> Unit,
    onBackToDashboard: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.adp),
    ) {
        Spacer(Modifier.height(8.adp))

        DoneSuccessHeader(
            packName      = packName,
            questionCount = questionCount,
            language      = language,
        )

        DoneStatsRow(
            questionCount = questionCount,
            languageFlag  = languageFlag,
            languageShort = languageShort,
        )

        SectionLabel(text = stringResource(R.string.done_preview_label))

        val questionsToShow = if (generatedQuestions.isNotEmpty()) {
            generatedQuestions.take(3).mapIndexed { _, q ->
                MockQuestion(
                    type  = q.type.name.replace("_", "/"),
                    emoji = when (q.type) {
                        QuestionType.MCQ        -> "🧠"
                        QuestionType.TRUE_FALSE -> "✅"
                        QuestionType.FLASHCARD  -> "📚"
                        else                    -> "❓"
                    },
                    text = q.questionText,
                )
            }
        } else {
            MOCK_PREVIEW
        }

        questionsToShow.forEachIndexed { i, q ->
            PreviewQuestionCard(
                typeLabel    = q.type,
                emoji        = q.emoji,
                questionText = q.text,
                index        = i,
            )
        }

        if (questionCount > 3) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color    = MaterialTheme.colorScheme.primaryContainer,
                shape    = Radius.ShapeLarge,
                border   = BorderStroke(1.5.adp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
            ) {
                Text(
                    text      = stringResource(R.string.done_more_questions, questionCount - 3),
                    modifier  = Modifier.padding(14.adp),
                    style     = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color     = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                )
            }
        }

        PrimaryGradientButton(
            text    = stringResource(R.string.done_start_studying),
            iconRes = R.drawable.ic_zap,
            enabled = true,
            onClick = onStartStudying,
        )

        SecondaryButton(
            text    = stringResource(R.string.done_back_to_dashboard),
            onClick = onBackToDashboard,
        )

        Spacer(Modifier.height(8.adp))
    }
}

@Composable
fun DoneSuccessHeader(
    packName: String,
    questionCount: Int,
    language: String,
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "done_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue  = 0.35f,
        targetValue   = 0f,
        animationSpec = infiniteRepeatable(tween(2_500), RepeatMode.Reverse),
        label         = "done_pulse_alpha",
    )

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AnimatedVisibility(
            visible = visible,
            enter   = scaleIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + fadeIn(),
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(120.adp)) {
                // Pulsing radial glow
                Box(
                    modifier = Modifier
                        .size(120.adp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = pulseAlpha),
                                    Color.Transparent,
                                )
                            )
                        ),
                )
                // Success circle — brand emerald gradient from design token system
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(80.adp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    BrandColors.BrandSuccessLight,
                                    BrandColors.BrandSuccessDark,
                                )
                            )
                        ),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_check),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(40.adp),
                    )
                }
            }
        }

        Spacer(Modifier.height(16.adp))

        Text(
            text  = stringResource(R.string.done_title),
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(Modifier.height(8.adp))

        val primaryColor  = MaterialTheme.colorScheme.primary
        val subtitleColor = MaterialTheme.colorScheme.onSurfaceVariant
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = primaryColor, fontWeight = FontWeight.Bold)) {
                    append(" $packName ")
                }
                append(stringResource(R.string.done_subtitle_ready))
                withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) {
                    append(" ${stringResource(R.string.done_subtitle_questions, questionCount)}")
                }
                append(" ${stringResource(R.string.done_subtitle_in, language)}")
            },
            style      = MaterialTheme.typography.bodyMedium,
            color      = subtitleColor,
            textAlign  = TextAlign.Center,
            lineHeight = 22.sp,
        )
    }
}

@Composable
fun DoneStatsRow(
    questionCount: Int,
    languageFlag: String,
    languageShort: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.adp),
    ) {
        StatChip(
            value    = "$questionCount",
            label    = stringResource(R.string.stat_questions),
            color    = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f),
        )
        StatChip(
            value    = "$languageFlag $languageShort",
            label    = stringResource(R.string.stat_language),
            color    = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
fun StatChip(
    value: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color    = color.copy(alpha = 0.12f),
        shape    = Radius.ShapeLarge,
    ) {
        Column(
            modifier = Modifier.padding(12.adp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text  = value,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                color = color,
            )
            Text(
                text     = label,
                style    = MaterialTheme.typography.labelSmall,
                color    = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.adp),
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
    modifier: Modifier = Modifier,
) {
    ElevatedCard(
        modifier  = modifier.fillMaxWidth(),
        shape     = Radius.ShapeLarge,
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = Elevation.Level1),
    ) {
        Column(modifier = Modifier.padding(16.adp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.adp),
                modifier = Modifier.padding(bottom = 8.adp),
            ) {
                Text(text = emoji, fontSize = 14.sp)
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = CircleShape,
                ) {
                    Text(
                        text     = typeLabel.uppercase(),
                        modifier = Modifier.padding(horizontal = 8.adp, vertical = 2.adp),
                        style    = MaterialTheme.typography.labelSmall.copy(
                            fontWeight    = FontWeight.ExtraBold,
                            letterSpacing = 0.05.sp,
                            fontSize      = 9.sp,
                        ),
                        color = MaterialTheme.colorScheme.secondary,
                    )
                }
                Text(
                    text  = "Q${index + 1}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text       = questionText,
                style      = MaterialTheme.typography.bodyMedium,
                color      = MaterialTheme.colorScheme.onSurface,
                lineHeight = 20.sp,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageBottomSheet(
    selectedCode: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
        modifier         = modifier,
        shape            = Radius.ShapeXXXL,
        tonalElevation   = Elevation.Level4,
    ) {
        // Sheet header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.adp)
                .padding(bottom = 12.adp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.adp),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_globe),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.adp),
            )
            Text(
                text     = stringResource(R.string.language_sheet_title),
                style    = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color    = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            Surface(
                onClick  = onDismiss,
                shape    = CircleShape,
                color    = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(30.adp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(R.drawable.ic_x),
                        contentDescription = stringResource(R.string.cd_close),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.adp),
                    )
                }
            }
        }

        HorizontalDivider()

        LazyColumn(modifier = Modifier.navigationBarsPadding()) {
            items(LANGUAGES, key = { it.code }) { lang ->
                val isSelected = selectedCode == lang.code
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color    = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                    else Color.Transparent,
                    onClick  = { onSelect(lang.code) },
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.adp, vertical = 14.adp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.adp),
                    ) {
                        Text(text = lang.flag, fontSize = 20.sp)
                        Text(
                            text     = lang.label,
                            style    = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            ),
                            color    = if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f),
                        )
                        if (isSelected) {
                            Icon(
                                painter = painterResource(R.drawable.ic_check),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(15.adp),
                            )
                        }
                    }
                }
                if (lang != LANGUAGES.last()) {
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 20.adp))
                }
            }
        }
    }
}

@Composable
private fun SectionCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    ElevatedCard(
        modifier  = modifier.fillMaxWidth(),
        shape     = Radius.ShapeXXL,
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = Elevation.Level1),
    ) {
        Column(
            modifier = Modifier.padding(20.adp),
            content  = content,
        )
    }
}

@Composable
private fun SectionLabel(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text     = text,
        style    = MaterialTheme.typography.labelSmall.copy(
            fontWeight    = FontWeight.Bold,
            letterSpacing = 0.08.sp,
        ),
        color    = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier,
    )
}

@Preview(name = "Header · Light", showBackground = true)
@Preview(name = "Header · Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun AddPdfHeaderPreview() {
    SynapseTheme {
        AddPdfHeader(onBack = {}, modifier = Modifier.padding(20.adp))
    }
}

@Preview(name = "StepIndicator · Step 2 · Light", showBackground = true)
@Preview(name = "StepIndicator · Step 2 · Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun StepIndicatorPreview() {
    SynapseTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            StepIndicator(
                currentIndex = 1,
                modifier     = Modifier
                    .fillMaxWidth()
                    .padding(20.adp),
            )
        }
    }
}

@Preview(name = "QuestionCountCard · Light", showBackground = true)
@Preview(name = "QuestionCountCard · Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun QuestionCountCardPreview() {
    SynapseTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            QuestionCountCard(
                count    = 20,
                onChange = {},
                modifier = Modifier.padding(16.adp),
            )
        }
    }
}

@Preview(name = "GeneratingStep · Light", showBackground = true)
@Preview(name = "GeneratingStep · Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun GeneratingStepPreview() {
    SynapseTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            GeneratingStep(
                progress         = 0.65f,
                questionCount    = 20,
                language         = "English",
                focusNotesActive = true,
                modifier         = Modifier.padding(20.adp),
            )
        }
    }
}

@Preview(name = "DoneSuccessHeader · Light", showBackground = true)
@Preview(name = "DoneSuccessHeader · Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun DoneSuccessHeaderPreview() {
    SynapseTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            DoneSuccessHeader(
                packName      = "Machine Learning Ch.3",
                questionCount = 20,
                language      = "English",
                modifier      = Modifier.padding(20.adp),
            )
        }
    }
}

@Preview(name = "OcrBanner · On · Light", showBackground = true)
@Preview(name = "OcrBanner · On · Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun OcrBannerPreview() {
    SynapseTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            OcrBanner(
                enabled  = true,
                onToggle = {},
                modifier = Modifier.padding(16.adp),
            )
        }
    }
}

@Preview(name = "LanguageBottomSheet Row · Light", showBackground = true)
@Preview(name = "LanguageBottomSheet Row · Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun LanguageRowPreview() {
    SynapseTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column {
                LANGUAGES.take(4).forEach { lang ->
                    val isSelected = lang.code == "en"
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color    = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                        else Color.Transparent,
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 20.adp, vertical = 14.adp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.adp),
                        ) {
                            Text(text = lang.flag, fontSize = 20.sp)
                            Text(
                                text  = lang.label,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                ),
                                color = if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 20.adp))
                }
            }
        }
    }
}