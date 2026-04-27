package io.synapse.ai.data.repo

import dagger.Lazy
import io.synapse.ai.domain.model.DefaultConfig
import io.synapse.ai.domain.model.RCKeys
import io.synapse.ai.domain.repo.IRemoteConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppConfigProvider @Inject constructor(
    private val remoteConfig: IRemoteConfig,
    private val premiumManager: Lazy<PremiumManager>,
) {
    val isPremiumFlow: StateFlow<Boolean>
        get() = premiumManager.get().isPro

    val isPremium: Boolean
        get() = premiumManager.get().isPro.value

    val libraryFreePackLimitFlow: Flow<Int>
        get() = isPremiumFlow.map { isPremium ->
            if (isPremium) Int.MAX_VALUE else remoteConfig.getLong(
                RCKeys.Library.FREE_PACK_LIMIT,
                DefaultConfig.Library.FREE_PACK_LIMIT.toLong()
            ).toInt()
        }

    val isWebImportLockedFlow: Flow<Boolean>
        get() = isPremiumFlow.map { isPremium ->
            !isPremium && remoteConfig.getBoolean(
                RCKeys.AddPdf.WEB_IMPORT_LOCKED,
                DefaultConfig.AddPdf.WEB_IMPORT_LOCKED,
            )
        }

    val addPdfMaxFileSizeMbFlow: Flow<Int>
        get() = isPremiumFlow.map { isPremium ->
            if (isPremium) {
                remoteConfig.getLong(
                    RCKeys.AddPdf.MAX_FILE_SIZE_MB_PRO,
                    DefaultConfig.AddPdf.MAX_FILE_SIZE_MB_PRO.toLong()
                ).toInt()
            } else {
                remoteConfig.getLong(
                    RCKeys.AddPdf.MAX_FILE_SIZE_MB,
                    DefaultConfig.AddPdf.MAX_FILE_SIZE_MB.toLong()
                ).toInt()
            }
        }
    val proMaxFileSizeMb: Int
        get() = remoteConfig.getLong(
            RCKeys.AddPdf.MAX_FILE_SIZE_MB_PRO,
            DefaultConfig.AddPdf.MAX_FILE_SIZE_MB_PRO.toLong()
        ).toInt()

    val isOcrProLockedFlow: Flow<Boolean>
        get() = isPremiumFlow.map { isPremium ->
            !isPremium && remoteConfig.getBoolean(
                RCKeys.AddPdf.OCR_LOCKED,
                DefaultConfig.AddPdf.OCR_LOCKED,
            )
        }

    val ocrMaxPagesFlow: Flow<Int>
        get() = isPremiumFlow.map { isPremium ->
            if (isPremium) {
                remoteConfig.getLong(
                    RCKeys.AddPdf.MAX_PAGES_PRO,
                    DefaultConfig.AddPdf.MAX_PAGES_PRO.toLong()
                ).toInt()
            } else {
                remoteConfig.getLong(
                    RCKeys.AddPdf.MAX_PAGES,
                    DefaultConfig.AddPdf.MAX_PAGES.toLong()
                ).toInt()
            }
        }

    val isThinkingLockedFlow: Flow<Boolean>
        get() = isPremiumFlow.map { isPremium ->
            !isPremium
        }

    val isAdsEnabledFlow: Flow<Boolean>
        get() = isPremiumFlow.map { isPremium ->
            !isPremium && remoteConfig.getBoolean(
                RCKeys.Ads.ENABLED,
                DefaultConfig.Ads.ENABLED,
            )
        }


    val premiumTrialDays: Long
        get() = remoteConfig.getLong(
            RCKeys.Premium.TRIAL_DAYS,
            DefaultConfig.Premium.TRIAL_DAYS.toLong()
        )

    val premiumSocialProofLabel: String
        get() = remoteConfig.getString(
            RCKeys.Premium.SOCIAL_PROOF_LABEL,
            DefaultConfig.Premium.SOCIAL_PROOF_LABEL,
        )

    val sessionAutosaveIntervalMs: Long
        get() = remoteConfig.getLong(
            RCKeys.Session.AUTOSAVE_INTERVAL_MS,
            DefaultConfig.Session.AUTOSAVE_INTERVAL_MS,
        )

    val providerGeminiModel: String
        get() = remoteConfig.getString(
            RCKeys.Provider.GEMINI_MODEL,
            DefaultConfig.Provider.GEMINI_MODEL
        ).ifEmpty { DefaultConfig.Provider.GEMINI_MODEL }

    val providerGroqModel: String
        get() = remoteConfig.getString(
            RCKeys.Provider.GROQ_MODEL,
            DefaultConfig.Provider.GROQ_MODEL
        ).ifEmpty { DefaultConfig.Provider.GROQ_MODEL }

    val providerGptModel: String
        get() = remoteConfig.getString(
            RCKeys.Provider.GPT_MODEL,
            DefaultConfig.Provider.GPT_MODEL
        ).ifEmpty { DefaultConfig.Provider.GPT_MODEL }

    val isAnalyticsEnabled: Boolean
        get() = remoteConfig.getBoolean(
            RCKeys.Global.ANALYTICS_ENABLED,
            DefaultConfig.Global.ANALYTICS_ENABLED,
        )

    val isCrashlyticsEnabled: Boolean
        get() = remoteConfig.getBoolean(
            RCKeys.Global.CRASHLYTICS_ENABLED,
            DefaultConfig.Global.CRASHLYTICS_ENABLED,
        )

    val syncPeriodicIntervalMs: Long
        get() = remoteConfig.getLong(
            RCKeys.Sync.PERIODIC_INTERVAL_MS,
            DefaultConfig.Sync.PERIODIC_INTERVAL_MS,
        )

    val appEmail: String get() = getString(RCKeys.AppInfo.EMAIL, DefaultConfig.AppInfo.EMAIL)
    val appGithub: String get() = getString(RCKeys.AppInfo.GITHUB, DefaultConfig.AppInfo.GITHUB)
    val appLinkedin: String get() = getString(RCKeys.AppInfo.LINKEDIN, DefaultConfig.AppInfo.LINKEDIN)
    val appPrivacy: String get() = getString(RCKeys.AppInfo.PRIVACY, DefaultConfig.AppInfo.PRIVACY)
    val appTerms: String get() = getString(RCKeys.AppInfo.TERMS, DefaultConfig.AppInfo.TERMS)
    val appPlayStore: String get() = getString(RCKeys.AppInfo.PLAY_STORE, DefaultConfig.AppInfo.PLAY_STORE)
    val appPlayStoreSearch: String get() = getString(RCKeys.AppInfo.PLAY_STORE_SEARCH, DefaultConfig.AppInfo.PLAY_STORE_SEARCH)
    val appHelpUrl: String get() = getString(RCKeys.AppInfo.HELP_URL, DefaultConfig.AppInfo.HELP_URL)
    val appDeleteAccountUrl: String get() = getString(RCKeys.AppInfo.DELETE_ACCOUNT_URL, DefaultConfig.AppInfo.DELETE_ACCOUNT_URL)
    val appRateAppUrl: String get() = getString(RCKeys.AppInfo.RATE_APP_URL, DefaultConfig.AppInfo.RATE_APP_URL)

    private fun getString(key: String, default: String): String =
        remoteConfig.getString(key, default).ifEmpty { default }

    suspend fun fetchAndActivate() {
        remoteConfig.fetchAndActivate()
    }
}