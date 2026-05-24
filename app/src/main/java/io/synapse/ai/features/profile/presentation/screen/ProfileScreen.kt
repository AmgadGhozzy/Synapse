package io.synapse.ai.features.profile.presentation.screen

import android.Manifest
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.synapse.ai.R
import io.synapse.ai.core.theme.AppTheme
import io.synapse.ai.core.theme.SynapseTheme
import io.synapse.ai.core.theme.synapse
import io.synapse.ai.core.theme.tokens.adp
import io.synapse.ai.core.ui.components.GoogleSignInButton
import io.synapse.ai.core.ui.components.LoadingContent
import io.synapse.ai.core.ui.components.SnackbarHost
import io.synapse.ai.core.ui.components.SynapseSwitch
import io.synapse.ai.core.ui.components.rememberSnackbarController
import io.synapse.ai.core.ui.state.UiEffect
import io.synapse.ai.data.sync.SyncConsent
import io.synapse.ai.features.profile.presentation.components.ClearDataConfirmDialog
import io.synapse.ai.features.profile.presentation.components.DeleteAccountConfirmDialog
import io.synapse.ai.features.profile.presentation.components.DestructiveSettingsRow
import io.synapse.ai.features.profile.presentation.components.LifetimeProgressCard
import io.synapse.ai.features.profile.presentation.components.ProfileAvatarSection
import io.synapse.ai.features.profile.presentation.components.ProfileChevron
import io.synapse.ai.features.profile.presentation.components.ProfileSettingsRow
import io.synapse.ai.features.profile.presentation.components.ProfileSettingsSection
import io.synapse.ai.features.profile.presentation.components.ProfileSignOutRow
import io.synapse.ai.features.profile.presentation.components.ReminderTimePickerDialog
import io.synapse.ai.features.profile.presentation.components.StepperRow
import io.synapse.ai.features.profile.presentation.components.TimeDisplayRow
import io.synapse.ai.features.profile.presentation.state.ProfileUiState
import io.synapse.ai.features.profile.presentation.state.StudySettingsUiState
import io.synapse.ai.features.profile.presentation.viewmodel.ProfileViewModel
import io.synapse.ai.features.profile.presentation.viewmodel.StudySettingsViewModel

