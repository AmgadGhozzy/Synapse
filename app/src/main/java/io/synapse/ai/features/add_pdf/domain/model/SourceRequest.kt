package io.synapse.ai.features.add_pdf.domain.model

data class SourceRequest(
    val type: RequestType,
    val uri: String? = null,
    val fileName: String? = null,
    val text: String? = null,
    val url: String? = null
) {
    enum class RequestType { FILE, TEXT, WEB, YOUTUBE }
}

data class ResolvedSource(
    val content: String,
    val sourceType: SourceType
)
