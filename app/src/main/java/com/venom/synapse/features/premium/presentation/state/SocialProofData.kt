package com.venom.synapse.features.premium.presentation.state

import androidx.compose.runtime.Immutable

@Immutable
data class SocialProofData(
    val userCountLabel: String,
    val avatarInitials: List<String> = listOf("A", "J", "M", "S", "K"),
)