@Composable
fun ProfileScreen(
    onNavigate: (String) -> Unit,
    onNavigateBack: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel(),
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isActionLoading by viewModel.isActionLoading.collectAsStateWithLifecycle()
    val snackbarController = rememberSnackbarController()
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    LaunchedEffect(Unit) {
        viewModel.uiEffects.collect { effect ->
            when (effect) {
                is UiEffect.Navigate -> onNavigate(effect.route)
                is UiEffect.NavigateBack -> onNavigateBack()
                is UiEffect.OpenExternal -> uriHandler.openUri(effect.url)
                is UiEffect.ShowToast -> snackbarController.success(effect.text.asString(context))
                is UiEffect.ShowError -> snackbarController.error(effect.text.asString(context))
                else -> Unit
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { snackbarController.SnackbarHost() },
        containerColor = Color.Transparent,
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            ProfileContent(
                uiState = uiState,
                isActionLoading = isActionLoading,
                onPrivacy = viewModel::onPrivacyTapped,
                onRateApp = viewModel::onRateAppTapped,
                onContactUs = viewModel::onContactUsTapped,
                onAbout = viewModel::onAboutTapped,
                onSyncToggle = viewModel::onSyncToggleChanged,
                onConsentGranted = viewModel::onConsentGranted,
                onConsentDenied = viewModel::onConsentDenied,
                onClearAllData = viewModel::onClearAllData,
                onDeleteAccount = viewModel::onDeleteAccount,
                onDeleteAccountViaWeb = viewModel::onDeleteAccountViaTapped,
                onSignOut = viewModel::onSignOut,
                onGoogleSignIn = viewModel::onGoogleSignIn,
                // Privacy consent
                onAnalyticsConsentChanged = viewModel::onAnalyticsConsentChanged,
                onCrashConsentChanged = viewModel::onCrashConsentChanged,
                onPushConsentChanged = viewModel::onPushConsentChanged,
                modifier = Modifier.padding(innerPadding),
            )

            if (isActionLoading) {
                LoadingContent(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
private fun ProfileContent(
    uiState: ProfileUiState,
    isActionLoading: Boolean,
    studyViewModel: StudySettingsViewModel = hiltViewModel(),
    onPrivacy: () -> Unit,
    onRateApp: () -> Unit,
    onContactUs: () -> Unit,
    onAbout: () -> Unit,
    onSyncToggle: (Boolean) -> Unit,
    onConsentGranted: () -> Unit,
    onConsentDenied: () -> Unit,
    onClearAllData: () -> Unit = {},
    onDeleteAccount: () -> Unit,
    onDeleteAccountViaWeb: () -> Unit,
    onSignOut: () -> Unit,
    onGoogleSignIn: (Context) -> Unit,
    // Privacy consent toggles
    onAnalyticsConsentChanged: (Boolean) -> Unit = {},
    onCrashConsentChanged: (Boolean) -> Unit = {},
    onPushConsentChanged: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val studySettings by studyViewModel.uiState.collectAsStateWithLifecycle()

    val synapse = MaterialTheme.synapse
    val semantic = synapse.semantic
    val cs = MaterialTheme.colorScheme

    // ── POST_NOTIFICATIONS permission (Android 13+) ───────────────────────────
    val context = LocalContext.current
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            studyViewModel.updateDailyReminder(true)
            onPushConsentChanged(true)
        }
        // Denied: do nothing — toggle stays off, system already explained denial
    }
    // Helper: requests permission if needed on API 33+, otherwise enables directly
    val requestNotificationPermissionThenEnable: () -> Unit = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !NotificationManagerCompat.from(context).areNotificationsEnabled()
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            studyViewModel.updateDailyReminder(true)
            onPushConsentChanged(true)
        }
    }

    // Local UI state — time picker + delete account dialog visibility
    var showTimePicker by rememberSaveable { mutableStateOf(false) }
    var showThemeDialog by rememberSaveable { mutableStateOf(false) }
    var showDeleteAccountDialog by rememberSaveable { mutableStateOf(false) }
    var showClearDataDialog by rememberSaveable { mutableStateOf(false) }
    var showSyncConsentDialog by rememberSaveable { mutableStateOf(false) }

    if (showSyncConsentDialog) {
        AlertDialog(
            onDismissRequest = { showSyncConsentDialog = false },
            title = { Text(stringResource(R.string.profile_cloud_sync_dialog_title)) },
            text = { Text(stringResource(R.string.profile_cloud_sync_dialog_body)) },
            confirmButton = {
                TextButton(onClick = {
                    showSyncConsentDialog = false
                    onConsentGranted()
                }) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showSyncConsentDialog = false
                    onConsentDenied()
                }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    // Show M3 TimePicker dialog when requested
    if (showTimePicker) {
        ReminderTimePickerDialog(
            initialHour = studySettings.reminderHour,
            initialMinute = studySettings.reminderMinute,
            onDismiss = { showTimePicker = false },
            onConfirm = { h, m ->
                studyViewModel.updateReminderTime(h, m)
                showTimePicker = false
            },
        )
    }

    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text(stringResource(R.string.profile_theme)) },
            text = {
                Column(Modifier.selectableGroup()) {
                    AppTheme.entries.forEach { theme ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .height(56.adp)
                                .selectable(
                                    selected = (theme == studySettings.appTheme),
                                    onClick = {
                                        studyViewModel.updateAppTheme(theme)
                                        showThemeDialog = false
                                    },
                                    role = Role.RadioButton
                                )
                                .padding(horizontal = 16.adp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (theme == studySettings.appTheme),
                                onClick = null // null recommended for accessibility with row click
                            )
                            Spacer(Modifier.width(16.adp))
                            Text(
                                text = stringResource(theme.title),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text(stringResource(R.string.cancel)) // Using cancel button as "Close"
                }
            }
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

    if (showClearDataDialog) {
        ClearDataConfirmDialog(
            onDismiss = { showClearDataDialog = false },
            onConfirm = {
                showClearDataDialog = false
                onClearAllData()
            },
        )
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            bottom = MaterialTheme.synapse.spacing.screenContentBottom
        ),
        verticalArrangement = Arrangement.spacedBy(20.adp),
    ) {

        // ── Avatar ────────────────────────────────────────────────────────────
        item(key = "avatar") {
            ProfileAvatarSection(
                uiState = uiState,
                modifier = Modifier.padding(horizontal = 20.adp),
            )
        }

        // ── Google sign-in prompt (anonymous) ────────────────────────────────
        if (uiState.isAnonymous && !uiState.isLoading) {
            item(key = "google_sign_in") {
                val context = LocalContext.current
                GoogleSignInButton(
                    isLoading = isActionLoading,
                    onClick = { onGoogleSignIn(context) },
                    modifier = Modifier.padding(horizontal = 20.adp),
                )
            }
        }

        // ── Lifetime Progress card ────────────────────────────────────────────
        item(key = "lifetime_progress") {
            LifetimeProgressCard(
                cardsLearned = uiState.totalCardsLearned,
                studyTimeHours = uiState.studyTimeHours,
                avgRetentionPct = uiState.avgRetentionPct,
                modifier = Modifier.padding(horizontal = 20.adp),
            )
        }

        // ── Premium banner (free users only) ───────────────────
//        if (!uiState.isPremium && !uiState.isLoading) {
//            item(key = "premium_banner") {
//                PremiumBannerCard(
//                    onUpgrade = onUpgrade,
//                    modifier = Modifier.padding(horizontal = 20.adp),
//                )
//            }
//        }

        // ── 1. Study Settings ─────────────────────────────────────────────────
        item(key = "study_settings") {
            StudySettingsSection(
                studySettings = studySettings,
                studyViewModel = studyViewModel,
                modifier = Modifier.padding(horizontal = 20.adp),
            )
        }

        // ── 3. Notifications ──────────────────────────────────────────────────
        item(key = "notifications") {

        }

        // ── 2. Appearance ─────────────────────────────────────────────────────
        item(key = "appearance") {
            ProfileSettingsSection(
                title = stringResource(R.string.profile_section_preferences),
                modifier = Modifier.padding(horizontal = 20.adp),
            ) {

                ProfileSettingsRow(
                    label = stringResource(R.string.profile_theme),
                    subLabel = stringResource(studySettings.appTheme.title),
                    iconRes = R.drawable.ic_moon,
                    iconTint = cs.secondary,
                    hasDivider = true,
                    onClick = { showThemeDialog = true },
                ) {
                    ProfileChevron()
                }

                NotificationsSection(
                    studySettings = studySettings,
                    onToggle = { enabled ->
                        if (enabled) requestNotificationPermissionThenEnable()
                        else studyViewModel.updateDailyReminder(false)
                    },
                    onTimeRowTap = { showTimePicker = true }
                )

                if (uiState.isAnonymous) {
                    ProfileSettingsRow(
                        label = stringResource(R.string.profile_cloud_sync),
                        subLabel = stringResource(R.string.profile_cloud_sync_locked),
                        iconRes = R.drawable.ic_cloud,
                        iconTint = cs.onSurfaceVariant,
                        hasDivider = false,
                        onClick = { /* Locked for anonymous */ },
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_lock),
                            contentDescription = stringResource(R.string.a11y_locked),
                            tint = cs.onSurfaceVariant
                        )
                    }
                } else {
                    ProfileSettingsRow(
                        label = stringResource(R.string.profile_cloud_sync),
                        subLabel = stringResource(R.string.profile_cloud_sync_sub),
                        iconRes = R.drawable.ic_cloud,
                        iconTint = synapse.semantic.gold,
                        hasDivider = false,
                        onClick = {
                            if (!uiState.isSyncEnabled && uiState.consentState != SyncConsent.ACCEPTED) {
                                showSyncConsentDialog = true
                            } else {
                                onSyncToggle(!uiState.isSyncEnabled)
                            }
                        },
                    ) {
                        SynapseSwitch(
                            checked = uiState.isSyncEnabled,
                            onCheckedChange = { checked ->
                                if (checked && uiState.consentState != SyncConsent.ACCEPTED) {
                                    showSyncConsentDialog = true
                                } else {
                                    onSyncToggle(checked)
                                }
                            },
                        )
                    }
                }
            }
        }

        // ── 5. Privacy ───────────────────────────────────────────────────────
        item(key = "privacy") {
            ProfileSettingsSection(
                title = stringResource(R.string.profile_section_privacy),
                modifier = Modifier.padding(horizontal = 20.adp),
            ) {
                // Privacy Policy — primary violet: "protection / trust"
                ProfileSettingsRow(
                    label = stringResource(R.string.profile_privacy_policy),
                    subLabel = stringResource(R.string.profile_privacy_policy_sub),
                    iconRes = R.drawable.ic_shield,
                    iconTint = cs.primary,
                    hasDivider = true,
                    onClick = onPrivacy,
                ) { ProfileChevron() }

                // Licenses — accent indigo: "informational / library"
                ProfileSettingsRow(
                    label = stringResource(R.string.profile_licenses),
                    subLabel = stringResource(R.string.profile_licenses_sub),
                    iconRes = R.drawable.ic_info,
                    iconTint = semantic.accent,
                    hasDivider = false,
                    onClick = onAbout,
                ) { ProfileChevron() }

                // Analytics
                ProfileSettingsRow(
                    label = stringResource(R.string.profile_analytics),
                    subLabel = stringResource(
                        if (uiState.analyticsEnabled) R.string.profile_enabled
                        else R.string.profile_disabled
                    ),
                    iconRes = R.drawable.ic_chart_bar,
                    iconTint = cs.tertiary,
                    hasDivider = true,
                    onClick = { onAnalyticsConsentChanged(!uiState.analyticsEnabled) },
                ) {
                    SynapseSwitch(
                        checked = uiState.analyticsEnabled,
                        onCheckedChange = onAnalyticsConsentChanged,
                    )
                }

                // Crash Reports
                ProfileSettingsRow(
                    label = stringResource(R.string.profile_crash_reports),
                    subLabel = stringResource(
                        if (uiState.crashEnabled) R.string.profile_enabled
                        else R.string.profile_disabled
                    ),
                    iconRes = R.drawable.ic_alert_circle,
                    iconTint = cs.error,
                    hasDivider = true,
                    onClick = { onCrashConsentChanged(!uiState.crashEnabled) },
                ) {
                    SynapseSwitch(
                        checked = uiState.crashEnabled,
                        onCheckedChange = onCrashConsentChanged,
                    )
                }
            }
        }

        // ── 6. Account ────────────────────────────────────────────────────────
        item(key = "account") {
            AccountSection(
                onRateApp = onRateApp,
                onContactUs = onContactUs,
                modifier = Modifier.padding(horizontal = 20.adp),
            )
        }

        item(key = "danger_zone") {
            DangerZoneSection(
                isAnonymous = uiState.isAnonymous,
                onClearAllData = { showClearDataDialog = true },
                onDeleteAccount = { showDeleteAccountDialog = true },
                onDeleteAccountViaWeb = onDeleteAccountViaWeb,
                modifier = Modifier.padding(horizontal = 20.adp),
            )
        }

        // Sign Out authenticated only
        if (!uiState.isAnonymous && !uiState.isLoading) {
            item(key = "sign_out") {
                ProfileSignOutRow(
                    onClick = onSignOut,
                    modifier = Modifier.padding(horizontal = 20.adp),
                )
            }
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
        title = stringResource(R.string.settings_section_study),
        modifier = modifier,
    ) {
        StepperRow(
            label = stringResource(R.string.settings_daily_goal),
            subLabel = stringResource(R.string.settings_daily_goal_sub),
            iconRes = R.drawable.ic_target,
            iconTint = cs.primary,
            value = studySettings.dailyGoal,
            unit = stringResource(R.string.settings_unit_cards),
            min = StudySettingsViewModel.DAILY_GOAL_MIN,
            max = StudySettingsViewModel.DAILY_GOAL_MAX,
            onIncrement = studyViewModel::incrementDailyGoal,
            onDecrement = studyViewModel::decrementDailyGoal,
            hasDivider = true,
        )
        StepperRow(
            label = stringResource(R.string.settings_new_cards_day),
            subLabel = stringResource(R.string.settings_new_cards_day_sub),
            iconRes = R.drawable.ic_brain,
            iconTint = MaterialTheme.synapse.semantic.success,
            value = studySettings.newCardsPerDay,
            unit = stringResource(R.string.settings_unit_cards),
            min = StudySettingsViewModel.NEW_PER_DAY_MIN,
            max = StudySettingsViewModel.NEW_PER_DAY_MAX,
            onIncrement = studyViewModel::incrementNewPerDay,
            onDecrement = studyViewModel::decrementNewPerDay,
            hasDivider = true,
        )
        StepperRow(
            label = stringResource(R.string.settings_review_limit),
            subLabel = stringResource(R.string.settings_review_limit_sub),
            iconRes = R.drawable.ic_refresh_cw,
            iconTint = MaterialTheme.synapse.semantic.gold,
            value = studySettings.reviewLimit,
            unit = stringResource(R.string.settings_unit_cards),
            min = StudySettingsViewModel.REVIEW_MIN,
            max = StudySettingsViewModel.REVIEW_MAX,
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
    onTimeRowTap: () -> Unit
) {
    val cs = MaterialTheme.synapse.semantic

    // Daily Reminder toggle
    ProfileSettingsRow(
        label = stringResource(R.string.settings_daily_reminder),
        subLabel = stringResource(R.string.settings_daily_reminder_sub),
        iconRes = R.drawable.ic_bell,
        iconTint = cs.success,
        hasDivider = studySettings.dailyReminderEnabled,
        onClick = { onToggle(!studySettings.dailyReminderEnabled) },
    ) {
        SynapseSwitch(
            checked = studySettings.dailyReminderEnabled,
            onCheckedChange = onToggle,
        )
    }

    // Reminder Time row — animated show/hide
    AnimatedVisibility(
        visible = studySettings.dailyReminderEnabled,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
    ) {
        TimeDisplayRow(
            label = stringResource(R.string.settings_reminder_time),
            subLabel = stringResource(R.string.settings_reminder_time_sub),
            iconRes = R.drawable.ic_clock,
            iconTint = cs.primary,
            hour = studySettings.reminderHour,
            minute = studySettings.reminderMinute,
            onTap = onTimeRowTap,
        )
    }
}

@Composable
private fun AccountSection(
    onRateApp: () -> Unit,
    onContactUs: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val semantic = MaterialTheme.synapse.semantic

    ProfileSettingsSection(
        title = stringResource(R.string.profile_section_support),
        modifier = modifier,
    ) {

        // Rate App — star stays gold but uses level token for bg contrast
        ProfileSettingsRow(
            label = stringResource(R.string.profile_rate_app),
            subLabel = stringResource(R.string.profile_rate_app_sub),
            iconRes = R.drawable.ic_star,
            iconTint = semantic.gold,
            hasDivider = true,
            onClick = onRateApp,
        ) { ProfileChevron() }

        // Contact Us — success emerald: "reach out / positive action"
        ProfileSettingsRow(
            label = stringResource(R.string.profile_contact_us),
            subLabel = stringResource(R.string.profile_contact_us_sub),
            iconRes = R.drawable.ic_mail,
            iconTint = semantic.success,
            hasDivider = true,
            onClick = onContactUs,
        ) { ProfileChevron() }
    }
}


@Composable
private fun DangerZoneSection(
    isAnonymous: Boolean,
    onClearAllData: () -> Unit,
    onDeleteAccount: () -> Unit,
    onDeleteAccountViaWeb: () -> Unit,   // ← new
    modifier: Modifier = Modifier,
) {
    ProfileSettingsSection(
        title = stringResource(R.string.settings_section_danger_zone),
        modifier = modifier,
    ) {
        DestructiveSettingsRow(
            label = stringResource(R.string.settings_clear_data),
            subLabel = stringResource(R.string.settings_clear_data_sub),
            iconRes = R.drawable.ic_trash_2,
            hasDivider = !isAnonymous,
            onClick = onClearAllData,
        )

        if (!isAnonymous) {
            // In-app deletion: fast path, confirmation dialog guards it
            DestructiveSettingsRow(
                label = stringResource(R.string.settings_delete_account),
                subLabel = stringResource(R.string.settings_delete_account_sub),
                iconRes = R.drawable.ic_user_x,
                hasDivider = true,
                onClick = onDeleteAccount,
            )
            // Web deletion: Google Play compliance — external access path
            DestructiveSettingsRow(
                label = stringResource(R.string.settings_delete_account_web),
                subLabel = stringResource(R.string.settings_delete_account_web_sub),
                iconRes = R.drawable.ic_external_link,
                hasDivider = false,
                onClick = onDeleteAccountViaWeb
            )
        }
    }
}

@Preview(name = "StudySettingsSection · Light", showBackground = true)
@Preview(
    name = "StudySettingsSection · Dark",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true
)
@Composable
private fun StudySettingsSectionPreview() {
    SynapseTheme {
        ProfileSettingsSection(
            title = "Study Settings",
            modifier = Modifier.padding(16.adp),
        ) {
            Text(
                text = "3 stepper rows (Daily Goal / New Cards / Review Limit)",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(16.adp),
            )
        }
    }
}
