package com.venom.synapse.domain.repo

import com.venom.synapse.domain.model.PaywallConfig

interface PremiumRepository {
    /**
     * Fetches pricing plans and feature list.
     */
    suspend fun loadPaywallConfig(): Result<PaywallConfig>
}
