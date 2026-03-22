package com.venom.synapse.features.profile.presentation.screen

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.venom.domain.model.AppTheme
import com.venom.synapse.R
import com.venom.synapse.core.theme.SynapseTheme
import com.venom.synapse.core.theme.synapse
import com.venom.synapse.core.ui.state.UiEffect
import com.venom.synapse.core.ui.components.rememberSnackbarController
import com.venom.synapse.core.ui.components.SnackbarHost
import com.venom.synapse.features.profile.presentation.components.DeleteAccountConfirmDialog
import com.venom.synapse.features.profile.presentation.components.DestructiveSettingsRow
import com.venom.synapse.features.profile.presentation.components.LifetimeProgressCard
import com.venom.synapse.features.profile.presentation.components.PremiumBannerCard
import com.venom.synapse.features.profile.presentation.components.ProfileAvatarSection
import com.venom.synapse.features.profile.presentation.components.ProfileChevron
import com.venom.synapse.features.profile.presentation.components.ProfileSettingsRow
import com.venom.synapse.features.profile.presentation.components.ProfileSettingsSection
import com.venom.synapse.features.profile.presentation.components.ProfileSignOutRow
import com.venom.synapse.features.profile.presentation.components.ProfileStatRow
import com.venom.synapse.features.profile.presentation.components.ReminderTimePickerDialog
import com.venom.synapse.features.profile.presentation.components.StepperRow
import com.venom.synapse.features.profile.presentation.components.SynapseSwitch
import com.venom.synapse.features.profile.presentation.components.TimeDisplayRow
import com.venom.synapse.features.profile.presentation.state.ProfileUiState
import com.venom.synapse.features.profile.presentation.state.StudySettingsUiState
import com.venom.synapse.features.profile.presentation.viewmodel.ProfileViewModel
import com.venom.synapse.features.profile.presentation.viewmodel.StudySettingsViewModel
import com.venom.ui.components.common.adp
import com.venom.ui.components.onboarding.GoogleSignInButton
import com.venom.ui.viewmodel.SettingsViewModel

// ── Screen entry ──────────────────────────────────────────────────────────────

@Composable
fun ProfileScreen(
    onNavigate: (String) -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarController = rememberSnackbarController()

    LaunchedEffect(Unit) {
        viewModel.uiEffects.collect { effect ->
            when (effect) {
                is UiEffect.Navigate      -> onNavigate(effect.route)
                is UiEffect.OpenExternal  -> onNavigate(effect.url)
                is UiEffect.ShowToast     -> snackbarController.success(effect.message)
                is UiEffect.ShowError     -> snackbarController.error(effect.message)
                else                      -> Unit
            }
        }
    }

    Scaffold(
        modifier            = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor      = MaterialTheme.colorScheme.background,
        snackbarHost        = { snackbarController.SnackbarHost() },
    ) { innerPadding ->
        ProfileContent(
            uiState             = uiState,
            onUpgrade           = viewModel::onUpgradeTapped,
            onPrivacy           = viewModel::onPrivacyTapped,
            onHelp              = viewModel::onHelpTapped,
            onRateApp           = viewModel::onRateAppTapped,
            onClearAllData      = viewModel::onClearAllData,
            onDeleteAccount     = viewModel::onDeleteAccount,
            onSignOut           = viewModel::onSignOut,
            onGoogleSignIn      = viewModel::onGoogleSignIn,
            modifier            = Modifier.padding(innerPadding),
        )
    }
}

// ── Content ───────────────────────────────────────────────────────────────────

