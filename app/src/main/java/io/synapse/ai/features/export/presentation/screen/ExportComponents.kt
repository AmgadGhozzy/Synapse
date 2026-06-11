package io.synapse.ai.features.export.presentation.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import io.synapse.ai.R
import io.synapse.ai.core.theme.synapse
import io.synapse.ai.core.theme.tokens.adp
import io.synapse.ai.core.theme.tokens.toShadow
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

    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = if (isSelected) model.accentColor.copy(alpha = 0.06f)
        else MaterialTheme.colorScheme.surface,
        border = BorderStroke(if (isSelected) 2.adp else 0.adp, borderColor),
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
        modifier = modifier.fillMaxWidth()
            .dropShadow(
                MaterialTheme.shapes.medium,
                MaterialTheme.synapse.shadows.subtle.toShadow(),
            ),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
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
            
            // Selected Options
            val options = mutableListOf<String>()
            options.clear()
            if (state.options.includeAnswers) options.add(stringResource(R.string.include_answers))
            if (state.options.includeAnswerKey) options.add(stringResource(R.string.include_answer_key))
            if (state.options.showMarks) options.add(stringResource(R.string.show_marks))
            if (state.options.shuffleQuestions) options.add(stringResource(R.string.shuffle_questions))
            
            if (options.isNotEmpty()) {
                Text(
                    text = options.joinToString(" • "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = MaterialTheme.synapse.spacing.s12),
                )
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

