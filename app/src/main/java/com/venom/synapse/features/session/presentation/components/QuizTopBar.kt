package com.venom.synapse.features.session.presentation.components

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.venom.synapse.R
import com.venom.synapse.core.theme.SynapseTheme
import com.venom.synapse.core.theme.synapse
import com.venom.ui.components.common.adp
import com.venom.ui.components.common.localized

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun QuizTopBar(
    title          : String,
    questionIndex  : Int,
    totalQuestions : Int,
    progress       : Float,
    accuracy       : Float,
    showAccuracy   : Boolean,
    onClose        : () -> Unit,
    scrollBehavior : TopAppBarScrollBehavior,
    modifier       : Modifier = Modifier,
) {
    val primary        = MaterialTheme.colorScheme.primary
    val semantic       = MaterialTheme.synapse.semantic
    val spacing        = MaterialTheme.synapse.spacing
    val useDotsProgress = remember(totalQuestions) { totalQuestions <= 30 }

    MediumFlexibleTopAppBar(
        modifier       = modifier.fillMaxWidth()
            .clipToBounds(),
        windowInsets   = WindowInsets(0, 0, 0, 0),
        scrollBehavior = scrollBehavior,
        colors         = TopAppBarDefaults.topAppBarColors(
            containerColor        = Color.Transparent,
            scrolledContainerColor = Color.Transparent,
            titleContentColor     = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        title = {
            Text(
                text      = title,
                fontWeight = FontWeight.ExtraBold,
                maxLines  = 1,
                overflow  = TextOverflow.Ellipsis,
            )
        },
        subtitle = {
                if (useDotsProgress) {
                    SegmentedDotTrack(
                        questionIndex  = questionIndex,
                        totalQuestions = totalQuestions,
                    )
                } else {
                    SmoothProgressBar(progress       = progress)
                }
        },
        navigationIcon = {
            Box(
                modifier = Modifier
                    .padding(end = spacing.s8, top = spacing.s16)
                    .size(48.adp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .border(
                        width = Dp.Hairline,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        shape = MaterialTheme.shapes.medium,
                    )
                    .clickable(onClick = onClose),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter           = painterResource(R.drawable.ic_x),
                    contentDescription = null,
                    tint              = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier          = Modifier.size(20.adp),
                )
            }
        },
        actions = {
            Row(
                verticalAlignment     = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(spacing.s10),
                modifier              = Modifier.padding(start = spacing.s8, top = spacing.s16),
            ) {
                AnimatedVisibility(
                    visible = showAccuracy,
                    enter   = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)),
                    exit    = scaleOut(tween(180)),
                ) {
                    val accInt   = (accuracy * 100).toInt()
                    val badgeColor = if (accuracy >= 0.70f) semantic.success else semantic.error
                    BadgePill(
                        text      = "${accInt.localized()}${stringResource(R.string.percent_mark)}",
                        textColor = badgeColor,
                        bgColor   = badgeColor.copy(alpha = 0.13f),
                        rimColor  = badgeColor.copy(alpha = 0.35f),
                    )
                }

                BadgePill(
                    text      = "${questionIndex.localized()} / ${totalQuestions.localized()}",
                    textColor = primary,
                    bgColor   = primary.copy(alpha = 0.12f),
                    rimColor  = primary.copy(alpha = 0.28f),
                )
            }
        },
    )
}
@Composable
private fun BadgePill(
    text: String,
    textColor: Color,
    bgColor: Color,
    rimColor: Color,
) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(bgColor)
            .border(Dp.Hairline, rimColor, CircleShape)
            .padding(
                horizontal = MaterialTheme.synapse.spacing.s16,
                vertical = MaterialTheme.synapse.spacing.s4,
            ),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(
                platformStyle = PlatformTextStyle(includeFontPadding = false),
                fontWeight = FontWeight.ExtraBold
            ),
            color = textColor
        )
    }
}

@Composable
private fun SegmentedDotTrack(
    questionIndex: Int,
    totalQuestions: Int,
    modifier: Modifier = Modifier,
) {
    val primary = MaterialTheme.colorScheme.primary

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.adp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        for (i in 0 until totalQuestions) {
            val isDone = i < questionIndex - 1
            val isActive = i == questionIndex - 1

            val dotHeight by animateFloatAsState(
                targetValue = if (isActive) 11f else 7f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
            )
            val dotAlpha by animateFloatAsState(
                targetValue = if (isDone || isActive) 1f else 0.28f,
                animationSpec = tween(200)
            )

            Box(
                modifier = Modifier
                    .weight(if (isActive) 1.8f else 1f)
                    .height(dotHeight.dp)
                    .drawBehind {
                        if (isActive) {
                            drawRoundRect(
                                color = primary.copy(alpha = 0.18f),
                                cornerRadius = CornerRadius(size.height * 2),
                                size = size.copy(
                                    width = size.width + 8.dp.toPx(),
                                    height = size.height + 8.dp.toPx(),
                                ),
                                topLeft = Offset(-4.dp.toPx(), -4.dp.toPx()),
                            )
                        }
                    }
                    .clip(CircleShape)
                    .background(primary.copy(alpha = dotAlpha)),
            )
        }
    }
}

@Composable
private fun SmoothProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
) {
    val primary = MaterialTheme.colorScheme.primary

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(500, easing = FastOutSlowInEasing),)

    val fillBrush = remember(primary) {
        Brush.horizontalGradient(
            colors = listOf(primary.copy(alpha = 0.75f), primary),
        )
    }

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(11.adp)
                .clip(CircleShape)
                .background(primary.copy(alpha = 0.12f)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .height(11.adp)
                    .clip(CircleShape)
                    .background(fillBrush),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "TopBar Dots — Light, Expanded", showBackground = true, locale = "ar")
@Preview(
    name = "TopBar Dots — Dark, Expanded",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
)
@Composable
private fun QuizTopBarDotsPreview() {
    SynapseTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            QuizTopBar(
                title = "Machine Learning Basics",
                questionIndex = 3,
                totalQuestions = 20,
                progress = 0.15f,
                accuracy = 0.85f,
                showAccuracy = true,
                onClose = {},
                scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
                    rememberTopAppBarState(),
                ),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "TopBar Bar — Light, Expanded", showBackground = true)
@Preview(
    name = "TopBar Bar — Dark, Expanded",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
)
@Composable
private fun QuizTopBarBarPreview() {
    SynapseTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            QuizTopBar(
                title = "Advanced Data Structures & Algorithms",
                questionIndex = 8,
                totalQuestions = 50,
                progress = 0.28f,
                accuracy = 0.55f,
                showAccuracy = true,
                onClose = {},
                scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
                    rememberTopAppBarState(),
                ),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "No Accuracy — Light", showBackground = true)
@Composable
private fun QuizTopBarNoAccuracyPreview() {
    SynapseTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            QuizTopBar(
                title = "Introduction to Physics",
                questionIndex = 1,
                totalQuestions = 15,
                progress = 0f,
                accuracy = 0f,
                showAccuracy = false,
                onClose = {},
                scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
                    rememberTopAppBarState(),
                ),
            )
        }
    }
}