@Composable
private fun ProfileContent(
    uiState: ProfileUiState,
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    studyViewModel: StudySettingsViewModel = hiltViewModel(),
    onUpgrade: () -> Unit,
    onPrivacy: () -> Unit,
    onHelp: () -> Unit,
    onRateApp: () -> Unit,
    onExportData: () -> Unit = {},
    onClearAllData: () -> Unit = {},
    onDeleteAccount: () -> Unit,
    onSignOut: () -> Unit,
    onGoogleSignIn: (android.content.Context) -> Unit,
    modifier: Modifier = Modifier,
) {
    val settings      by settingsViewModel.uiState.collectAsStateWithLifecycle()
    val studySettings by studyViewModel.uiState.collectAsStateWithLifecycle()
    val isDark = settings.themePrefs.appTheme == AppTheme.DARK

    val synapse  = MaterialTheme.synapse
    val semantic = synapse.semantic
    val cs       = MaterialTheme.colorScheme

    // Local UI state — time picker + delete account dialog visibility
    var showTimePicker         by rememberSaveable { mutableStateOf(false) }
    var showDeleteAccountDialog by rememberSaveable { mutableStateOf(false) }

    // Show M3 TimePicker dialog when requested
    if (showTimePicker) {
        ReminderTimePickerDialog(
            initialHour   = studySettings.reminderHour,
            initialMinute = studySettings.reminderMinute,
            onDismiss     = { showTimePicker = false },
            onConfirm     = { h, m ->
                studyViewModel.updateReminderTime(h, m)
                showTimePicker = false
            },
        )
    }

    // Delete Account confirmation — shown before the irreversible action
    if (showDeleteAccountDialog) {
        DeleteAccountConfirmDialog(
            userEmail = uiState.userEmail,
            onDismiss = { showDeleteAccountDialog = false },
            onConfirm = {
                showDeleteAccountDialog = false
                onDeleteAccount()
            },
        )
    }

    LazyColumn(
        modifier            = modifier.fillMaxSize(),
        contentPadding      = PaddingValues(bottom = 96.adp, top = 132.adp),
        verticalArrangement = Arrangement.spacedBy(20.adp),
    ) {

        // ── Avatar ────────────────────────────────────────────────────────────
        item {
            ProfileAvatarSection(
                uiState  = uiState,
                gradient = synapse.gradients.primary,
                modifier = Modifier.padding(horizontal = 20.adp),
            )
        }

        // ── Google sign-in prompt (anonymous) ────────────────────────────────
        if (uiState.isAnonymous) {
            item {
                val context = LocalContext.current
                GoogleSignInButton(
                    onClick  = { onGoogleSignIn(context) },
                    modifier = Modifier.padding(horizontal = 20.adp),
                )
            }
        }

        // ── Lifetime Progress card ────────────────────────────────────────────
        item {
            LifetimeProgressCard(
                cardsLearned    = uiState.totalCardsLearned,
                studyTimeHours  = uiState.studyTimeHours,
                avgRetentionPct = uiState.avgRetentionPct,
                modifier        = Modifier.padding(horizontal = 20.adp),
            )
        }

        // ── Premium banner (free users only) ─────────────────────────────────
        if (!uiState.isPremium) {
            item {
                PremiumBannerCard(
                    gold      = semantic.gold,
                    goldGrad  = synapse.gradients.goPro,
                    bgGrad    = synapse.gradients.streakHero,
                    onUpgrade = onUpgrade,
                    modifier  = Modifier.padding(horizontal = 20.adp),
                )
            }
        }

        // ── 1. Study Settings ─────────────────────────────────────────────────
        item {
            StudySettingsSection(
                studySettings = studySettings,
                studyViewModel = studyViewModel,
                modifier      = Modifier.padding(horizontal = 20.adp),
            )
        }

        // ── 2. Appearance ─────────────────────────────────────────────────────
        item {
            ProfileSettingsSection(
                title    = stringResource(R.string.profile_section_preferences),
                modifier = Modifier.padding(horizontal = 20.adp),
            ) {
                ProfileSettingsRow(
                    label     = stringResource(R.string.profile_dark_mode),
                    subLabel  = if (isDark) stringResource(R.string.profile_dark_mode_on)
                    else        stringResource(R.string.profile_dark_mode_off),
                    iconRes   = R.drawable.ic_moon,
                    iconTint  = cs.secondary,
                    iconBg    = cs.secondaryContainer.copy(alpha = 0.5f),
                    hasDivider = false,
                    onClick   = { settingsViewModel.updateAppTheme(if (isDark) AppTheme.LIGHT else AppTheme.DARK) },
                ) {
                    SynapseSwitch(
                        checked         = isDark,
                        onCheckedChange = { settingsViewModel.updateAppTheme(if (it) AppTheme.DARK else AppTheme.LIGHT) },
                    )
                }
            }
        }

//        // ── 3. Notifications ──────────────────────────────────────────────────
//        item {
//            NotificationsSection(
//                studySettings  = studySettings,
//                onToggle       = studyViewModel::updateDailyReminder,
//                onTimeRowTap   = { showTimePicker = true },
//                modifier       = Modifier.padding(horizontal = 20.adp),
//            )
//        }

//        // ── 4. Data Management ────────────────────────────────────────────────
//        item {
//            DataManagementSection(
//                onExportData   = onExportData,
//                onClearAllData = onClearAllData,
//                modifier       = Modifier.padding(horizontal = 20.adp),
//            )
//        }

        // ── 5. Account ────────────────────────────────────────────────────────
        item {
            AccountSection(
                onPrivacy          = onPrivacy,
                onHelp             = onHelp,
                onRateApp          = onRateApp,
                onDeleteAccount    = { showDeleteAccountDialog = true },
                modifier           = Modifier.padding(horizontal = 20.adp),
            )
        }

        // ── Sign Out ──────────────────────────────────────────────────────────
        item {
            ProfileSignOutRow(
                onClick  = onSignOut,
                modifier = Modifier.padding(horizontal = 20.adp),
            )
        }
    }
}

