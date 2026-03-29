package com.venom.synapse.features.dashboard.presentation.components

import android.content.res.Configuration
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
import androidx.compose.ui.unit.dp
import com.venom.synapse.R
import com.venom.synapse.core.theme.SynapseTheme
import com.venom.synapse.core.theme.synapse
import com.venom.synapse.core.theme.tokens.ShadowTokens
import com.venom.synapse.core.theme.tokens.toShadow
import com.venom.ui.components.common.adp

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DashboardFab(
    isLocked  : Boolean,
    isExpanded: Boolean,
    onClick   : () -> Unit,
    modifier  : Modifier = Modifier,
) {
    val gradientBg = MaterialTheme.synapse.gradients.primary
    val lockedBg   = MaterialTheme.synapse.gradients.gold
    val fabShape   = MaterialTheme.shapes.medium

    Box(
        modifier = modifier
            .dropShadow(
                shape  = fabShape,
                shadow = ShadowTokens.ShadowFab.toShadow(
                    customColor = if (isLocked) MaterialTheme.synapse.semantic.gold else MaterialTheme.colorScheme.primary
                )
            )
            .clip(fabShape)
            .background(if (isLocked) lockedBg else gradientBg),
    ) {
        ExtendedFloatingActionButton(
            text = {
                Text(
                    text = if (isLocked) {
                        stringResource(R.string.go_pro_label)
                    } else {
                        stringResource(R.string.fab_new_pack)
                    },
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                )
            },
            icon = {
                Icon(
                    painter = painterResource(
                        if (isLocked) R.drawable.ic_lock else R.drawable.ic_plus,
                    ),
                    contentDescription = if (isLocked) {
                        stringResource(R.string.fab_upgrade_description)
                    } else {
                        stringResource(R.string.fab_new_pack_description)
                    },
                    modifier = Modifier.size(20.adp),
                )
            },
            onClick        = onClick,
            expanded       = isExpanded,
            containerColor = Color.Transparent,
            contentColor   = if (isLocked) {
                MaterialTheme.colorScheme.onTertiary
            } else {
                Color.White.copy(0.9f)
            },
            shape     = fabShape,
            elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp),
        )
    }
}

@Preview(name = "Light — Normal", showBackground = true)
@Preview(name = "Dark — Normal", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun DashboardFabNormalPreview() {
    SynapseTheme {
        DashboardFab(isLocked = false, isExpanded = true, onClick = {})
    }
}

@Preview(name = "Light — Locked", showBackground = true)
@Preview(name = "Dark — Locked", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun DashboardFabLockedPreview() {
    SynapseTheme {
        DashboardFab(isLocked = true, isExpanded = true, onClick = {})
    }
}