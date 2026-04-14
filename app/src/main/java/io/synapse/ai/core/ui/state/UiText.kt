package io.synapse.ai.core.ui.state

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

sealed class UiText {
    data class Raw(
        val resId: Int,
        val args: List<Any> = emptyList()
    ) : UiText() {
        constructor(@StringRes resId: Int, vararg args: Any) : this(resId, args.toList())
    }
    data class Dynamic(val value: String)     : UiText()

    @Composable
    fun resolve(): String = when (this) {
        is Raw     -> if (args.isEmpty()) stringResource(resId) else stringResource(resId, *args.toTypedArray())
        is Dynamic -> value
    }

    fun asString(context: Context): String = when (this) {
        is Raw     -> if (args.isEmpty()) context.getString(resId) else context.getString(resId, *args.toTypedArray())
        is Dynamic -> value
    }
}
