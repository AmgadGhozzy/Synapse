package com.venom.synapse.data.repo

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import com.venom.synapse.domain.model.Entitlement
import com.venom.synapse.domain.model.SubscriptionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
/**
 * local cache for the user's [Entitlement].
 */
class EntitlementCache(
    private val dataStore: DataStore<Preferences>,
) {

    // ── Keys ─────────────────────────────────────────────────────────
    private companion object {
        val KEY_EXPIRY_MS       = longPreferencesKey("entitlement_expiry_ms")
        val KEY_STATUS_ORDINAL  = intPreferencesKey("entitlement_status_ordinal")
        val KEY_LAST_SYNC_MS    = longPreferencesKey("entitlement_last_sync_ms")
        val KEY_CLOCK_OFFSET_MS = longPreferencesKey("entitlement_clock_offset_ms")
    }

    // ── Read ─────────────────────────────────────────────────────────

    /**
     * Emits the cached [Entitlement] Collected by [EntitlementManager] oncold start.
     */
    val entitlementFlow: Flow<Entitlement?> = dataStore.data.map { prefs ->
        val expiry      = prefs[KEY_EXPIRY_MS]       ?: return@map null
        val statusOrd   = prefs[KEY_STATUS_ORDINAL]  ?: return@map null
        val lastSync    = prefs[KEY_LAST_SYNC_MS]    ?: return@map null
        val clockOffset = prefs[KEY_CLOCK_OFFSET_MS] ?: 0L

        val status = SubscriptionStatus.entries.getOrElse(statusOrd) {
            SubscriptionStatus.UNKNOWN
        }

        Entitlement(
            status         = status,
            expiryTimeMs   = expiry,
            lastSyncTimeMs = lastSync,
            clockOffsetMs  = clockOffset,
        )
    }

    // ── Write ─────────────────────────────────────────────────────────
    suspend fun write(entitlement: Entitlement) {
        dataStore.edit { prefs ->
            prefs[KEY_EXPIRY_MS]       = entitlement.expiryTimeMs
            prefs[KEY_STATUS_ORDINAL]  = entitlement.status.ordinal
            prefs[KEY_LAST_SYNC_MS]    = entitlement.lastSyncTimeMs
            prefs[KEY_CLOCK_OFFSET_MS] = entitlement.clockOffsetMs
        }
    }

    /** Clears the cache on sign-out. */
    suspend fun clear() {
        dataStore.edit { it.clear() }
    }
}
