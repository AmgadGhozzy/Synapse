package com.venom.synapse.domain.model

/** Immutable snapshot of the current user's authentication state. */
data class UserState(
    val userId: String? = null,
    val isAnonymous: Boolean = true,
    val displayName: String? = null,
    val email: String? = null,
    val avatarUrl: String? = null,
    val isPremium: Boolean = false,
)
