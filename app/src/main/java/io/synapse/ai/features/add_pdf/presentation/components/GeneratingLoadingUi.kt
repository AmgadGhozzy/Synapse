package io.synapse.ai.features.add_pdf.presentation.components


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import io.synapse.ai.R
import io.synapse.ai.core.theme.tokens.adp
import io.synapse.ai.core.ui.components.AnimatedTypingText
import io.synapse.ai.core.ui.components.PrimaryGradientButton
import io.synapse.ai.core.ui.components.WavyProgressIndicator

// Smooth progress curve: fast start → steady middle → slow finish
private fun mapGeneratingProgress(real: Float): Float = when {
    real < 0.2f -> real * 0.5f
    real < 0.8f -> 0.1f + (real - 0.2f) * 1.25f
    else        -> 0.85f + (real - 0.8f) * 0.75f
}

/**
 * Generic "generating" loading screen shared between the pack and summary features.
 *
 * @param headerContent    Slot rendered inside the 72.adp circle avatar (emoji text, icon, etc.)
 * @param title            Bold headline below the avatar.
 * @param stage            Current progress stage label shown in primary colour.
 * @param progress         Raw progress in [0f, 1f]. When 0f the progress row is hidden.
 * @param stats            Optional list of (value, label, color) stat chips shown below the bar.
 * @param earlyStartVisible Whether to show the "start early" banner + button.
 * @param earlyStartMessage Hint text inside the banner.
 * @param earlyStartLabel   Button label.
 * @param onStartEarly      Callback for the early-start button; ignored when [earlyStartVisible] is false.
 * @param modifier          Modifier applied to the root Column.
 */
@Composable
fun GeneratingLoadingUi(
    headerContent: @Composable () -> Unit,
    title: String,
    stage: String,
    progress: Float,
    modifier: Modifier = Modifier,
    stats: List<Triple<String, String, Color>> = emptyList(),
    earlyStartVisible: Boolean = false,
    earlyStartMessage: String = "",
    earlyStartLabel: String = "",
    onStartEarly: () -> Unit = {},
) {
    val mappedProgress = remember(progress) { mapGeneratingProgress(progress) }
    val animatedProgress by animateFloatAsState(
        targetValue = mappedProgress,
        animationSpec = tween(800, easing = FastOutSlowInEasing)
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(16.adp))

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(72.adp)
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
        ) {
            headerContent()
        }

        Spacer(Modifier.height(24.adp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.adp))
        AnimatedTypingText(
            text = stage,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
        )

        if (progress > 0f) {
            Spacer(Modifier.height(32.adp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.adp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.generating_progress_lbl),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${(animatedProgress * 100).toInt()} %",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            WavyProgressIndicator(progress = animatedProgress)
        }

        if (stats.isNotEmpty()) {
            Spacer(Modifier.height(28.adp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.adp)
            ) {
                stats.forEach { (value, label, color) ->
                    GeneratingStatChip(
                        value = value,
                        label = label,
                        color = color,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = earlyStartVisible,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Spacer(Modifier.height(32.adp))
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Row(
                        modifier = Modifier.padding(16.adp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.adp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_zap),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.adp)
                        )
                        Text(
                            text = earlyStartMessage,
                            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Spacer(Modifier.height(16.adp))
                PrimaryGradientButton(
                    text = earlyStartLabel,
                    iconRes = R.drawable.ic_zap,
                    enabled = true,
                    onClick = onStartEarly
                )
            }
        }
    }
}

@Composable
private fun GeneratingStatChip(
    value: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = MaterialTheme.shapes.medium,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.adp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = color
            )
            Spacer(Modifier.height(4.adp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
        }
    }
}

