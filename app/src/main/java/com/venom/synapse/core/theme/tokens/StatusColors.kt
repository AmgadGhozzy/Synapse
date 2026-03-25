package com.venom.synapse.core.theme.tokens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.venom.synapse.core.theme.Amber100
import com.venom.synapse.core.theme.Amber300
import com.venom.synapse.core.theme.Amber400
import com.venom.synapse.core.theme.Amber500
import com.venom.synapse.core.theme.Amber600
import com.venom.synapse.core.theme.Amber700
import com.venom.synapse.core.theme.Green100
import com.venom.synapse.core.theme.Green300
import com.venom.synapse.core.theme.Green400
import com.venom.synapse.core.theme.Green600
import com.venom.synapse.core.theme.Green700
import com.venom.synapse.core.theme.Red100
import com.venom.synapse.core.theme.Red300
import com.venom.synapse.core.theme.Red400
import com.venom.synapse.core.theme.Red500
import com.venom.synapse.core.theme.Red700
import com.venom.synapse.core.theme.Violet100
import com.venom.synapse.core.theme.Violet300
import com.venom.synapse.core.theme.Violet400
import com.venom.synapse.core.theme.Violet500
import com.venom.synapse.core.theme.Violet700

@Immutable
data class StatusColors(
    val accentColor: Color,
    val bgColor    : Color,
    val borderColor: Color,
)

@Immutable
data class LevelColors(
    val basic : StatusColors,
    val normal: StatusColors,
    val master: StatusColors,
    val elite : StatusColors,
    val hint  : StatusColors,
)

val DarkLevelColors = LevelColors(
    basic = StatusColors(
        accentColor = Green300,
        bgColor     = Green400.copy(alpha = 0.15f),
        borderColor = Green400.copy(alpha = 0.3f),
    ),
    normal = StatusColors(
        accentColor = Amber300,
        bgColor     = Amber400.copy(alpha = 0.14f),
        borderColor = Amber500.copy(alpha = 0.35f),
    ),
    master = StatusColors(
        accentColor = Red300,
        bgColor     = Red400.copy(alpha = 0.15f),
        borderColor = Red400.copy(alpha = 0.4f),
    ),
    elite = StatusColors(
        accentColor = Violet300,
        bgColor     = Violet400.copy(alpha = 0.14f),
        borderColor = Violet500.copy(alpha = 0.35f),
    ),
    hint = StatusColors(
        accentColor = Amber400,
        bgColor     = Amber400.copy(alpha = 0.12f),
        borderColor = Amber400.copy(alpha = 0.30f),
    ),
)

val LightLevelColors = LevelColors(

    basic = StatusColors(
        accentColor = Green700,
        bgColor     = Green100,
        borderColor = Green600,
    ),

    normal = StatusColors(
        accentColor = Amber700,
        bgColor     = Amber100,
        borderColor = Amber600,
    ),

    master = StatusColors(
        accentColor = Red700,
        bgColor     = Red100,
        borderColor = Red500,
    ),

    elite = StatusColors(
        accentColor = Violet700,
        bgColor     = Violet100,
        borderColor = Violet500,
    ),

    hint = StatusColors(
        accentColor = Amber700,
        bgColor     = Amber100,
        borderColor = Amber600,
    ),
)

val LocalLevelColors = staticCompositionLocalOf { LightLevelColors }

@Composable
@ReadOnlyComposable
fun getLevelColors(score: Int): StatusColors {
    val colors = LocalLevelColors.current
    return when (score) {
        in 1..3 -> colors.basic
        in 4..7 -> colors.normal
        else    -> colors.master
    }
}