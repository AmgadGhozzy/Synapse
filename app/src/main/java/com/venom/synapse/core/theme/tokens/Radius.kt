package com.venom.synapse.core.theme.tokens

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

data class RadiusTokens(
    val xs:     Shape = Radius.ShapeExtraSmall,
    val sm:     Shape = Radius.ShapeSmall,
    val md:     Shape = Radius.ShapeMedium,
    val lg:     Shape = Radius.ShapeLarge,
    val xl:     Shape = Radius.ShapeXL,
    val xxl:    Shape = Radius.ShapeXXL,
    val xxxl:   Shape = Radius.ShapeXXXL,
    val pill:   Shape = Radius.ShapePill,
    val circle: Shape = Radius.ShapeCircle,
    val stripe: Shape = Radius.ShapeAccentStripe,
)

val defaultRadiusTokens = RadiusTokens()

object Radius {
    val RadiusExtraSmall = 4.dp
    val RadiusSmall      = 8.dp
    val RadiusMedium     = 12.dp
    val RadiusLarge      = 16.dp
    val RadiusXL         = 20.dp
    val RadiusXXL        = 24.dp
    val RadiusXXXL       = 28.dp
    val RadiusFrame      = 44.dp

    val ShapeExtraSmall = RoundedCornerShape(RadiusExtraSmall)
    val ShapeSmall      = RoundedCornerShape(RadiusSmall)
    val ShapeMedium     = RoundedCornerShape(RadiusMedium)
    val ShapeLarge      = RoundedCornerShape(RadiusLarge)
    val ShapeXL         = RoundedCornerShape(RadiusXL)
    val ShapeXXL        = RoundedCornerShape(RadiusXXL)
    val ShapeXXXL       = RoundedCornerShape(RadiusXXXL)
    val ShapePill       = RoundedCornerShape(50)
    val ShapeCircle     = CircleShape

    val ShapeAccentStripe = RoundedCornerShape(
        topStart    = RadiusMedium,
        topEnd      = 0.dp,
        bottomEnd   = 0.dp,
        bottomStart = RadiusMedium,
    )
}

val SynapseShapes = Shapes(
    extraSmall = Radius.ShapeExtraSmall,
    small      = Radius.ShapeSmall,
    medium     = Radius.ShapeLarge,
    large      = Radius.ShapeXXL,
    extraLarge = Radius.ShapeXXXL,
)