package io.synapse.ai.features.marketplace.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import io.synapse.ai.R
import io.synapse.ai.core.theme.LocalSemanticColors
import io.synapse.ai.core.theme.synapse
import io.synapse.ai.core.theme.tokens.adp
import io.synapse.ai.core.theme.tokens.toShadow
import io.synapse.ai.features.marketplace.domain.MarketplacePack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketplacePackCard(
    pack: MarketplacePack,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val semantic = LocalSemanticColors.current

    val difficultyColor = when (pack.difficulty?.lowercase()) {
        "easy" -> semantic.success
        "medium" -> semantic.gold
        else -> MaterialTheme.colorScheme.error
    }

    val difficultyBg = when (pack.difficulty?.lowercase()) {
        "easy" -> semantic.successBg
        "medium" -> semantic.goldBg
        else -> MaterialTheme.colorScheme.errorContainer
    }

    Card(
        onClick = onClick,
        modifier = modifier
            .width(220.adp)
            .dropShadow(
                shape = MaterialTheme.synapse.radius.xl,
                shadow = MaterialTheme.synapse.shadows.medium.toShadow(),
            ),
        shape = MaterialTheme.synapse.radius.xl,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
    ) {
        Column(
            modifier = Modifier.padding(14.adp)
        ) {

            // Top row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {

                Box(
                    modifier = Modifier
                        .size(52.adp)
                        .clip(MaterialTheme.synapse.radius.md)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = pack.emoji ?: "📚",
                        style = MaterialTheme.typography.headlineSmall
                    )
                }

                if (pack.isPremium) {
                    Text(
                        text = stringResource(R.string.synapse_marketplace_pro_badge),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = semantic.gold,
                        modifier = Modifier
                            .background(
                                semantic.gold.copy(alpha = 0.15f),
                                MaterialTheme.synapse.radius.sm
                            )
                            .padding(horizontal = 8.adp, vertical = 2.adp)
                    )
                }
            }

            Spacer(Modifier.height(MaterialTheme.synapse.spacing.s10))

            Text(
                text = pack.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(Modifier.height(MaterialTheme.synapse.spacing.s6))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.adp)
            ) {

                pack.difficulty?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelSmall,
                        color = difficultyColor,
                        modifier = Modifier
                            .background(difficultyBg, MaterialTheme.synapse.radius.sm)
                            .padding(horizontal = 6.adp, vertical = 2.adp)
                    )
                }

                Text(
                    text = stringResource(
                        R.string.synapse_marketplace_cards_count,
                        pack.questionCount
                    ) ,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                pack.estimatedMinutes?.let {
                    Text(
                        text = "• ${it}m",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
@Composable
fun MarketplaceSearchBar(
    query: String,
    onQuery: (String) -> Unit,
    onFilters: () -> Unit,
    hasActiveFilters: Boolean,
) {
    val semantic = LocalSemanticColors.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .dropShadow(
                shape = MaterialTheme.shapes.medium,
                shadow = MaterialTheme.synapse.shadows.subtle.toShadow(),
            )
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surface)
            .padding(
                horizontal = MaterialTheme.synapse.spacing.s14,
                vertical = MaterialTheme.synapse.spacing.s12
            ),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(R.drawable.icon_search),
                contentDescription = "Search",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(MaterialTheme.synapse.spacing.icon_lg),
            )
            Spacer(Modifier.width(MaterialTheme.synapse.spacing.s10))
            BasicTextField(
                value = query,
                onValueChange = onQuery,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                ),
                decorationBox = { inner ->
                    if (query.isEmpty()) {
                        Text(
                            text = stringResource(R.string.synapse_marketplace_search_hint),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium,
                            )
                        )
                    }
                    inner()
                },
                modifier = Modifier.weight(1f),
            )
            if (query.isNotEmpty()) {
                IconButton(
                    onClick = { onQuery("") },
                    modifier = Modifier.size(MaterialTheme.synapse.spacing.s32)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_x),
                        contentDescription = "Clear",
                        modifier = Modifier.size(MaterialTheme.synapse.spacing.s16)
                    )
                }
            }
            IconButton(
                onClick = onFilters,
                modifier = Modifier
                    .padding(MaterialTheme.synapse.spacing.s6)
                    .size(MaterialTheme.synapse.spacing.s32)
                    .clip(MaterialTheme.synapse.radius.md)
                    .background(
                        if (hasActiveFilters) semantic.primaryBg
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_filter),
                    contentDescription = "Filters",
                    tint = if (hasActiveFilters) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(MaterialTheme.synapse.spacing.icon_xs),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketplaceFiltersBottomSheet(
    currentCategory: String?,
    currentDifficulty: String?,
    categories: List<String>,
    onCategory: (String?) -> Unit,
    onDifficulty: (String?) -> Unit,
    onClear: () -> Unit,
    onClose: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onClose,
        sheetState = sheetState,
        shape = RoundedCornerShape(
            topStart = MaterialTheme.synapse.spacing.s28,
            topEnd = MaterialTheme.synapse.spacing.s28
        ),
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = MaterialTheme.synapse.spacing.s24)
                .padding(bottom = MaterialTheme.synapse.spacing.s48)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    stringResource(R.string.synapse_marketplace_filters),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black
                )
                TextButton(onClick = { onClear(); onClose() }) {
                    Text(
                        stringResource(R.string.synapse_marketplace_clear_filters),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(Modifier.height(MaterialTheme.synapse.spacing.s20))

            // Difficulty
            Text(
                stringResource(R.string.synapse_marketplace_difficulty),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(MaterialTheme.synapse.spacing.s10))
            Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.synapse.spacing.s10)) {
                listOf("easy", "medium", "hard").forEach { diff ->
                    val selected = currentDifficulty == diff
                    FilterChip(label = getDifficultyString(diff), selected = selected) {
                        onDifficulty(if (selected) null else diff)
                    }
                }
            }

            if (categories.isNotEmpty()) {
                Spacer(Modifier.height(MaterialTheme.synapse.spacing.s20))
                Text(
                    stringResource(R.string.synapse_marketplace_category),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(MaterialTheme.synapse.spacing.s10))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.synapse.spacing.s10),
                ) {
                    categories.forEach { cat ->
                        val selected = currentCategory == cat
                        FilterChip(label = cat, selected = selected) {
                            onCategory(if (selected) null else cat)
                        }
                    }
                }
            }

            Spacer(Modifier.height(MaterialTheme.synapse.spacing.s24))
            Button(
                onClick = onClose,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(MaterialTheme.synapse.spacing.s48 + MaterialTheme.synapse.spacing.s4),
                shape = MaterialTheme.shapes.large,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            ) {
                Text(
                    stringResource(R.string.synapse_marketplace_apply),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
private fun FilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        border = if (!selected) androidx.compose.foundation.BorderStroke(
            MaterialTheme.synapse.spacing.s2 / 2,
            MaterialTheme.colorScheme.outlineVariant
        ) else null,
        modifier = Modifier
            .height(MaterialTheme.synapse.spacing.s32 + MaterialTheme.synapse.spacing.s4)
            .dropShadow(
                shape = CircleShape,
                shadow = MaterialTheme.synapse.shadows.subtle.toShadow(),
            )
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = MaterialTheme.synapse.spacing.s14)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (selected) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
fun MarketplaceErrorPlaceholder(
    message: String?,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(MaterialTheme.synapse.spacing.s32),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("😕", style = MaterialTheme.typography.displayMedium)
        Spacer(Modifier.height(MaterialTheme.synapse.spacing.s16))
        Text(
            text = message ?: stringResource(R.string.synapse_marketplace_error_default),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium,
        )
        Spacer(Modifier.height(MaterialTheme.synapse.spacing.s24))
        Button(onClick = onRetry, shape = MaterialTheme.synapse.radius.md) {
            Text(stringResource(R.string.synapse_marketplace_retry))
        }
    }
}

@Composable
fun MarketplaceSectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Black,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier,
    )
}

@Composable
private fun getDifficultyString(diff: String): String {
    return when (diff.lowercase()) {
        "easy" -> stringResource(R.string.synapse_marketplace_diff_easy)
        "medium" -> stringResource(R.string.synapse_marketplace_diff_medium)
        "hard" -> stringResource(R.string.synapse_marketplace_diff_hard)
        else -> diff.replaceFirstChar { it.uppercase() }
    }
}
