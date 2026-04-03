package com.venom.synapse.core.theme.tokens

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

data class RadiusTokens(
    val xs: Shape = RoundedCornerShape(4.dp),
    val sm: Shape = RoundedCornerShape(8.dp),
    val md: Shape = RoundedCornerShape(12.dp),
    val lg: Shape = RoundedCornerShape(16.dp),
    val xl: Shape = RoundedCornerShape(20.dp),
    val xxl: Shape = RoundedCornerShape(24.dp),
    val xxxl: Shape = RoundedCornerShape(28.dp),
    val pill: Shape = RoundedCornerShape(50),
)

val defaultRadiusTokens = RadiusTokens()

/** Material 3 Shapes*/
val SynapseShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp),
)