// ── Section composables ───────────────────────────────────────────────────────
@Composable
private fun StudySettingsSection(
    studySettings: StudySettingsUiState,
    studyViewModel: StudySettingsViewModel,
    modifier: Modifier = Modifier,
) {
    val cs = MaterialTheme.colorScheme
    ProfileSettingsSection(
        title    = stringResource(R.string.settings_section_study),
        modifier = modifier,
    ) {
        StepperRow(
            label      = stringResource(R.string.settings_daily_goal),
            subLabel   = stringResource(R.string.settings_daily_goal_sub),
            iconRes    = R.drawable.ic_target,
            iconTint   = cs.primary,
            iconBg     = cs.primaryContainer.copy(alpha = 0.5f),
            value      = studySettings.dailyGoal,
            unit       = stringResource(R.string.settings_unit_cards),
            min        = StudySettingsViewModel.DAILY_GOAL_MIN,
            max        = StudySettingsViewModel.DAILY_GOAL_MAX,
            onIncrement = studyViewModel::incrementDailyGoal,
            onDecrement = studyViewModel::decrementDailyGoal,
            hasDivider = true,
        )
        StepperRow(
            label      = stringResource(R.string.settings_new_cards_day),
            subLabel   = stringResource(R.string.settings_new_cards_day_sub),
            iconRes    = R.drawable.ic_brain,
            iconTint   = MaterialTheme.synapse.semantic.success,
            iconBg     = MaterialTheme.synapse.semantic.success.copy(alpha = 0.12f),
            value      = studySettings.newCardsPerDay,
            unit       = stringResource(R.string.settings_unit_cards),
            min        = StudySettingsViewModel.NEW_PER_DAY_MIN,
            max        = StudySettingsViewModel.NEW_PER_DAY_MAX,
            onIncrement = studyViewModel::incrementNewPerDay,
            onDecrement = studyViewModel::decrementNewPerDay,
            hasDivider = true,
        )
        StepperRow(
            label      = stringResource(R.string.settings_review_limit),
            subLabel   = stringResource(R.string.settings_review_limit_sub),
            iconRes    = R.drawable.ic_refresh_cw,
            iconTint   = MaterialTheme.synapse.semantic.gold,
            iconBg     = MaterialTheme.synapse.semantic.gold.copy(alpha = 0.12f),
            value      = studySettings.reviewLimit,
            unit       = stringResource(R.string.settings_unit_cards),
            min        = StudySettingsViewModel.REVIEW_MIN,
            max        = StudySettingsViewModel.REVIEW_MAX,
            onIncrement = studyViewModel::incrementReviewLimit,
            onDecrement = studyViewModel::decrementReviewLimit,
            hasDivider = false,
        )
    }
}

@Composable
private fun NotificationsSection(
    studySettings: StudySettingsUiState,
    onToggle: (Boolean) -> Unit,
    onTimeRowTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val cs = MaterialTheme.colorScheme
    ProfileSettingsSection(
        title    = stringResource(R.string.settings_section_notifications),
        modifier = modifier,
    ) {
        // Daily Reminder toggle
        ProfileSettingsRow(
            label      = stringResource(R.string.settings_daily_reminder),
            subLabel   = stringResource(R.string.settings_daily_reminder_sub),
            iconRes    = R.drawable.ic_bell,
            iconTint   = cs.secondary,
            iconBg     = cs.secondaryContainer.copy(alpha = 0.5f),
            hasDivider = studySettings.dailyReminderEnabled,
            onClick    = { onToggle(!studySettings.dailyReminderEnabled) },
        ) {
            SynapseSwitch(
                checked         = studySettings.dailyReminderEnabled,
                onCheckedChange = onToggle,
            )
        }

        // Reminder Time row — animated show/hide
        AnimatedVisibility(
            visible = studySettings.dailyReminderEnabled,
            enter   = expandVertically() + fadeIn(),
            exit    = shrinkVertically() + fadeOut(),
        ) {
            TimeDisplayRow(
                label    = stringResource(R.string.settings_reminder_time),
                subLabel = stringResource(R.string.settings_reminder_time_sub),
                iconRes  = R.drawable.ic_bell,
                iconTint = cs.secondary,
                iconBg   = cs.secondaryContainer.copy(alpha = 0.35f),
                hour     = studySettings.reminderHour,
                minute   = studySettings.reminderMinute,
                onTap    = onTimeRowTap,
            )
        }
    }
}

