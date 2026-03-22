package com.venom.synapse.features.profile.presentation.components

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.venom.synapse.R
import com.venom.synapse.core.theme.SynapseTheme
import com.venom.ui.components.common.adp
import com.venom.ui.components.common.asp

// ─────────────────────────────────────────────────────────────────────────────
// Section wrapper — keeps existing API
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ProfileSettingsSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(modifier = modifier) {
        Text(
            text       = title.uppercase(),
            style      = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color      = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier   = Modifier.padding(start = 4.adp, bottom = 8.adp),
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape    = RoundedCornerShape(24.adp),
            color    = MaterialTheme.colorScheme.surface,
            border   = BorderStroke(1.adp, MaterialTheme.colorScheme.outlineVariant),
        ) {
            Column { content() }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Standard row with icon + label/sub + trailing slot
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ProfileSettingsRow(
    label: String,
    subLabel: String,
    iconRes: Int,
    iconTint: Color,
    iconBg: Color,
    hasDivider: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailingContent: @Composable () -> Unit,
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .semantics(mergeDescendants = true) {}
                .clickable(onClick = onClick)
                .padding(horizontal = 16.adp, vertical = 14.adp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.adp),
        ) {
            SettingsIconBox(iconRes = iconRes, iconTint = iconTint, iconBg = iconBg)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = label,
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color      = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(1.adp))
                Text(
                    text  = subLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            trailingContent()
        }
        if (hasDivider) SettingsDivider()
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Stepper row — +/− buttons matching React design
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun StepperRow(
    label: String,
    subLabel: String,
    iconRes: Int,
    iconTint: Color,
    iconBg: Color,
    value: Int,
    unit: String,
    min: Int,
    max: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    hasDivider: Boolean,
    modifier: Modifier = Modifier,
) {
    val cs = MaterialTheme.colorScheme
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.adp, vertical = 14.adp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.adp),
        ) {
            SettingsIconBox(iconRes = iconRes, iconTint = iconTint, iconBg = iconBg)

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = label,
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color      = cs.onSurface,
                )
                Spacer(Modifier.height(1.adp))
                Text(
                    text  = subLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = cs.onSurfaceVariant,
                )
            }

            // ── Stepper control ───────────────────────────────────────────────
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.adp),
            ) {
                StepperButton(
                    symbol  = "−",
                    enabled = value > min,
                    onClick = onDecrement,
                )

                // Animated value
                AnimatedContent(
                    targetState = value,
                    transitionSpec = {
                        val dir = if (targetState > initialState) 1 else -1
                        slideInVertically { dir * it } togetherWith slideOutVertically { -dir * it }
                    },
                    label = "stepper_value",
                ) { count ->
                    Row(
                        modifier               = Modifier.widthIn(min = 44.adp),
                        verticalAlignment      = Alignment.CenterVertically,
                        horizontalArrangement  = Arrangement.Center,
                    ) {
                        Text(
                            text       = "$count",
                            fontSize   = 14.asp,
                            fontWeight = FontWeight.Bold,
                            color      = cs.onSurface,
                            textAlign  = TextAlign.Center,
                        )
                        Spacer(Modifier.width(2.adp))
                        Text(
                            text      = unit,
                            fontSize  = 10.asp,
                            color     = cs.onSurfaceVariant,
                        )
                    }
                }

                StepperButton(
                    symbol  = "+",
                    enabled = value < max,
                    onClick = onIncrement,
                )
            }
        }
        if (hasDivider) SettingsDivider()
    }
}

