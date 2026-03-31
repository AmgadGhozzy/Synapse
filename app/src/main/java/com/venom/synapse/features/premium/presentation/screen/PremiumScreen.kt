package com.venom.synapse.features.premium.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.venom.synapse.R
import com.venom.synapse.core.theme.synapse
import com.venom.synapse.core.ui.components.LoadingContent
import com.venom.synapse.core.ui.components.SnackbarHost
import com.venom.synapse.core.ui.components.rememberSnackbarController
import com.venom.synapse.features.premium.presentation.components.AmbientOrb
import com.venom.synapse.features.premium.presentation.components.CtaSection
import com.venom.synapse.features.premium.presentation.components.HeroSection
import com.venom.synapse.features.premium.presentation.components.PlansRow
import com.venom.synapse.features.premium.presentation.components.PremiumErrorContent
import com.venom.synapse.features.premium.presentation.components.PremiumFeaturesCard
import com.venom.synapse.features.premium.presentation.components.SectionLabel
import com.venom.synapse.features.premium.presentation.state.PremiumEvent
import com.venom.synapse.features.premium.presentation.state.PremiumUiState
import com.venom.synapse.features.premium.presentation.viewmodel.PremiumViewModel
import com.venom.ui.components.common.adp

@Composable
fun SynapsePremiumScreen(
    onDismiss        : () -> Unit,
    onPurchaseSuccess: (planId: String) -> Unit,
    modifier         : Modifier = Modifier,
    viewModel        : PremiumViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarController = rememberSnackbarController()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is PremiumEvent.PurchaseSuccess -> onPurchaseSuccess(event.planId)
                is PremiumEvent.Dismissed       -> onDismiss()
                is PremiumEvent.RequiresSignIn  -> snackbarController.error("Sign in required to purchase Premium.")
                is PremiumEvent.PurchaseFailed  -> snackbarController.error(event.reason)
                is PremiumEvent.ShowSnackbar    -> snackbarController.success(event.message)
            }
        }
    }

    Scaffold(
        snackbarHost        = { snackbarController.SnackbarHost() },
        modifier            = modifier,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor      = Color.Transparent,
    ) { innerPadding ->
        when (val state = uiState) {
            is PremiumUiState.Loading      -> LoadingContent(Modifier.padding(innerPadding))
            is PremiumUiState.AlreadyPremium -> AlreadyPremiumContent(
                onDismiss = onDismiss,
                modifier  = Modifier.padding(innerPadding),
            )
            is PremiumUiState.Error        -> PremiumErrorContent(
                message  = state.message,
                onRetry  = viewModel::loadConfig,
                modifier = Modifier.padding(innerPadding),
            )
            is PremiumUiState.Ready        -> PremiumReadyContent(
                state          = state,
                onPlanSelected = viewModel::selectPlan,
                onStartTrial   = viewModel::startPurchase,
                onDismiss      = viewModel::dismiss,
                modifier       = Modifier.padding(innerPadding),
            )
        }
    }
}

@Composable
private fun PremiumReadyContent(
    state         : PremiumUiState.Ready,
    onPlanSelected: (String) -> Unit,
    onStartTrial  : () -> Unit,
    onDismiss     : () -> Unit,
    modifier      : Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.synapse.gradients.page),
    ) {
        AmbientOrb(
            color    = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            size     = 280.adp,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 60.adp, y = (-80).adp),
        )
        AmbientOrb(
            color    = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.08f),
            size     = 240.adp,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = (-60).adp, y = 120.adp),
        )

        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(
                        end = MaterialTheme.synapse.spacing.s24,
                        top = MaterialTheme.synapse.spacing.s20,
                    ),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Icon(
                    imageVector        = Icons.AutoMirrored.Rounded.ArrowForwardIos,
                    contentDescription = stringResource(R.string.premium_close),
                    tint               = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.8f),
                    modifier           = Modifier.size(24.adp),
                )
            }

            LazyColumn(
                contentPadding = PaddingValues(bottom = MaterialTheme.synapse.spacing.s32),
                modifier       = Modifier.fillMaxSize(),
            ) {
                item { HeroSection(trialDays = state.trialDays) }

                item {
                    SectionLabel(
                        text     = stringResource(R.string.premium_section_features),
                        modifier = Modifier.padding(
                            start   = MaterialTheme.synapse.spacing.s20,
                            end     = MaterialTheme.synapse.spacing.s20,
                            top     = MaterialTheme.synapse.spacing.s4,
                            bottom  = MaterialTheme.synapse.spacing.s10,
                        ),
                    )
                }

                item { PremiumFeaturesCard(features = state.features) }

                item {
                    SectionLabel(
                        text     = stringResource(R.string.premium_section_plans),
                        modifier = Modifier.padding(
                            start   = MaterialTheme.synapse.spacing.s20,
                            end     = MaterialTheme.synapse.spacing.s20,
                            top     = MaterialTheme.synapse.spacing.s20,
                            bottom  = MaterialTheme.synapse.spacing.s10,
                        ),
                    )
                }

                item {
                    PlansRow(
                        plans          = state.plans,
                        selectedPlanId = state.selectedPlanId,
                        onPlanSelected = onPlanSelected,
                        modifier       = Modifier.padding(horizontal = MaterialTheme.synapse.spacing.s20),
                    )
                }

                item {
                    Spacer(Modifier.height(MaterialTheme.synapse.spacing.s20))
                    CtaSection(
                        trialDays    = state.trialDays,
                        isPurchasing = state.isPurchasing,
                        socialProof  = state.socialProof,
                        onStartTrial = onStartTrial,
                        onDismiss    = onDismiss,
                        modifier     = Modifier.padding(horizontal = MaterialTheme.synapse.spacing.s20),
                    )
                }
            }
        }
    }
}