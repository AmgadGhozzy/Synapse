package io.synapse.ai.features.premium.presentation.screen

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.synapse.ai.R
import io.synapse.ai.core.theme.synapse
import io.synapse.ai.core.theme.tokens.adp
import io.synapse.ai.core.ui.components.LoadingContent
import io.synapse.ai.core.ui.components.SnackbarHost
import io.synapse.ai.core.ui.components.rememberSnackbarController
import io.synapse.ai.features.premium.presentation.components.AmbientOrb
import io.synapse.ai.features.premium.presentation.components.CtaSection
import io.synapse.ai.features.premium.presentation.components.HeroSection
import io.synapse.ai.features.premium.presentation.components.PlansRow
import io.synapse.ai.features.premium.presentation.components.PremiumErrorContent
import io.synapse.ai.features.premium.presentation.components.PremiumFeaturesCard
import io.synapse.ai.features.premium.presentation.components.SectionLabel
import io.synapse.ai.features.premium.presentation.state.PremiumEvent
import io.synapse.ai.features.premium.presentation.state.PremiumUiState
import io.synapse.ai.features.premium.presentation.viewmodel.PremiumViewModel
import io.synapse.ai.ui.openSubscriptionManagement
import kotlinx.coroutines.delay

@Composable
fun SynapsePremiumScreen(
    onDismiss: () -> Unit,
    onPurchaseSuccess: (planId: String) -> Unit,
    onNavigateToProfile: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PremiumViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarController = rememberSnackbarController()
    val context = LocalContext.current
    val activity = context as? Activity

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is PremiumEvent.PurchaseSuccess -> onPurchaseSuccess(event.skuId)
                is PremiumEvent.Dismissed -> onDismiss()
                is PremiumEvent.NavigateToProfile -> {
                    snackbarController.success("Please sign in to continue")
                    delay(1200)
                    onNavigateToProfile()
                }

                is PremiumEvent.RequiresSignIn -> snackbarController.error(
                    event.message.asString(
                        context
                    )
                )

                is PremiumEvent.PurchaseFailed -> snackbarController.error(event.reason)
                is PremiumEvent.ShowSnackbar -> snackbarController.success(event.message)
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { snackbarController.SnackbarHost() },
        containerColor = Color.Transparent,
    ) { innerPadding ->
        when (val state = uiState) {
            is PremiumUiState.Loading -> LoadingContent(Modifier.padding(innerPadding))
            is PremiumUiState.AlreadyPremium -> AlreadyPremiumContent(
                onDismiss = onDismiss,
                openSubManage = { openSubscriptionManagement(context) },
                onRestore = viewModel::restorePurchases,
                modifier = Modifier.padding(innerPadding),
            )

            is PremiumUiState.Error -> PremiumErrorContent(
                message = state.message,
                onRetry = viewModel::loadPaywall,
                modifier = Modifier.padding(innerPadding),
            )

            is PremiumUiState.Ready -> PremiumReadyContent(
                state = state,
                onPlanSelected = viewModel::selectPlan,
                onStartTrial = { activity?.let { viewModel.startPurchase(it) } },
                onDismiss = viewModel::dismiss,
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}

@Composable
private fun PremiumReadyContent(
    state: PremiumUiState.Ready,
    onPlanSelected: (String) -> Unit,
    onStartTrial: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        AmbientOrb(
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            size = 280.adp,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 60.adp, y = (-80).adp),
        )
        AmbientOrb(
            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.08f),
            size = 240.adp,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = (-60).adp, y = 120.adp),
        )
        LazyColumn(
            contentPadding = PaddingValues(
                MaterialTheme.synapse.spacing.screen,
            ),
            modifier = Modifier.fillMaxSize(),
        ) {
            stickyHeader {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Transparent)
                        .padding(),
                    contentAlignment = Alignment.CenterEnd,
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(40.adp),
                        shape = CircleShape,
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowForwardIos,
                            contentDescription = stringResource(R.string.premium_close),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.8f),
                        )
                    }
                }
            }

            item { HeroSection(trialDays = state.trialDays) }

            item {
                SectionLabel(
                    text = stringResource(R.string.premium_section_features),
                    modifier = Modifier.padding(
                        top = MaterialTheme.synapse.spacing.s4,
                        bottom = MaterialTheme.synapse.spacing.s10,
                    ),
                )
            }

            item { PremiumFeaturesCard(features = state.features) }

            item {
                SectionLabel(
                    text = stringResource(R.string.premium_section_plans),
                    modifier = Modifier.padding(
                        top = MaterialTheme.synapse.spacing.s20,
                        bottom = MaterialTheme.synapse.spacing.s10,
                    ),
                )
            }

            item {
                PlansRow(
                    products = state.products,
                    selectedSkuId = state.selectedSkuId,
                    onPlanSelected = onPlanSelected
                )
            }

            item {
                Spacer(Modifier.height(MaterialTheme.synapse.spacing.s20))
                CtaSection(
                    trialDays = state.trialDays,
                    isPurchasing = state.isPurchasing,
                    socialProof = state.socialProof,
                    selectedSkuId = state.selectedSkuId,
                    products = state.products,
                    onStartTrial = onStartTrial,
                    onDismiss = onDismiss
                )
            }
        }
    }
}