@Composable
private fun StepperButton(
    symbol: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .size(28.adp)
            .clip(RoundedCornerShape(8.adp))
            .background(cs.primaryContainer.copy(alpha = 0.4f))
            .alpha(if (enabled) 1f else 0.38f)
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text       = symbol,
            fontSize   = 16.asp,
            color      = cs.primary,
            fontWeight = FontWeight.Bold,
            lineHeight = 16.asp,
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Time-display row — shows formatted time + opens picker on tap
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun TimeDisplayRow(
    label: String,
    subLabel: String,
    iconRes: Int,
    iconTint: Color,
    iconBg: Color,
    hour: Int,
    minute: Int,
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val cs = MaterialTheme.colorScheme
    val timeLabel = remember(hour, minute) {
        val h = if (hour % 12 == 0) 12 else hour % 12
        val m = minute.toString().padStart(2, '0')
        val amPm = if (hour < 12) "AM" else "PM"
        "$h:$m $amPm"
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {}
            .clickable(onClick = onTap)
            .padding(horizontal = 16.adp, vertical = 14.adp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.adp),
    ) {
        SettingsIconBox(iconRes = iconRes, iconTint = iconTint, iconBg = iconBg)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text       = label,
                style      = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color      = cs.onSurface,
            )
            Spacer(Modifier.height(1.adp))
            Text(
                text  = subLabel,
                style = MaterialTheme.typography.labelSmall,
                color = cs.onSurfaceVariant,
            )
        }
        // Time pill
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.adp))
                .background(cs.primaryContainer.copy(alpha = 0.35f))
                .border(1.adp, cs.primary.copy(alpha = 0.25f), RoundedCornerShape(10.adp))
                .padding(horizontal = 10.adp, vertical = 5.adp),
        ) {
            Text(
                text       = timeLabel,
                fontSize   = 13.asp,
                fontWeight = FontWeight.SemiBold,
                color      = cs.primary,
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Material 3 Time Picker Dialog
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderTimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onDismiss: () -> Unit,
    onConfirm: (hour: Int, minute: Int) -> Unit,
) {
    val state = rememberTimePickerState(
        initialHour   = initialHour,
        initialMinute = initialMinute,
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text  = stringResource(R.string.settings_reminder_time_picker_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        },
        text = { TimePicker(state = state) },
        confirmButton = {
            TextButton(onClick = { onConfirm(state.hour, state.minute) }) {
                Text(stringResource(R.string.settings_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.settings_cancel))
            }
        },
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Delete Account Confirmation Dialog
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Two-step confirmation dialog for permanent account deletion.
 *
 * Design:
 *   • Title + warning body using [MaterialTheme.colorScheme.error] tones.
 *   • "Delete" button is a filled [Button] with [error] container — visually
 *     distinct from the safe [TextButton] cancel path.
 *   • [userEmail] is shown in the warning copy so the user knows exactly
 *     which account will be deleted. Pass null for anonymous users.
 *
 * Caller is responsible for controlling visibility via a `rememberSaveable`
 * boolean and only calling [onConfirm] once (it triggers the ViewModel).
 */
@Composable
fun DeleteAccountConfirmDialog(
    userEmail: String?,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    val error   = MaterialTheme.colorScheme.error
    val onError = MaterialTheme.colorScheme.onError

    AlertDialog(
        onDismissRequest = onDismiss,
        // ── Icon ─────────────────────────────────────────────────────────────
        icon = {
            Box(
                modifier         = Modifier
                    .size(48.adp)
                    .clip(RoundedCornerShape(14.adp))
                    .background(error.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter            = painterResource(R.drawable.ic_user_x),
                    contentDescription = null,
                    tint               = error,
                    modifier           = Modifier.size(22.adp),
                )
            }
        },
        // ── Title ─────────────────────────────────────────────────────────────
        title = {
            Text(
                text       = stringResource(R.string.settings_delete_account_dialog_title),
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color      = error,
            )
        },
        // ── Body ──────────────────────────────────────────────────────────────
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.adp)) {
                Text(
                    text  = stringResource(R.string.settings_delete_account_dialog_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (!userEmail.isNullOrBlank()) {
                    // Highlight the email so the user knows exactly what's affected
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.adp))
                            .background(error.copy(alpha = 0.08f))
                            .padding(horizontal = 12.adp, vertical = 8.adp),
                    ) {
                        Text(
                            text       = userEmail,
                            style      = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color      = error,
                        )
                    }
                }
                Text(
                    text  = stringResource(R.string.settings_delete_account_dialog_irreversible),
                    style = MaterialTheme.typography.labelSmall,
                    color = error.copy(alpha = 0.7f),
                )
            }
        },
        // ── Actions ───────────────────────────────────────────────────────────
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors  = ButtonDefaults.buttonColors(
                    containerColor = error,
                    contentColor   = onError,
                ),
            ) {
                Icon(
                    painter            = painterResource(R.drawable.ic_trash_2),
                    contentDescription = null,
                    modifier           = Modifier.size(15.adp),
                )
                Spacer(Modifier.width(6.adp))
                Text(
                    text       = stringResource(R.string.settings_delete_account_dialog_confirm),
                    fontWeight = FontWeight.SemiBold,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.settings_cancel))
            }
        },
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Destructive chevron row — for Delete Account / Clear All Data
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun DestructiveSettingsRow(
    label: String,
    subLabel: String,
    iconRes: Int,
    hasDivider: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val error = MaterialTheme.colorScheme.error
    val errorBg = error.copy(alpha = 0.10f)

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .semantics(mergeDescendants = true) {}
                .clickable(onClick = onClick)
                .padding(horizontal = 16.adp, vertical = 14.adp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.adp),
        ) {
            SettingsIconBox(iconRes = iconRes, iconTint = error, iconBg = errorBg)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = label,
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color      = error,
                )
                Spacer(Modifier.height(1.adp))
                Text(
                    text  = subLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = error.copy(alpha = 0.6f),
                )
            }
            Icon(
                imageVector        = Icons.AutoMirrored.Rounded.ArrowForwardIos,
                contentDescription = null,
                tint               = error.copy(alpha = 0.6f),
                modifier           = Modifier.size(14.adp),
            )
        }
        if (hasDivider) SettingsDivider()
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Shared private helpers
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SettingsIconBox(iconRes: Int, iconTint: Color, iconBg: Color) {
    Box(
        modifier         = Modifier
            .size(36.adp)
            .clip(RoundedCornerShape(10.adp))
            .background(iconBg),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter            = painterResource(iconRes),
            contentDescription = null,
            tint               = iconTint,
            modifier           = Modifier.size(17.adp),
        )
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        modifier  = Modifier.padding(start = 64.adp, end = 16.adp),
        thickness = 1.adp,
        color     = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Existing components — kept for backward compatibility
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun SynapseSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Switch(
        checked         = checked,
        onCheckedChange = onCheckedChange,
        modifier        = modifier,
        thumbContent    = {
            Icon(
                painter            = painterResource(if (checked) R.drawable.ic_moon else R.drawable.ic_sun),
                contentDescription = null,
                modifier           = Modifier.size(12.adp),
            )
        },
        colors = SwitchDefaults.colors(
            checkedTrackColor   = MaterialTheme.colorScheme.secondary,
            checkedThumbColor   = Color.White,
            checkedIconColor    = MaterialTheme.colorScheme.secondary,
            uncheckedTrackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
            uncheckedThumbColor = Color.White,
            uncheckedIconColor  = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
    )
}

@Composable
fun ProfileChevron(modifier: Modifier = Modifier) {
    Icon(
        imageVector        = Icons.AutoMirrored.Rounded.ArrowForwardIos,
        contentDescription = null,
        tint               = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier           = modifier.size(14.adp),
    )
}

@Composable
fun ProfileSignOutRow(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val error = MaterialTheme.colorScheme.error
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.adp))
            .background(error.copy(alpha = 0.10f))
            .border(1.adp, error.copy(alpha = 0.2f), RoundedCornerShape(16.adp))
            .semantics { role = Role.Button }
            .clickable(onClick = onClick)
            .padding(vertical = 16.adp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        Icon(
            painter            = painterResource(R.drawable.ic_log_out),
            contentDescription = null,
            tint               = error,
            modifier           = Modifier.size(16.adp),
        )
        Spacer(Modifier.width(8.adp))
        Text(
            text       = stringResource(R.string.profile_sign_out),
            fontSize   = 14.asp,
            fontWeight = FontWeight.SemiBold,
            color      = error,
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Previews
// ─────────────────────────────────────────────────────────────────────────────

@Preview(name = "StepperRow · Light", showBackground = true)
@Preview(name = "StepperRow · Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun StepperRowPreview() {
    SynapseTheme {
        Surface {
            StepperRow(
                label      = "Daily Goal",
                subLabel   = "Cards to review each day",
                iconRes    = R.drawable.ic_target,
                iconTint   = MaterialTheme.colorScheme.primary,
                iconBg     = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                value      = 20,
                unit       = "cards",
                min        = 5,
                max        = 200,
                onIncrement = {},
                onDecrement = {},
                hasDivider = false,
            )
        }
    }
}

@Preview(name = "DestructiveRow · Light", showBackground = true)
@Preview(name = "DestructiveRow · Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun DestructiveRowPreview() {
    SynapseTheme {
        Surface {
            DestructiveSettingsRow(
                label      = "Clear All Data",
                subLabel   = "Reset progress and all decks",
                iconRes    = R.drawable.ic_trash_2,
                hasDivider = false,
                onClick    = {},
            )
        }
    }
}

@Preview(name = "DeleteAccountDialog · Light", showBackground = true)
@Preview(name = "DeleteAccountDialog · Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun DeleteAccountDialogPreview() {
    SynapseTheme {
        DeleteAccountConfirmDialog(
            userEmail = "alex.johnson@email.com",
            onDismiss = {},
            onConfirm = {},
        )
    }
}