package io.synapse.ai.features.add_pdf.presentation.components

import io.synapse.ai.core.ui.components.AnimatedTypingText

import io.synapse.ai.core.ui.components.LoadingIndicator

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import io.synapse.ai.R
import io.synapse.ai.core.theme.tokens.adp

@Composable
fun SharedUploadingPhaseUi(
    isPro: Boolean,
    uploadProgress: Float?,
    fileName: String?,
    fileSizeMb: Float,
    filePageCount: Int?,
) {
    // Hybrid Fake/Real Progress: Reaches 85% smoothly if no real progress is available
    var fakeProgress by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        animate(
            initialValue = 0f,
            targetValue = 0.85f,
            animationSpec = tween(durationMillis = 8000, easing = FastOutSlowInEasing)
        ) { value, _ ->
            fakeProgress = value
        }
    }

    val displayProgress = uploadProgress ?: fakeProgress

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.adp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(120.adp)) {
            if (displayProgress > 0f) {
                CircularProgressIndicator(
                    progress = { displayProgress },
                    modifier = Modifier.fillMaxSize(),
                    strokeWidth = 6.adp,
                    strokeCap = StrokeCap.Round
                )
            } else {
                CircularProgressIndicator(
                    modifier = Modifier.fillMaxSize(),
                    strokeWidth = 6.adp,
                    strokeCap = StrokeCap.Round
                )
            }
            Icon(
                painter = painterResource(R.drawable.ic_upload),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.adp)
            )
        }

        Spacer(Modifier.height(28.adp))
        Text(
            text = stringResource(R.string.generating_upload_title),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(8.adp))
        val pageCountStr = filePageCount?.let { " • $it pages" } ?: ""
        Text(
            text = "${fileName ?: stringResource(R.string.generating_upload_pdf_fallback)} • ${
                String.format(
                    java.util.Locale.US,
                    "%.1f",
                    fileSizeMb
                )
            } MB$pageCountStr",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(16.adp))
        Text(
            text = stringResource(R.string.generating_upload_subtitle),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline,
            textAlign = TextAlign.Center
        )

        if (!isPro) {
            Spacer(Modifier.height(32.adp))
            AnimatedVisibility(
                visible = fakeProgress > 0.3f, // Show the tip after 30% progress (~2.5s) to let user feel the wait
                enter = fadeIn() + expandVertically()
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        text = stringResource(R.string.generating_upload_pro_tip),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 16.adp, vertical = 8.adp)
                    )
                }
            }
        }
    }
}

@Composable
fun SharedPreparingPhaseUi(
    isPro: Boolean,
    progressMessageIndex: Int,
    fileSizeMb: Float,
    filePageCount: Int?,
) {
    val tasks = stringArrayResource(
        if (isPro) R.array.generating_tasks_pro else R.array.generating_tasks_standard
    )

    val currentIndex = progressMessageIndex.coerceAtMost(tasks.lastIndex)
    val currentTaskText = tasks[currentIndex]

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.adp, horizontal = 16.adp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(100.adp)) {
            LoadingIndicator(size = 100.adp)
        }
        Spacer(Modifier.height(32.adp))
        Text(
            text = stringResource(R.string.generating_prepare_title),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(8.adp))
        if (fileSizeMb > 0f) {
            val displayString = if (filePageCount != null) {
                stringResource(
                    R.string.generating_prepare_analyzing_size_pages,
                    fileSizeMb,
                    filePageCount
                )
            } else {
                stringResource(R.string.generating_prepare_analyzing_size, fileSizeMb)
            }
            Text(
                text = displayString,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(Modifier.height(32.adp))

        // Animated Typing Text
        AnimatedTypingText(
            text = currentTaskText,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

