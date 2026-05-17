package io.synapse.ai.features.premium.presentation.screen

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.synapse.ai.R
import io.synapse.ai.core.theme.synapse
import io.synapse.ai.core.theme.tokens.adp
import io.synapse.ai.core.theme.tokens.toShadow
import io.synapse.ai.core.ui.components.LoadingContent
import io.synapse.ai.core.ui.components.SnackbarHost
import io.synapse.ai.core.ui.components.rememberSnackbarController
import io.synapse.ai.features.premium.presentation.components.AmbientOrb
import io.synapse.ai.features.premium.presentation.components.CtaSection
import io.synapse.ai.features.premium.presentation.components.HeroSection
import io.synapse.ai.features.premium.presentation.components.PlansRow
import io.synapse.ai.features.premium.presentation.components.PremiumErrorContent
import io.synapse.ai.features.premium.presentation.components.PremiumFeaturesCard
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
    val scrollState = rememberScrollState()
    var bottomSheetHeight by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current

    Box(modifier = modifier.fillMaxSize()) {
        AmbientOrb(
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            size = 280.adp,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 70.adp, y = (-90).adp),
        )
        AmbientOrb(
            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.08f),
            size = 240.adp,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = (-60).adp, y = 120.adp),
        )

        // Top Section (Scrollable) - Fills entire screen so it passes under the bottom sheet
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = MaterialTheme.synapse.spacing.screen)
        ) {
            Spacer(Modifier.height(MaterialTheme.synapse.spacing.s10))

            HeroSection()
            Spacer(Modifier.height(MaterialTheme.synapse.spacing.s10))
            PremiumFeaturesCard(features = state.features)

            // Dynamic space to scroll past the bottom sheet
            Spacer(Modifier.height(with(density) { bottomSheetHeight.toDp() } + 20.adp))
        }

        // Bottom Section (Static/Pinned)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .onGloballyPositioned { bottomSheetHeight = it.size.height }
                .dropShadow(
                    shape = RoundedCornerShape(topStart = 32.adp, topEnd = 32.adp),
                    shadow = MaterialTheme.synapse.shadows.strong.toShadow()
                )
                .background(
                    brush = MaterialTheme.synapse.gradients.page,
                    shape = RoundedCornerShape(topStart = 32.adp, topEnd = 32.adp)
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MaterialTheme.synapse.spacing.screen)
                    .padding(top = MaterialTheme.synapse.spacing.s12)
                    .navigationBarsPadding()
            ) {
                PlansRow(
                    plans = state.products,
                    selectedPlanId = state.selectedSkuId,
                    onPlanSelected = onPlanSelected
                )

                Spacer(Modifier.height(MaterialTheme.synapse.spacing.s18))

                CtaSection(
                    isPurchasing = state.isPurchasing,
                    socialProof = null,
                    selectedSkuId = state.selectedSkuId,
                    products = state.products,
                    onStartTrial = onStartTrial,
                    onDismiss = onDismiss
                )
            }
        }

        // Sticky Close Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = MaterialTheme.synapse.spacing.screen,
                    end = MaterialTheme.synapse.spacing.screen
                ),
            contentAlignment = Alignment.CenterEnd,
        ) {
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(48.adp),
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
}
