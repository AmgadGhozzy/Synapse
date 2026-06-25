package io.synapse.ai.features.add_pdf.data.resolver

import android.content.Context
import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.core.net.toUri
import dagger.hilt.android.qualifiers.ApplicationContext
import io.synapse.ai.features.add_pdf.domain.model.ResolvedSource
import io.synapse.ai.features.add_pdf.domain.model.SourceRequest
import io.synapse.ai.features.add_pdf.domain.model.SourceType
import io.synapse.ai.features.add_pdf.domain.resolver.SourceContentResolver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AndroidSourceContentResolver @Inject constructor(
    @param:ApplicationContext private val context: Context
) : SourceContentResolver {

    companion object {
        private const val TAG = "AndroidSourceContentResolver"
        val YOUTUBE_PATTERN = Regex(
            "^https?://(www\\.)?youtube\\.com/watch\\?v=[\\w-]{11}" +
                    "|^https?://youtu\\.be/[\\w-]{11}" +
                    "|^https?://(www\\.)?youtube\\.com/shorts/[\\w-]{11}",
            RegexOption.IGNORE_CASE,
        )
    }

    override suspend fun resolve(request: SourceRequest): Result<ResolvedSource> = withContext(Dispatchers.IO) {
        try {
            when (request.type) {
                SourceRequest.RequestType.FILE -> {
                    val uriString = request.uri ?: return@withContext Result.failure(IllegalArgumentException("No file URI"))
                    val uri = uriString.toUri()
                    val fileName = request.fileName ?: "document.pdf"
                    val sourceType = sourceTypeFromName(fileName)

                    val bytes = readBytes(uri) ?: return@withContext Result.failure(IllegalArgumentException("Cannot read file"))
                    if (bytes.isEmpty()) return@withContext Result.failure(IllegalArgumentException("File is empty"))

                    val ext = fileName.substringAfterLast('.', "").lowercase()
                    if (ext == "pdf" && !isValidPdf(bytes)) return@withContext Result.failure(IllegalArgumentException("Not a valid PDF"))
                    if (ext in setOf("doc", "docx") && !isValidWord(bytes)) return@withContext Result.failure(IllegalArgumentException("Not a valid Word document"))

                    val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
                    Result.success(ResolvedSource(base64, sourceType))
                }
                SourceRequest.RequestType.TEXT -> {
                    val text = request.text?.trim()
                    if (text.isNullOrBlank()) return@withContext Result.failure(IllegalArgumentException("Empty text"))
                    Result.success(ResolvedSource(text, SourceType.TEXT))
                }
                SourceRequest.RequestType.WEB -> {
                    val url = request.url?.trim()
                    if (url.isNullOrBlank()) return@withContext Result.failure(IllegalArgumentException("Empty URL"))
                    if (!url.startsWith("http://", true) && !url.startsWith("https://", true)) {
                        return@withContext Result.failure(IllegalArgumentException("Invalid URL protocol"))
                    }
                    Result.success(ResolvedSource(url, SourceType.URL))
                }
                SourceRequest.RequestType.YOUTUBE -> {
                    val url = request.url?.trim()
                    if (url.isNullOrBlank()) return@withContext Result.failure(IllegalArgumentException("Empty URL"))
                    if (!YOUTUBE_PATTERN.containsMatchIn(url)) {
                        return@withContext Result.failure(IllegalArgumentException("Invalid YouTube URL"))
                    }
                    Result.success(ResolvedSource(url, SourceType.YOUTUBE))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "resolve: failed", e)
            Result.failure(e)
        }
    }

    override suspend fun getPageCount(uri: String): Int? = withContext(Dispatchers.IO) {
        try {
            val parsedUri = uri.toUri()
            val pfd = when (parsedUri.scheme) {
                "content" -> context.contentResolver.openFileDescriptor(parsedUri, "r")
                "file"    -> android.os.ParcelFileDescriptor.open(
                    java.io.File(parsedUri.path ?: return@withContext null),
                    android.os.ParcelFileDescriptor.MODE_READ_ONLY,
                )
                else      -> null
            }
            pfd?.use { android.graphics.pdf.PdfRenderer(it).use { renderer -> renderer.pageCount } }
        } catch (e: Exception) {
            Log.w(TAG, "getPageCount: could not read page count", e)
            null
        }
    }

    private fun sourceTypeFromName(fileName: String): SourceType = when {
        fileName.endsWith(".pdf", ignoreCase = true) -> SourceType.PDF
        fileName.endsWith(".doc", ignoreCase = true) ||
        fileName.endsWith(".docx", ignoreCase = true) -> SourceType.DOC
        else -> SourceType.PDF
    }

    private fun isValidPdf(bytes: ByteArray): Boolean {
        if (bytes.size < 5) return false
        val header = bytes.sliceArray(0..4)
        return header.contentEquals(byteArrayOf(0x25, 0x50, 0x44, 0x46, 0x2D)) ||
                header.contentEquals(byteArrayOf(0x25, 0x50, 0x44, 0x46, 0x20))
    }

    private fun isValidWord(bytes: ByteArray): Boolean {
        if (bytes.size < 8) return false
        val oleHeader = bytes.sliceArray(0..7)
        if (oleHeader.contentEquals(byteArrayOf(0xD0.toByte(), 0xCF.toByte(), 0x11.toByte(), 0xE0.toByte(), 0xA1.toByte(), 0xB1.toByte(), 0x1A.toByte(), 0xE1.toByte()))) return true
        if (bytes.size >= 4) {
            val zipHeader = bytes.sliceArray(0..3)
            if (zipHeader.contentEquals(byteArrayOf(0x50, 0x4B, 0x03, 0x04))) return true
        }
        return false
    }

    private fun readBytes(uri: Uri): ByteArray? = try {
        when (uri.scheme) {
            "content" -> context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            "file"    -> uri.path?.let { java.io.File(it).readBytes() }
            else      -> null
        }
    } catch (e: Exception) {
        Log.e(TAG, "readBytes() failed for \$uri: \${e.message}")
        null
    }
}
