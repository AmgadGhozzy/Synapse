package io.synapse.ai.features.profile.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import coil.compose.AsyncImage
import io.synapse.ai.R
import io.synapse.ai.core.theme.synapse
import io.synapse.ai.core.theme.tokens.adp
import io.synapse.ai.features.profile.presentation.state.ProfileUiState

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ProfileAvatarSection(
    uiState: ProfileUiState,
    modifier: Modifier = Modifier,
) {
    val cs = MaterialTheme.colorScheme
    val tokens = MaterialTheme.synapse
    val shape = MaterialShapes.Cookie9Sided.toShape()

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(contentAlignment = Alignment.BottomEnd) {
            Box(
                modifier = Modifier
                    .size(86.adp)
                    .background(tokens.gradients.primary, shape)
                    .border(
                        2.adp,
                        if (uiState.isPremium) tokens.gradients.gold else tokens.gradients.primary,
                        shape
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (uiState.avatarUrl != null) {
                    AsyncImage(
                        model = uiState.avatarUrl,
                        contentDescription = stringResource(R.string.profile_photo_description),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .size(86.adp)
                            .clip(shape),
                    )
                } else {
                    Text(
                        text = uiState.avatarInitial.toString(),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White.copy(0.9f),
                    )
                }
            }
            if (!uiState.isAnonymous) {
                val badgeColor =
                    if (uiState.isPremium) tokens.semantic.gold else tokens.semantic.success
                Icon(
                    painter = painterResource(R.drawable.ic_seal_check),
                    contentDescription = null,
                    tint = badgeColor,
                    modifier = Modifier.size(MaterialTheme.synapse.spacing.icon_lg),
                )
            }
        }

        Spacer(Modifier.height(12.adp))

        Text(
            text = uiState.userName ?: stringResource(R.string.profile_anonymous_user),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = cs.onSurface,
        )
        Spacer(Modifier.height(2.adp))
        Text(
            text = uiState.userEmail ?: stringResource(R.string.profile_sign_in_prompt),
            style = MaterialTheme.typography.bodySmall,
            color = cs.onSurfaceVariant,
        )

        // Plan badge chip
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(cs.primaryContainer.copy(alpha = 0.25f))
                .border(1.adp, cs.outlineVariant, CircleShape)
                .padding(horizontal = 12.adp, vertical = 4.adp),
        ) {
            Text(
                text = stringResource(uiState.planLabelRes),
                style = MaterialTheme.typography.labelLarge,
                color = if (uiState.isPremium) tokens.semantic.gold else cs.onSurfaceVariant,
            )
        }
    }
}
