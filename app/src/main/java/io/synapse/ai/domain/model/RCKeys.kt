package io.synapse.ai.domain.model

/**
 * Constant keys used for Firebase Remote Config.
 */
object RCKeys {

    object Library {
        const val FREE_PACK_LIMIT = "monetization_free_pack_limit"
    }

    object AddPdf {
        const val WEB_IMPORT_LOCKED = "monetization_web_import_locked"
        const val MAX_FILE_SIZE_MB = "add_pdf_max_file_size_mb"
        const val MAX_PAGES = "add_pdf_max_pages"
        const val MAX_FILE_SIZE_MB_PRO = "add_pdf_max_file_size_mb_pro"
        const val MAX_PAGES_PRO = "add_pdf_max_pages_pro"
        const val OCR_LOCKED = "monetization_pro_ocr_locked"
        const val COMPRESSION_QUALITY = "ocr_compression_quality"
    }

    object Global {
        const val ANALYTICS_ENABLED = "app_analytics_enabled"
        const val CRASHLYTICS_ENABLED = "app_crashlytics_enabled"
    }

    object Premium {
        const val TRIAL_DAYS = "monetization_trial_days"
        const val SOCIAL_PROOF_LABEL = "monetization_social_proof_label"
    }

    object Session {
        const val AUTOSAVE_INTERVAL_MS = "session_autosave_interval_ms"
    }

    object Provider {
        const val GEMINI_MODEL = "provider_gemini_model"
        const val GROQ_MODEL = "provider_groq_model"
        const val GPT_MODEL = "provider_gpt_model"
    }

    object Sync {
        const val PERIODIC_INTERVAL_MS = "sync_periodic_interval_ms"
    }

    object Ads {
        const val ENABLED = "feature_ads_enabled"
    }

    object Thinking {
        const val PRO_LOCKED = "thinking_pro_locked"
    }

    object Reviewer {
        const val REVIEWER_MODE = "reviewer_mode_enabled"
    }

    object AppInfo {
        const val EMAIL = "app_email"
        const val GITHUB = "app_github"
        const val LINKEDIN = "app_linkedin"
        const val LICENSE = "app_license"
        const val PRIVACY = "app_privacy"
        const val TERMS = "app_terms"
        const val PLAY_STORE = "app_play_store"
        const val PLAY_STORE_SEARCH = "app_play_store_search"
        const val HELP_URL = "app_help_url"
        const val DELETE_ACCOUNT_URL = "app_delete_account_url"
        const val RATE_APP_URL = "app_rate_app_url"
    }
}
