package io.synapse.ai.features.add_pdf.domain.usecase

import kotlinx.coroutines.flow.Flow

interface ValidationConfigProvider {
    val isPremiumFlow: Flow<Boolean>
    val addPdfMaxFileSizeMbFlow: Flow<Int>
    val ocrMaxPagesFlow: Flow<Int>
    val proMaxFileSizeMb: Int
}
