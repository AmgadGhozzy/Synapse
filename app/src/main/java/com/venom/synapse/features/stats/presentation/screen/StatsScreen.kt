package com.venom.synapse.features.stats.presentation.screen

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.venom.synapse.R
import com.venom.synapse.core.theme.SynapseTheme
import com.venom.synapse.core.theme.tokens.Spacing
import com.venom.synapse.features.stats.presentation.components.ActivityChartCard
import com.venom.synapse.features.stats.presentation.components.AiInsightCard
import com.venom.synapse.features.stats.presentation.components.RetentionChartCard
import com.venom.synapse.features.stats.presentation.components.StatCardsGrid
import com.venom.synapse.features.stats.presentation.components.StreakHeroCard
import com.venom.synapse.features.stats.presentation.state.DayActivity
import com.venom.synapse.features.stats.presentation.state.StatsUiState
import com.venom.synapse.features.stats.presentation.viewmodel.StatsViewModel
import com.venom.ui.components.common.adp

@Composable
fun StatsScreen(
    modifier: Modifier = Modifier,
    viewModel: StatsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    Scaffold(
        modifier            = modifier,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor      = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        StatsContent(
            state = state,
            modifier = modifier.fillMaxSize().padding(innerPadding),
        )
    }
}

@Composable
private fun StatsContent(
    state: StatsUiState,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(bottom = 172.adp, top = 132.adp),
        verticalArrangement = Arrangement.spacedBy(Spacing.ListItemVerticalGap),
    ) {
        item {
            StatsHeader(
                state,
                modifier = Modifier.padding(horizontal = Spacing.ScreenHorizontalPadding),
            )
        }
        item {
            StreakHeroCard(
                currentStreak    = state.currentStreak,
                bestStreak       = state.bestStreak,
                streakActiveDays = state.streakActiveDays,
                todayDayIndex    = state.todayDayIndex,
                modifier         = Modifier.padding(horizontal = Spacing.ScreenHorizontalPadding),
            )
        }
        item {
            AiInsightCard(
                accuracyDelta = state.averageAccuracy,
                modifier      = Modifier.padding(bottom = Spacing.ListItemVerticalGap),
            )
        }
        item {
            StatCardsGrid(
                state    = state,
                modifier = Modifier.padding(horizontal = Spacing.ScreenHorizontalPadding),
            )
        }
        item {
            RetentionChartCard(
                weeklyActivity    = state.weeklyActivity,
                retentionDeltaPct = state.retentionDeltaPct,
                modifier          = Modifier.padding(horizontal = Spacing.ScreenHorizontalPadding),
            )
        }
        item {
            ActivityChartCard(
                weeklyActivity   = state.weeklyActivity,
                totalWeeklyCards = state.totalWeeklyCards,
                modifier         = Modifier.padding(horizontal = Spacing.ScreenHorizontalPadding),
            )
        }
    }
}

@Composable
private fun StatsHeader(
    state: StatsUiState,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = !state.isLoading,
        enter = fadeIn(tween(300)) + slideInVertically(tween(350)) { -8 },
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.adp)) {
                Text(
                    text = stringResource(R.string.stats_header_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(
                        R.string.stats_header_subtitle,
                        state.totalPacks,
                        state.totalQuestions,
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Preview(name = "StatsScreen · Light", showBackground = true)
@Preview(
    name = "StatsScreen · Dark",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true
)
@Composable
private fun StatsScreenPreview() {
    SynapseTheme {
        val previewState = StatsUiState(
            totalPacks = 5,
            totalQuestions = 120,
            currentStreak = 7,
            bestStreak = 21,
            streakActiveDays = listOf(true, true, true, true, true, false, false),
            todayDayIndex = 4,
            averageAccuracy = 0.77f,
            retentionDeltaPct = 4,
            totalWeeklyCards = 270,
            timeStudiedHours = 4.2f,
            bestDayLabel = "Thu",
            bestDayCards = 65,
            weeklyActivity = listOf(
                DayActivity("Mon", 32, 0.68f),
                DayActivity("Tue", 48, 0.74f),
                DayActivity("Wed", 21, 0.70f),
                DayActivity("Thu", 65, 0.82f),
                DayActivity("Fri", 54, 0.79f),
                DayActivity("Sat", 38, 0.85f),
                DayActivity("Sun", 12, 0.77f),
            ),
            isLoading = false,
        )
        StatsContent(previewState)
    }
}
