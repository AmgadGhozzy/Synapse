package io.synapse.ai.features.marketplace.presentation.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBackIos
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import io.synapse.ai.R
import io.synapse.ai.core.theme.LocalSemanticColors
import io.synapse.ai.core.theme.synapse
import io.synapse.ai.core.theme.tokens.adp
import io.synapse.ai.core.theme.tokens.asp
import io.synapse.ai.core.theme.tokens.toShadow
import io.synapse.ai.core.ui.components.LoadingIndicator
import io.synapse.ai.features.marketplace.domain.MarketplacePack
import io.synapse.ai.features.marketplace.domain.MarketplacePackDetail

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PackDetailsBottomSheet(
    detail: MarketplacePackDetail?,
    isPro: Boolean,
    isAcquiring: Boolean,
    onDismiss: () -> Unit,
    onAcquire: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(
            topStart = MaterialTheme.synapse.spacing.s28,
            topEnd = MaterialTheme.synapse.spacing.s28
        ),
        containerColor = MaterialTheme.colorScheme.background,
        dragHandle = null,
    ) {
        if (detail == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(MaterialTheme.synapse.spacing.screenContentBottom),
                contentAlignment = Alignment.TopCenter
            ) {
                LoadingIndicator(size = 124.adp)
            }
        } else {
            PackDetailsContent(
                detail = detail,
                isPro = isPro,
                isAcquiring = isAcquiring,
                onBack = onDismiss,
                onAcquire = onAcquire,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun PackDetailsContent(
    detail: MarketplacePackDetail,
    isPro: Boolean,
    isAcquiring: Boolean,
    onBack: () -> Unit,
    onAcquire: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        LazyColumn(
            modifier = modifier,
            contentPadding = PaddingValues(bottom = 120.adp),
        ) {
            item { HeroSection(pack = detail.pack) }
            item { CurriculumHeader(moduleCount = detail.pack.modules.size) }
            itemsIndexed(detail.pack.modules, key = { idx, _ -> "mod_$idx" }) { idx, module ->
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(tween(200 + idx * 60)) +
                            slideInHorizontally(tween(200 + idx * 60)),
                ) {
                    ModuleCard(
                        index = idx,
                        title = module.title,
                        modifier = Modifier.padding(
                            horizontal = MaterialTheme.synapse.spacing.s20,
                            vertical = MaterialTheme.synapse.spacing.s4
                        ),
                    )
                }
            }
        }

        // Pinned bottom CTA
        AcquireButton(
            isOwned = detail.isOwned,
            isPro = isPro,
            isPremium = detail.pack.isPremium,
            isAcquiring = isAcquiring,
            onClick = onAcquire,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun HeroSection(pack: MarketplacePack) {
    val semantic = LocalSemanticColors.current

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(MaterialTheme.synapse.spacing.s28))
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Ambient blur decoration
        Box(
            modifier = Modifier
                .size(220.adp)
                .align(Alignment.TopEnd)
                .background(semantic.primaryBg, CircleShape)
                .blur(64.adp),
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.synapse.spacing.s24)
                .padding(
                    top = MaterialTheme.synapse.spacing.s32,
                    bottom = MaterialTheme.synapse.spacing.s28
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Emoji card
            Surface(
                shape = MaterialTheme.synapse.radius.xxxl,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .size(88.adp)
                    .dropShadow(
                        shape = MaterialTheme.synapse.radius.xxxl,
                        shadow = MaterialTheme.synapse.shadows.medium.toShadow(),
                    )
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text(text = pack.emoji ?: "📚", style = MaterialTheme.typography.displayMedium)
                }
            }

            Spacer(Modifier.height(MaterialTheme.synapse.spacing.s24))

            Text(
                text = pack.title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                letterSpacing = (-0.5).sp,
                modifier = Modifier.padding(horizontal = 16.adp),
            )

            Spacer(Modifier.height(MaterialTheme.synapse.spacing.s10))

            pack.description?.let { desc ->
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.widthIn(max = 300.adp),
                )
                Spacer(Modifier.height(MaterialTheme.synapse.spacing.s20))
            }

            // Tags
            if (pack.tags.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.Center) {
                    pack.tags.take(3).forEach { tag ->
                        Surface(
                            shape = MaterialTheme.synapse.radius.pill,
                            color = semantic.primaryBg,
                            modifier = Modifier.padding(horizontal = MaterialTheme.synapse.spacing.s4),
                        ) {
                            Text(
                                text = tag.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 1.asp,
                                modifier = Modifier.padding(horizontal = 12.adp, vertical = 5.adp),
                            )
                        }
                    }
                }
                Spacer(Modifier.height(MaterialTheme.synapse.spacing.s24))
            }

            StatsGrid(pack = pack)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────
// Stats grid
// ─────────────────────────────────────────────────────────────────────

@Composable
private fun StatsGrid(pack: MarketplacePack) {
    val langShort = pack.language.take(2).uppercase()
    val iqLabel = when (pack.difficulty?.lowercase()) {
        "easy" -> "80+"
        "medium" -> "110+"
        else -> "140+"
    }
    val stats = listOf(
        Triple(
            stringResource(R.string.synapse_marketplace_stat_cards),
            pack.questionCount.toString(),
            R.drawable.ic_book_open
        ),
        Triple(
            stringResource(R.string.synapse_marketplace_stat_time),
            "${pack.estimatedMinutes ?: 0}m",
            R.drawable.ic_clock
        ),
        Triple(
            stringResource(R.string.synapse_marketplace_stat_iq),
            iqLabel,
            R.drawable.ic_brain
        ),
        Triple(
            stringResource(R.string.synapse_marketplace_stat_lang),
            langShort,
            R.drawable.ic_globe,
        ),
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.synapse.spacing.s8)
    ) {
        stats.forEach { (label, value, icon) ->
            StatTile(label = label, value = value, icon = icon, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun StatTile(
    label: String,
    value: String,
    icon: Int,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .dropShadow(
                MaterialTheme.synapse.radius.md,
                MaterialTheme.synapse.shadows.subtle.toShadow()
            )
            .clip(MaterialTheme.synapse.radius.md)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(vertical = MaterialTheme.synapse.spacing.s14),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(MaterialTheme.synapse.spacing.s14)
        )
        Spacer(Modifier.height(MaterialTheme.synapse.spacing.s4))
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Black
        )
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
private fun CurriculumHeader(moduleCount: Int) {
    val semantic = LocalSemanticColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.adp)
            .padding(top = 28.adp, bottom = 12.adp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(
                stringResource(R.string.synapse_marketplace_curriculum),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                letterSpacing = (-0.3).sp
            )
            Text(
                stringResource(R.string.synapse_marketplace_modules_count, moduleCount),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.5.sp
            )
        }
        Surface(
            shape = MaterialTheme.synapse.radius.md,
            color = semantic.primaryBg,
            modifier = Modifier.size(MaterialTheme.synapse.spacing.s48)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(
                    painter = painterResource(R.drawable.ic_sparkles),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}


@Composable
private fun ModuleCard(index: Int, title: String, modifier: Modifier = Modifier) {
    val semantic = LocalSemanticColors.current
    Surface(
        shape = MaterialTheme.synapse.radius.xl,
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier
            .fillMaxWidth()
            .dropShadow(
                shape = MaterialTheme.synapse.radius.xl,
                shadow = MaterialTheme.synapse.shadows.subtle.toShadow(),
            )
    ) {
        Row(
            modifier = Modifier.padding(MaterialTheme.synapse.spacing.s16),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.synapse.spacing.s14),
        ) {
            Surface(
                shape = MaterialTheme.synapse.radius.md,
                color = semantic.primaryBg,
                modifier = Modifier.size(MaterialTheme.synapse.spacing.s32 + MaterialTheme.synapse.spacing.s8),
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text(
                        "${index + 1}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
fun AcquireButton(
    isOwned: Boolean,
    isPro: Boolean,
    isPremium: Boolean,
    isAcquiring: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val locked = isPremium && !isPro
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    0f to MaterialTheme.colorScheme.background.copy(alpha = 0f),
                    0.3f to MaterialTheme.colorScheme.background.copy(alpha = 0.9f),
                    1f to MaterialTheme.colorScheme.background,
                )
            )
            .padding(
                horizontal = MaterialTheme.synapse.spacing.s24,
                vertical = MaterialTheme.synapse.spacing.s20
            )
            .dropShadow(
                shape = MaterialTheme.synapse.radius.xl,
                shadow = MaterialTheme.synapse.shadows.cta.toShadow(),
            )
    ) {
        Button(
            onClick  = onClick,
            enabled  = !isAcquiring,
            modifier = Modifier
                .fillMaxWidth()
                .height(MaterialTheme.synapse.spacing.s56),
            shape = MaterialTheme.synapse.radius.xl,
            colors = ButtonDefaults.buttonColors(
                containerColor = when {
                    isOwned -> MaterialTheme.colorScheme.secondary
                    locked  -> MaterialTheme.colorScheme.tertiary
                    else    -> MaterialTheme.colorScheme.primary
                }
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.adp),
        ) {
            if (isAcquiring) {
                CircularProgressIndicator(
                    modifier = Modifier.size(MaterialTheme.synapse.spacing.s24),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = MaterialTheme.synapse.spacing.s2,
                )

            } else when {
                isOwned -> {
                    Icon(
                        Icons.AutoMirrored.Rounded.ArrowBackIos,
                        contentDescription = null,
                        modifier = Modifier.size(MaterialTheme.synapse.spacing.s24)
                    )
                    Spacer(Modifier.width(MaterialTheme.synapse.spacing.s8))
                    Text(
                        stringResource(R.string.synapse_marketplace_start_studying),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.8.sp
                    )
                }
                locked -> {
                    Icon(
                        painter = painterResource(R.drawable.ic_crown),
                        contentDescription = null,
                        modifier = Modifier.size(MaterialTheme.synapse.spacing.s24)
                    )
                    Spacer(Modifier.width(MaterialTheme.synapse.spacing.s8))
                    Text(
                        stringResource(R.string.synapse_marketplace_unlock_pro),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.8.sp
                    )
                }
                else -> {
                    Text(
                        stringResource(R.string.synapse_marketplace_get_pack),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.8.sp
                    )
                }
            }
        }
    }
}
