package io.synapse.ai.features.dashboard.presentation.components

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import io.synapse.ai.R
import io.synapse.ai.core.theme.SynapseTheme
import io.synapse.ai.core.theme.synapse
import io.synapse.ai.core.theme.tokens.adp
import io.synapse.ai.core.theme.tokens.toShadow

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DashboardFab(
    isLocked  : Boolean,
    isExpanded: Boolean,
    isVisible : Boolean,
    onClick   : () -> Unit,
    modifier  : Modifier = Modifier,
) {
    val tokens     = MaterialTheme.synapse
    val gradientBg = tokens.gradients.primary
    val lockedBg   = tokens.gradients.gold
    val fabShape   = MaterialTheme.shapes.large

    val shadowColor = if (isLocked) tokens.semantic.gold
    else MaterialTheme.colorScheme.primary

    AnimatedVisibility(
        visible  = isVisible,
        modifier = modifier,
        enter    = fadeIn(
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        ) + scaleIn(
            initialScale  = 0.65f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness    = Spring.StiffnessMediumLow,
            ),
        ) + slideInVertically(
            initialOffsetY = { it / 3 },
            animationSpec  = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness    = Spring.StiffnessMediumLow,
            ),
        ),
        exit     = fadeOut(
            animationSpec = tween(200),
        ) + scaleOut(
            targetScale   = 0.80f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness    = Spring.StiffnessMedium,
            ),
        ) + slideOutVertically(
            targetOffsetY = { it / 3 },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness    = Spring.StiffnessMedium,
            ),
        ),
    ) {
        Box(
            modifier = Modifier
                .dropShadow(
                    shape  = fabShape,
                    shadow = tokens.shadows.medium.toShadow(customColor = shadowColor),
                )
                .clip(fabShape)
                .background(if (isLocked) lockedBg else gradientBg),
        ) {
            ExtendedFloatingActionButton(
                text = {
                    Text(
                        text  = stringResource(
                            if (isLocked) R.string.go_pro_label else R.string.fab_new_pack,
                        ),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                    )
                },
                icon = {
                    Icon(
                        painter            = painterResource(
                            if (isLocked) R.drawable.ic_lock else R.drawable.ic_plus,
                        ),
                        contentDescription = stringResource(
                            if (isLocked) R.string.fab_upgrade_description
                            else          R.string.fab_new_pack_description,
                        ),
                        modifier           = Modifier.size(MaterialTheme.synapse.spacing.icon_lg),
                    )
                },
                onClick        = onClick,
                expanded       = isExpanded,
                containerColor = Color.Transparent,
                contentColor   = Color.White.copy(alpha = 0.9f),
                shape          = fabShape,
                elevation      = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 0.adp,
                    pressedElevation = 0.adp,
                    focusedElevation = 0.adp,
                    hoveredElevation = 0.adp,
                ),
            )
        }
    }
}

@Preview(name = "Light — Normal", showBackground = true)
@Preview(name = "Dark — Normal",  uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun DashboardFabNormalPreview() {
    SynapseTheme {
        DashboardFab(isLocked = false, isExpanded = true, isVisible = true, onClick = {})
    }
}

@Preview(name = "Light — Locked", showBackground = true)
@Preview(name = "Dark — Locked",  uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun DashboardFabLockedPreview() {
    SynapseTheme {
        DashboardFab(isLocked = true, isExpanded = true, isVisible = true, onClick = {})
    }
}