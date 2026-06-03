package io.synapse.ai.core.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import io.synapse.ai.R
import io.synapse.ai.core.theme.synapse
import io.synapse.ai.core.theme.tokens.adp
import io.synapse.ai.core.tts.TtsState
import io.synapse.ai.core.tts.isLoadingFor
import io.synapse.ai.core.tts.isPlayingFor

@Composable
fun TtsButton(
    text: String,
    ttsState: TtsState,
    onSpeak: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isLoading = ttsState.isLoadingFor(text)
    val isPlaying = ttsState.isPlayingFor(text)

    // Pulse animation while playing
    val infiniteTransition = rememberInfiniteTransition()
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        )
    )

    val iconTint = when {
        isPlaying -> MaterialTheme.synapse.semantic.primary
        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(0.8f)
    }

    Box(
        modifier = modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            enabled = !isLoading,
        ) { onSpeak(text) },
        contentAlignment = Alignment.Center,
    ) {
        when {
            isLoading -> CircularProgressIndicator(
                modifier = Modifier.size(20.adp),
                strokeWidth = 2.adp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            isPlaying -> Icon(
                painter = painterResource(R.drawable.ic_stop),
                contentDescription = stringResource(R.string.tts_stop),
                tint = iconTint,
                modifier = Modifier
                    .size(20.adp)
                    .graphicsLayer { this.alpha = alpha },
            )

            else -> Icon(
                painter = painterResource(R.drawable.ic_volume),
                contentDescription = stringResource(R.string.tts_speak),
                tint = iconTint,
                modifier = Modifier.size(22.adp),
            )
        }
    }
}