@Composable
private fun DataManagementSection(
    onExportData: () -> Unit,
    onClearAllData: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ProfileSettingsSection(
        title    = stringResource(R.string.settings_section_data),
        modifier = modifier,
    ) {
        ProfileSettingsRow(
            label      = stringResource(R.string.settings_export_data),
            subLabel   = stringResource(R.string.settings_export_data_sub),
            iconRes    = R.drawable.ic_download,
            iconTint   = MaterialTheme.synapse.semantic.success,
            iconBg     = MaterialTheme.synapse.semantic.success.copy(alpha = 0.12f),
            hasDivider = true,
            onClick    = onExportData,
        ) { ProfileChevron() }

        DestructiveSettingsRow(
            label      = stringResource(R.string.settings_clear_data),
            subLabel   = stringResource(R.string.settings_clear_data_sub),
            iconRes    = R.drawable.ic_trash_2,
            hasDivider = false,
            onClick    = onClearAllData,
        )
    }
}

@Composable
private fun AccountSection(
    onPrivacy: () -> Unit,
    onHelp: () -> Unit,
    onRateApp: () -> Unit,
    onDeleteAccount: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val cs = MaterialTheme.colorScheme
    ProfileSettingsSection(
        title    = stringResource(R.string.profile_section_support),
        modifier = modifier,
    ) {
        ProfileSettingsRow(
            label      = stringResource(R.string.profile_privacy_policy),
            subLabel   = stringResource(R.string.profile_privacy_policy_sub),
            iconRes    = R.drawable.ic_shield,
            iconTint   = cs.onSurfaceVariant,
            iconBg     = cs.primaryContainer.copy(alpha = 0.25f),
            hasDivider = true,
            onClick    = onPrivacy,
        ) { ProfileChevron() }

        ProfileSettingsRow(
            label      = stringResource(R.string.profile_help_faq),
            subLabel   = stringResource(R.string.profile_help_faq_sub),
            iconRes    = R.drawable.ic_help_circle,
            iconTint   = cs.onSurfaceVariant,
            iconBg     = cs.primaryContainer.copy(alpha = 0.25f),
            hasDivider = true,
            onClick    = onHelp,
        ) { ProfileChevron() }

        ProfileSettingsRow(
            label      = stringResource(R.string.profile_rate_app),
            subLabel   = stringResource(R.string.profile_rate_app_sub),
            iconRes    = R.drawable.ic_star,
            iconTint   = MaterialTheme.synapse.semantic.gold,
            iconBg     = MaterialTheme.synapse.semantic.goldContainer,
            hasDivider = true,
            onClick    = onRateApp,
        ) { ProfileChevron() }

        DestructiveSettingsRow(
            label      = stringResource(R.string.settings_delete_account),
            subLabel   = stringResource(R.string.settings_delete_account_sub),
            iconRes    = R.drawable.ic_user_x,
            hasDivider = false,
            onClick    = onDeleteAccount,
        )
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

private val previewProfile = ProfileUiState(
    userName        = "Alex Johnson",
    userEmail       = "alex.johnson@email.com",
    planLabel       = "Free Plan",
    avatarInitial   = 'A',
    packCount       = 4,
    cardCount       = 470,
    streakDays      = 7,
    isLoading       = false,
    totalCardsLearned = 470,
    studyTimeHours  = 12.4f,
    avgRetentionPct = 0.77f,
)

private val previewStudy = StudySettingsUiState(
    dailyGoal            = 20,
    newCardsPerDay       = 10,
    reviewLimit          = 100,
    dailyReminderEnabled = true,
    reminderHour         = 8,
    reminderMinute       = 0,
)

@Preview(name = "StudySettingsSection · Light", showBackground = true)
@Preview(name = "StudySettingsSection · Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun StudySettingsSectionPreview() {
    SynapseTheme {
        // StudySettingsSection needs ViewModel — render standalone for preview
        ProfileSettingsSection(
            title    = "Study Settings",
            modifier = Modifier.padding(16.adp),
        ) {
            Text(
                text     = "3 stepper rows (Daily Goal / New Cards / Review Limit)",
                style    = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Normal,
                color    = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(16.adp),
            )
        }
    }
}

@Preview(name = "LifetimeProgress + Stats · Light", showBackground = true)
@Preview(name = "LifetimeProgress + Stats · Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun LifetimeProgressPreview() {
    SynapseTheme {
        val semantic = MaterialTheme.synapse.semantic
        androidx.compose.foundation.layout.Column(
            modifier            = Modifier.padding(16.adp),
            verticalArrangement = Arrangement.spacedBy(12.adp),
        ) {
            ProfileStatRow(
                packCount    = 4,
                cardCount    = 470,
                streakDays   = 7,
                accentColor  = MaterialTheme.colorScheme.secondary,
                successColor = semantic.success,
                goldColor    = semantic.gold,
            )
            LifetimeProgressCard(
                cardsLearned    = previewProfile.totalCardsLearned,
                studyTimeHours  = previewProfile.studyTimeHours,
                avgRetentionPct = previewProfile.avgRetentionPct,
            )
        }
    }
}