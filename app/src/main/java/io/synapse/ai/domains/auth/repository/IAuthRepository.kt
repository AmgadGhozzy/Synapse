package io.synapse.ai.domains.auth.repository

import android.content.Context
import io.synapse.ai.domains.auth.model.UserState
import kotlinx.coroutines.flow.StateFlow

/**
 * Single source of truth for authentication state.
 * Handles anonymous sign-in, Google identity linking, and session management.
 */
interface IAuthRepository {

    /** Reactive user state. Collect in ViewModels. */
    val userState: StateFlow<UserState>

    /** Ensures a session exists. Creates anonymous account on first launch. */
    suspend fun ensureSignedIn(): Result<Unit>

    /** Links a Google identity to the current (anonymous) user. UUID stays the same. */
    suspend fun linkGoogle(activityContext: Context): Result<Unit>

    /**
     * Signs out the current user and creates a fresh anonymous session.
     *
     * ANONYMOUS users:
     *   The anonymous Supabase row is intentionally kept server-side.
     *   Local data must be cleared by the caller BEFORE calling this.
     *
     * GOOGLE users:
     *   Remote data is preserved. Only local cache should be cleared.
     *
     * Caller responsibility: clear local DB first, then call signOut().
     */
    suspend fun signOut()

    /**
     * Permanently deletes the current user account and ALL associated data,
     * then creates a fresh anonymous session.
     *
     * Returns [Result.failure] on network error — the existing session is
     * preserved and local data is NOT cleared, so the user can retry.
     *
     * Caller responsibility: clear local DB ONLY after this returns success.
     */
    suspend fun deleteAccount(): Result<Unit>

    /** Current Supabase user ID. Never null after ensureSignedIn(). */
    fun currentUserId(): String

    /** Returns user ID - alias for currentUserId() */
    fun getUserId(): String = currentUserId()

    /** Whether the user has a linked Google identity (not anonymous). */
    fun isAuthenticated(): Boolean

    /** User tier string for Edge Function quota checks. */
    fun userType(): String
}

