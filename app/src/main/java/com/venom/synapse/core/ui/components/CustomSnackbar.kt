package com.venom.synapse.core.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.venom.synapse.core.theme.synapse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Type of snackbar message
 */
enum class SnackbarType {
    SUCCESS,
    ERROR,
    INFO,
    DEFAULT
}

/**
 * Modern Snackbar Controller with simplified API
 */
@Stable
class SnackbarController(
    val hostState: SnackbarHostState,
    private val scope: CoroutineScope,
    private val context: android.content.Context
) {
    private var currentType: SnackbarType = SnackbarType.DEFAULT

    /**
     * Show success message
     */
    fun success(message: String) {
        currentType = SnackbarType.SUCCESS
        show(message, duration = SnackbarDuration.Short)
    }

    fun success(@StringRes messageRes: Int) {
        success(context.getString(messageRes))
    }

    /**
     * Show error message
     */
    fun error(message: String) {
        currentType = SnackbarType.ERROR
        show(message, duration = SnackbarDuration.Long)
    }

    fun error(@StringRes messageRes: Int) {
        error(context.getString(messageRes))
    }

    fun error(
        message: String,
        @StringRes actionLabelRes: Int,
        action: () -> Unit
    ) {
        currentType = SnackbarType.ERROR
        show(
            message = message,
            actionLabel = context.getString(actionLabelRes),
            duration = SnackbarDuration.Long,
            action = action
        )
    }

    /**
     * Show info message
     */
    fun info(message: String) {
        currentType = SnackbarType.INFO
        show(message, duration = SnackbarDuration.Short)
    }

    fun info(@StringRes messageRes: Int) {
        info(context.getString(messageRes))
    }

    /**
     * Show custom message
     */
    fun show(
        message: String,
        actionLabel: String? = null,
        duration: SnackbarDuration = SnackbarDuration.Short,
        action: (() -> Unit)? = null
    ) {
        scope.launch {
            hostState.showSnackbar(
                message = message,
                actionLabel = actionLabel,
                duration = duration,
                withDismissAction = actionLabel == null
            ).let { result ->
                if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                    action?.invoke()
                }
            }
        }
    }

    /**
     * Dismiss current snackbar
     */
    fun dismiss() {
        hostState.currentSnackbarData?.dismiss()
    }

    internal fun getType(): SnackbarType = currentType
}

/**
 * Remember a SnackbarController
 */
@Composable
fun rememberSnackbarController(): SnackbarController {
    val hostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    return remember(hostState, scope, context) {
        SnackbarController(hostState, scope, context)
    }
}

/**
 * Modern Snackbar Host positioned above navigation bar
 * Place this at the root of your Scaffold
 */
@Composable
fun SnackbarHost(
    controller: SnackbarController,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        SnackbarHost(
            hostState = controller.hostState,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 80.dp, start = 16.dp, end = 16.dp)
        ) { snackbarData ->
            ModernSnackbar(
                snackbarData = snackbarData,
                type = controller.getType()
            )
        }
    }
}

/**
 * Modern styled Snackbar with icons and semantic colors
 */
@Composable
private fun ModernSnackbar(
    snackbarData: SnackbarData,
    type: SnackbarType,
    modifier: Modifier = Modifier
) {
    val semantic = MaterialTheme.synapse.semantic

    val (containerColor, contentColor, icon) = when (type) {
        SnackbarType.SUCCESS -> Triple(
            semantic.successContainer,
            semantic.success,
            Icons.Filled.CheckCircle
        )
        SnackbarType.ERROR -> Triple(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
            Icons.Filled.Error
        )
        SnackbarType.INFO -> Triple(
            semantic.accentContainer,
            semantic.accent,
            Icons.Filled.Info
        )
        SnackbarType.DEFAULT -> Triple(
            MaterialTheme.colorScheme.inverseSurface,
            MaterialTheme.colorScheme.inverseOnSurface,
            null
        )
    }

    Snackbar(
        modifier = modifier,
        action = {
            snackbarData.visuals.actionLabel?.let { actionLabel ->
                TextButton(
                    onClick = { snackbarData.performAction() }
                ) {
                    Text(
                        text = actionLabel,
                        color = contentColor,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        },
        dismissAction = {
            if (snackbarData.visuals.withDismissAction) {
                TextButton(onClick = { snackbarData.dismiss() }) {
                    Text(
                        text = "✕",
                        color = contentColor.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        },
        containerColor = containerColor,
        contentColor = contentColor,
        shape = RoundedCornerShape(12.dp),
        actionContentColor = contentColor
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = contentColor
                )
            }
            Text(
                text = snackbarData.visuals.message,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f, fill = false)
            )
        }
    }
}

@Composable
fun SnackbarController.SnackbarHost() {
    SnackbarHost(controller = this)
}
