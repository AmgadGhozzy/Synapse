package io.synapse.ai.data.repo

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.functions.functions
import io.github.jan.supabase.postgrest.postgrest
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.synapse.ai.data.mapper.DefaultPaywallData
import io.synapse.ai.data.mapper.EntitlementRemoteDto
import io.synapse.ai.data.mapper.PaywallConfigDto
import io.synapse.ai.data.mapper.PaywallFeatureDto
import io.synapse.ai.data.mapper.PaywallProductDto
import io.synapse.ai.data.mapper.toDomain
import io.synapse.ai.domain.model.Entitlement
import io.synapse.ai.domain.model.PaywallConfig
import io.synapse.ai.domain.model.SubscriptionStatus
import io.synapse.ai.domain.repo.IPremiumRepository
import io.synapse.ai.domain.repo.ISocialProofRepository
import io.synapse.ai.features.premium.presentation.state.SocialProofData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.SocketTimeoutException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PremiumRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient,
    private val appConfigProvider: AppConfigProvider,
) : IPremiumRepository, ISocialProofRepository {

    override suspend fun loadPaywallConfig(): Result<PaywallConfig> = withContext(Dispatchers.IO) {
        runCatching {
            supabase.postgrest["paywall_products"]
                .select()
                .decodeList<PaywallProductDto>()
                .let { products ->
                    supabase.postgrest["paywall_features"]
                        .select()
                        .decodeList<PaywallFeatureDto>()
                        .let { features ->
                            PaywallConfigDto(
                                products = products,
                                features = features,
                            ).toDomain()
                        }
                }
        }.recoverCatching {
            DefaultPaywallData.config
        }
    }

    override suspend fun verifyPurchaseWithServer(
        skuId: String,
        purchaseToken: String,
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val body = mapOf(
                "sku_id" to skuId,
                "purchase_token" to purchaseToken,
            )
            supabase.functions.invoke("verify-play-purchase", body)
            Unit
        }.onFailure { error ->
            if (isOfflineError(error)) {
                throw OfflineException("No internet connection. Please try again when online.")
            }
        }
    }

    override suspend fun validateSubscription(): Result<Entitlement> = withContext(Dispatchers.IO) {
        runCatching {
            val response = supabase.functions.invoke("get-subscription-status")
            val dto = response.body<EntitlementRemoteDto>()

            Entitlement(
                isPro = dto.isPro,
                status = parseStatus(dto.status),
                skuId = dto.skuId,
                expiresAt = parseExpiresAt(dto.expiresAt),
            )
        }.onFailure { error ->
            if (isOfflineError(error)) {
                throw OfflineException("No internet connection. Using cached subscription status.")
            }
        }
    }

    override suspend fun fetch(): Result<SocialProofData> = withContext(Dispatchers.IO) {
        runCatching {
            SocialProofData(
                userCountLabel = appConfigProvider.premiumSocialProofLabel,
                avatarInitials = DEFAULT_INITIALS,
            )
        }
    }

    private fun parseExpiresAt(expiresAtStr: String?): Long {
        if (expiresAtStr.isNullOrBlank()) return 0L

        // Try each formatter in order; first successful parse wins.
        for (formatter in DATE_FORMATS) {
            try {
                val date = formatter.get()!!.parse(expiresAtStr)
                if (date != null) return date.time
            } catch (_: ParseException) {
                // try next pattern
            }
        }
        return 0L
    }

    private fun isOfflineError(error: Throwable): Boolean {
        return error is HttpRequestTimeoutException ||
                error is SocketTimeoutException ||
                error.message?.contains("network", ignoreCase = true) == true ||
                error.message?.contains("timeout", ignoreCase = true) == true
    }

    private fun parseStatus(statusStr: String?): SubscriptionStatus {
        return when (statusStr?.lowercase()) {
            "active" -> SubscriptionStatus.ACTIVE
            "expired" -> SubscriptionStatus.EXPIRED
            "canceled", "cancelled" -> SubscriptionStatus.CANCELED
            else -> SubscriptionStatus.NONE
        }
    }

    class OfflineException(message: String) : Exception(message)

    companion object {
        private val DEFAULT_INITIALS = listOf("A", "J", "M", "S", "K")
        private val DATE_FORMATS: List<ThreadLocal<SimpleDateFormat>> = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
            "yyyy-MM-dd'T'HH:mm:ssXXX",
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
        ).map { pattern ->
            ThreadLocal.withInitial {
                SimpleDateFormat(pattern, Locale.US).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }
            }
        }
    }
}