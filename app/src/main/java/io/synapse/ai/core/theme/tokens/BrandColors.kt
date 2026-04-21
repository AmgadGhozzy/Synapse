package io.synapse.ai.core.theme.tokens

import androidx.compose.ui.graphics.Color
import io.synapse.ai.core.theme.Emerald500
import io.synapse.ai.core.theme.Emerald600
import io.synapse.ai.core.theme.Indigo600
import io.synapse.ai.core.theme.Orange400
import io.synapse.ai.core.theme.Orange500
import io.synapse.ai.core.theme.Red500
import io.synapse.ai.core.theme.Red600
import io.synapse.ai.core.theme.Slate100
import io.synapse.ai.core.theme.Slate200
import io.synapse.ai.core.theme.Slate300
import io.synapse.ai.core.theme.Slate50
import io.synapse.ai.core.theme.Slate600
import io.synapse.ai.core.theme.Slate900
import io.synapse.ai.core.theme.Slate950
import io.synapse.ai.core.theme.SynapseElevatedDark
import io.synapse.ai.core.theme.Violet500
import io.synapse.ai.core.theme.White

object BrandColors {

    // Brand
    val PrimaryLight = Indigo600
    val OnPrimaryLight = White

    val PrimaryDark = Indigo600
    val OnPrimaryDark = White

    val SecondaryLight = Violet500
    val OnSecondaryLight = White

    val SecondaryDark = Violet500
    val OnSecondaryDark = White

    val TertiaryLight = Orange500
    val OnTertiaryLight = Slate950

    val TertiaryDark = Orange400
    val OnTertiaryDark = Slate950

    // Status
    val SuccessLight = Emerald600
    val OnSuccessLight = White

    val SuccessDark = Emerald500
    val OnSuccessDark = White

    val ErrorLight = Red600
    val OnErrorLight = White

    val ErrorDark = Red500
    val OnErrorDark = White

    // Light surfaces
    val NeutralLightBg = Slate100
    val NeutralLightSurface = White
    val NeutralLightSurfaceLow = Slate100

    // Dark surfaces
    val NeutralDarkBg = Slate950
    val NeutralDarkSurface = Slate900
    val NeutralDarkSurfaceLow = SynapseElevatedDark

    // Text
    val OnLight = Slate900
    val OnLightMuted = Slate600

    val OnDark = Slate50
    val OnDarkMuted = Slate300

    // Borders
    val OutlineLight = Slate300
    val OutlineVariantLight = Slate200

    val OutlineDark = Color(0xFF334155)
    val OutlineVariantDark = Color(0xFF1E293B)
}