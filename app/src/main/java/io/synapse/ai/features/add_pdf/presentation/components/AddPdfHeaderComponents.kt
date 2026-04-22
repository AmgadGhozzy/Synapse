package io.synapse.ai.features.add_pdf.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import io.synapse.ai.R
import io.synapse.ai.core.theme.synapse
import io.synapse.ai.core.theme.tokens.adp
import io.synapse.ai.core.ui.components.CloseButton

/**
 * Header components for Add PDF screen.
 * Split from AddPdfComponents.kt for better code organization.
 */

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
        CloseButton(onClick = onBack)

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.add_pdf_title),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = stringResource(R.string.add_pdf_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
            Row(
                modifier = Modifier.padding(horizontal = 14.adp, vertical = 7.adp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.adp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_sparkles),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.adp),
                )
                Text(
                    text = stringResource(R.string.add_pdf_ai_ready),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
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
    val stepLabels = listOf(
        stringResource(R.string.step_upload),
        stringResource(R.string.step_configure),
        stringResource(R.string.step_generate),
        stringResource(R.string.step_done),
    )

    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        stepLabels.forEachIndexed { index, label ->
            val isActive = index == currentIndex
            val isDone = index < currentIndex

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.adp),
            ) {
                if (isDone) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(30.adp)
                            .clip(MaterialTheme.synapse.radius.sm)
                            .background(MaterialTheme.colorScheme.primary),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_check),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(15.adp),
                        )
                    }
                } else {
                    val boxBackground = if (isActive) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }

                    val numberColor = if (isActive) {
                        Color.White.copy(0.8f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.40f)
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(30.adp)
                            .clip(MaterialTheme.synapse.radius.sm)
                            .background(boxBackground),
                    ) {
                        Text(
                            text = "${index + 1}",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = numberColor,
                            textAlign = TextAlign.Center,
                        )
                    }
                }

                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                    ),
                    color = when {
                        isActive -> MaterialTheme.colorScheme.primary
                        isDone -> MaterialTheme.colorScheme.onSurfaceVariant
                        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.40f)
                    },
                )
            }

            // Connector line
            if (index < stepLabels.lastIndex) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 6.adp)
                        .height(2.adp)
                        .clip(RoundedCornerShape(1.adp))
                        .background(
                            if (index < currentIndex) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outlineVariant
                        ),
                )
            }
        }
    }
}
