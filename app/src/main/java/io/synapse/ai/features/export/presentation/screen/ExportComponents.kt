package io.synapse.ai.features.export.presentation.screen

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import io.synapse.ai.R
import io.synapse.ai.core.theme.synapse
import io.synapse.ai.core.theme.tokens.adp
import io.synapse.ai.features.export.domain.ExportTemplate
import io.synapse.ai.features.export.presentation.state.ExportUiState

data class TemplateUiModel(
    val template: ExportTemplate,
    val labelRes: Int,
    val descriptionRes: Int,
    val tagRes: Int,
    val iconRes: Int,
    val accentColor: Color,
)

@Composable
fun TemplateCard(
    model: TemplateUiModel,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor = if (isSelected) model.accentColor else MaterialTheme.colorScheme.outlineVariant
    val borderWidth = if (isSelected) MaterialTheme.synapse.spacing.s2 / 2 * 3 else MaterialTheme.synapse.spacing.s2 / 2

    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.synapse.radius.lg,
        color = if (isSelected) model.accentColor.copy(alpha = 0.06f)
        else MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(borderWidth, borderColor),
    ) {
        Row(
            modifier = Modifier.padding(MaterialTheme.synapse.spacing.s16),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.synapse.spacing.s12),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Icon container
            Box(
                modifier = Modifier
                    .size(44.adp)
                    .clip(MaterialTheme.synapse.radius.md)
                    .background(model.accentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(model.iconRes),
                    contentDescription = null,
                    tint = model.accentColor,
                    modifier = Modifier.size(22.adp),
                )
            }

            // Labels
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.synapse.spacing.s8),
                ) {
                    Text(
                        text = stringResource(model.labelRes),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    // Tag chip
                    Surface(
                        shape = MaterialTheme.synapse.radius.pill,
                        color = model.accentColor.copy(alpha = 0.12f),
                    ) {
                        Text(
                            text = stringResource(model.tagRes),
                            style = MaterialTheme.typography.labelSmall,
                            color = model.accentColor,
                            modifier = Modifier.padding(
                                horizontal = MaterialTheme.synapse.spacing.s6,
                                vertical = MaterialTheme.synapse.spacing.s2,
                            ),
                        )
                    }
                }
                Text(
                    text = stringResource(model.descriptionRes),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = MaterialTheme.synapse.spacing.s2),
                )
            }

            // Selected indicator
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(20.adp)
                        .clip(CircleShape)
                        .background(model.accentColor),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_check),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.size(12.adp),
                    )
                }
            }
        }
    }
}

@Composable
fun ExportSummaryCard(
    state: ExportUiState,
    templateColor: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.synapse.radius.lg,
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            MaterialTheme.synapse.spacing.s2 / 2,
            MaterialTheme.colorScheme.outlineVariant,
        ),
    ) {
        Column(modifier = Modifier.padding(MaterialTheme.synapse.spacing.s16)) {
            Text(
                text = stringResource(R.string.export_summary_label),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = MaterialTheme.synapse.spacing.s12),
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.synapse.spacing.s12),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(40.adp)
                        .clip(MaterialTheme.synapse.radius.md)
                        .background(templateColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(
                            when (state.options.template) {
                                ExportTemplate.STUDY -> R.drawable.ic_book_open
                                ExportTemplate.EXAM -> R.drawable.ic_file_text
                                ExportTemplate.TEACHER -> R.drawable.ic_graduation_cap
                            },
                        ),
                        contentDescription = null,
                        tint = templateColor,
                        modifier = Modifier.size(20.adp),
                    )
                }
                Column {
                    Text(
                        text = stringResource(
                            when (state.options.template) {
                                ExportTemplate.STUDY -> R.string.template_study
                                ExportTemplate.EXAM -> R.string.template_exam
                                ExportTemplate.TEACHER -> R.string.template_teacher
                            },
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = stringResource(R.string.export_questions_count, state.questionCount),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
fun FreeTierNotice(
    exportsRemaining: Int,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
        shape = MaterialTheme.synapse.radius.md,
    ) {
        Row(
            modifier = Modifier.padding(MaterialTheme.synapse.spacing.s12),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.synapse.spacing.s12),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_star),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(20.adp),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.free_tier_export_limit, exportsRemaining),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
                Text(
                    text = stringResource(R.string.watermark_applied),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f),
                )
            }
        }
    }
}

@Composable
fun ExportActionButton(
    @DrawableRes iconRes: Int,
    @StringRes titleRes: Int,
    isPrimary: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    @StringRes subtitleRes: Int? = null,
    enabled: Boolean = true,
) {
    val resolvedSubtitle = subtitle ?: subtitleRes?.let { stringResource(it) }

    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        shape = MaterialTheme.synapse.radius.lg,
        color = if (isPrimary) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.surface,
        border = if (!isPrimary) androidx.compose.foundation.BorderStroke(
            MaterialTheme.synapse.spacing.s2 / 2,
            MaterialTheme.colorScheme.outlineVariant,
        ) else null,
    ) {
        Row(
            modifier = Modifier.padding(MaterialTheme.synapse.spacing.s16),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.synapse.spacing.s12),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.adp)
                    .clip(MaterialTheme.synapse.radius.md)
                    .background(
                        if (isPrimary) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.primaryContainer,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    tint = if (isPrimary) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.adp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(titleRes),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isPrimary) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurface,
                )
                if (resolvedSubtitle != null) {
                    Text(
                        text = resolvedSubtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isPrimary) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowForwardIos,
                contentDescription = null,
                tint = if (isPrimary) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(14.adp),
            )
        }
    }
}
