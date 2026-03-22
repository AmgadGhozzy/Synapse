package com.venom.synapse.data.repo

import com.venom.synapse.data.mapper.DefaultPaywallData
import com.venom.synapse.data.mapper.EntitlementRemoteDto
import com.venom.synapse.data.mapper.FeatureDto
import com.venom.synapse.data.mapper.PaywallConfigDto
import com.venom.synapse.data.mapper.PlanDto
import com.venom.synapse.data.mapper.toDomain
import com.venom.synapse.domain.model.Entitlement
import com.venom.synapse.domain.model.PaywallConfig
import com.venom.synapse.domain.repo.PremiumRepository
import com.venom.synapse.domain.repo.SocialProofRepository
import com.venom.synapse.features.premium.presentation.state.SocialProofData
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.functions.functions
import io.github.jan.supabase.postgrest.postgrest
import io.ktor.client.call.body
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SynapsePremiumRepository @Inject constructor(
    private val supabase: SupabaseClient,
) : PremiumRepository, SocialProofRepository {

    private val json = Json { ignoreUnknownKeys = true }

    // ── PremiumRepository ─────────────────────────────────────────────────────

    override suspend fun loadPaywallConfig(): Result<PaywallConfig> = withContext(Dispatchers.IO) {
        runCatching {
            coroutineScope {
                // Fetch all three tables in parallel to minimise latency
                val plansDeferred    = async { supabase.postgrest["premium_plans"].select().decodeList<PlanDto>() }
                val featuresDeferred = async { supabase.postgrest["pro_features"].select().decodeList<FeatureDto>() }
                val configDeferred   = async { supabase.postgrest["paywall_config"].select().decodeSingle<Map<String, Int>>() }

                val trialDays = configDeferred.await()["trial_days"] ?: 7

                PaywallConfigDto(
                    plans     = plansDeferred.await(),
                    features  = featuresDeferred.await(),
                    trialDays = trialDays,
                ).toDomain()
            }
        }.recoverCatching {
            // Network failure, parse error, or empty table → fall back to compiled defaults
            DefaultPaywallData.config
        }
    }

    /**
     * Calls the Edge Function and returns a fresh [Entitlement].
     * Returns [Result.failure] on any network or auth error so the caller
     * ([EntitlementManager]) can decide the fallback strategy.
     * Does NOT write to [EntitlementCache] — that is the manager's job.
     */
    override suspend fun validateSubscription(): Result<Entitlement> = withContext(Dispatchers.IO) {
        runCatching {
            val syncTimeMs = System.currentTimeMillis()
            val response   = supabase.functions.invoke("validate-subscription")
            val dto        = json.decodeFromString<EntitlementRemoteDto>(
                response.body<ByteArray>().toString(Charsets.UTF_8)
            )
            dto.toDomain(syncTimeMs)
        }
    }

    // ── SocialProofRepository ─────────────────────────────────────────────────

    /**
     * Fetches the single-row `app_social_proof` table.
     *
     * Runs on [Dispatchers.IO] and returns [Result.failure] on any network or
     * parse error — the ViewModel calls `.getOrNull()` and passes the result
     * directly into [PremiumUiState.Ready.socialProof] as a nullable field.
     * A failure here never surfaces an error to the user.
     *
     * Required Supabase schema:
     * ```sql
     * CREATE TABLE app_social_proof (
     *     id                SERIAL PRIMARY KEY,
     *     user_count_label  TEXT   NOT NULL DEFAULT '50,000+',
     *     avatar_initials   TEXT[] NOT NULL DEFAULT ARRAY['A','J','M','S','K']
     * );
     * ALTER TABLE app_social_proof ENABLE ROW LEVEL SECURITY;
     * CREATE POLICY "Public read" ON app_social_proof FOR SELECT USING (true);
     * ```
     */
    override suspend fun fetch(): Result<SocialProofData> = withContext(Dispatchers.IO) {
        runCatching {
            val row = supabase.postgrest[TABLE_SOCIAL_PROOF]
                .select()
                .decodeSingle<SocialProofRow>()

            SocialProofData(
                userCountLabel = row.userCountLabel,
                avatarInitials = row.avatarInitials.ifEmpty { DEFAULT_INITIALS },
            )
        }
    }

    // ── DTOs ──────────────────────────────────────────────────────────────────

    @Serializable
    private data class SocialProofRow(
        @SerialName("user_count_label") val userCountLabel: String,
        @SerialName("avatar_initials")  val avatarInitials: List<String> = emptyList(),
    )

    // ── Constants ─────────────────────────────────────────────────────────────

    private companion object {
        const val TABLE_SOCIAL_PROOF = "app_social_proof"
        val DEFAULT_INITIALS = listOf("A", "J", "M", "S", "K")
    }
}