package com.venom.synapse.core.ui.state

import androidx.compose.runtime.Immutable

// DECK UI MODEL
@Immutable
data class PackUiModel(
    val id: Long,
    val title: String,
    val sourceType: String,
    val createdAt: Long,
    val note: String,
    val questionCount: Int = 0,
    val dueCount: Int = 0,
    val category: String? = null,
    val emoji: String? = null,
    val color: String? = null,
    val language: String = "en"
)
