package io.synapse.ai.domain.model

/**
 * Local fallback values for Remote Config.
 * Used when the remote fetch fails or for initial app startup.
 */
object DefaultConfig {

    object Library {
        const val FREE_PACK_LIMIT = 5
    }

    object AddPdf {
        const val WEB_IMPORT_LOCKED = true
        const val MAX_FILE_SIZE_MB = 5
        const val MAX_PAGES = 20
        const val MAX_FILE_SIZE_MB_PRO = 50
        const val MAX_PAGES_PRO = 100

        const val OCR_LOCKED = true
        const val COMPRESSION_QUALITY = 80
    }

    object Premium {
        const val SOCIAL_PROOF_LABEL = "5,000+"
    }

    object Session {
        const val AUTOSAVE_INTERVAL_MS = 30_000L
    }

    object Sync {
        const val PERIODIC_INTERVAL_MS = 60000L
    }

    object Ads {
        const val ENABLED = true
    }

    object Reviewer {
        const val REVIEWER_MODE = false
    }

    object Export {
        const val FREE_MONTHLY_LIMIT = 10
    }

    object AppInfo {
        const val EMAIL = "trysynapseapp@gmail.com"
        const val GITHUB = "https://github.com/AmgadGhozzy"
        const val LINKEDIN = "https://linkedin.com/AmgadGhozzy"

        const val PRIVACY = "https://synapse-app.netlify.app/privacy.html"
        const val TERMS = "https://synapse-app.netlify.app/terms.html"
        const val PLAY_STORE = "https://play.google.com/store/apps/details?id=io.synapse.ai"
        const val PLAY_STORE_SEARCH = "https://play.google.com/store/search?q=SynapseAi&c=apps"
        const val HELP_URL = "trysynapseapp@gmail.com"
        const val DELETE_ACCOUNT_URL = "https://synapse-app.netlify.app/delete-account.html"
        const val RATE_APP_URL = "market://details?id=io.synapse.ai"
    }

    /**
     * Converts the default configuration into a Map for Firebase Remote Config compatibility.
     */
    fun toMap(): Map<String, Any> = mapOf(
        RCKeys.Library.FREE_PACK_LIMIT to Library.FREE_PACK_LIMIT,
        RCKeys.AddPdf.WEB_IMPORT_LOCKED to AddPdf.WEB_IMPORT_LOCKED,
        RCKeys.AddPdf.MAX_FILE_SIZE_MB to AddPdf.MAX_FILE_SIZE_MB,
        RCKeys.AddPdf.MAX_FILE_SIZE_MB_PRO to AddPdf.MAX_FILE_SIZE_MB_PRO,
        RCKeys.AddPdf.OCR_LOCKED to AddPdf.OCR_LOCKED,
        RCKeys.AddPdf.MAX_PAGES to AddPdf.MAX_PAGES,
        RCKeys.AddPdf.COMPRESSION_QUALITY to AddPdf.COMPRESSION_QUALITY,
        RCKeys.Premium.SOCIAL_PROOF_LABEL to Premium.SOCIAL_PROOF_LABEL,
        RCKeys.Session.AUTOSAVE_INTERVAL_MS to Session.AUTOSAVE_INTERVAL_MS,
        RCKeys.Sync.PERIODIC_INTERVAL_MS to Sync.PERIODIC_INTERVAL_MS,
        RCKeys.Ads.ENABLED to Ads.ENABLED,
        RCKeys.Reviewer.REVIEWER_MODE to Reviewer.REVIEWER_MODE,
        RCKeys.Export.FREE_MONTHLY_LIMIT to Export.FREE_MONTHLY_LIMIT,
        RCKeys.AppInfo.EMAIL to AppInfo.EMAIL,
        RCKeys.AppInfo.GITHUB to AppInfo.GITHUB,
        RCKeys.AppInfo.LINKEDIN to AppInfo.LINKEDIN,
        RCKeys.AppInfo.PRIVACY to AppInfo.PRIVACY,
        RCKeys.AppInfo.TERMS to AppInfo.TERMS,
        RCKeys.AppInfo.PLAY_STORE to AppInfo.PLAY_STORE,
        RCKeys.AppInfo.PLAY_STORE_SEARCH to AppInfo.PLAY_STORE_SEARCH,
        RCKeys.AppInfo.HELP_URL to AppInfo.HELP_URL,
        RCKeys.AppInfo.DELETE_ACCOUNT_URL to AppInfo.DELETE_ACCOUNT_URL,
        RCKeys.AppInfo.RATE_APP_URL to AppInfo.RATE_APP_URL,
    )
}
