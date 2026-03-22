package com.venom.synapse.features.profile.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import coil.compose.AsyncImage
import com.venom.synapse.R
import com.venom.synapse.core.theme.synapse
import com.venom.synapse.features.profile.presentation.state.ProfileUiState
import com.venom.ui.components.common.adp
import com.venom.ui.components.common.asp

@Composable
fun ProfileAvatarSection(
    uiState: ProfileUiState,
    gradient: Brush,
    modifier: Modifier = Modifier,
) {
    val cs = MaterialTheme.colorScheme
    val semantic = MaterialTheme.synapse.semantic

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(contentAlignment = Alignment.BottomEnd) {
            Box(
                modifier = Modifier
                    .size(80.adp)
                    .clip(CircleShape)
                    .background(gradient),
                contentAlignment = Alignment.Center,
            ) {
                if (uiState.avatarUrl != null) {
                    AsyncImage(
                        model = uiState.avatarUrl,
                        contentDescription = "Profile photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(80.adp)
                            .clip(CircleShape),
                    )
                } else {
                    Text(
                        text = uiState.avatarInitial.toString(),
                        fontSize = 30.asp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                    )
                }
            }
            // Verified badge — only for authenticated (non-anonymous) users
            if (!uiState.isAnonymous) {
                val badgeColor = if (uiState.isPremium) semantic.gold else semantic.success
                Icon(
                    painter = painterResource(R.drawable.ic_seal_check),
                    contentDescription = null,
                    tint = badgeColor,
                    modifier = Modifier.size(22.adp),
                )
            }
        }

        Spacer(Modifier.height(12.adp))

        Text(
            text = uiState.userName ?: stringResource(R.string.profile_anonymous_user),
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.asp),
            fontWeight = FontWeight.Bold,
            color = cs.onSurface,
        )
        Spacer(Modifier.height(2.adp))
        Text(
            text = uiState.userEmail ?: stringResource(R.string.profile_sign_in_prompt),
            style = MaterialTheme.typography.bodySmall,
            color = cs.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.adp))

        // Plan badge chip
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(cs.primaryContainer.copy(alpha = 0.25f))
                .border(1.adp, cs.outlineVariant, CircleShape)
                .padding(horizontal = 12.adp, vertical = 4.adp),
        ) {
            Text(
                text = uiState.planLabel,
                style = MaterialTheme.typography.labelSmall,
                color = cs.onSurfaceVariant,
            )
        }
    }
}
