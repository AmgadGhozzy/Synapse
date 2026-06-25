package io.synapse.ai.features.add_pdf.data.saver

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.net.toUri
import dagger.hilt.android.qualifiers.ApplicationContext
import io.synapse.ai.features.add_pdf.domain.saver.DocumentSaver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AndroidDocumentSaver @Inject constructor(
    @param:ApplicationContext private val context: Context
) : DocumentSaver {

    override suspend fun savePdfToDownloads(uriString: String, destName: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val srcUri = uriString.toUri()
            val bytes = readBytes(srcUri) ?: throw IllegalStateException("Cannot read source file")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, destName)
                    put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
                    put(MediaStore.Downloads.IS_PENDING, 1)
                }
                val collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                val itemUri = context.contentResolver.insert(collection, values)
                    ?: throw IllegalStateException("MediaStore insert returned null")

                context.contentResolver.openOutputStream(itemUri)?.use { out ->
                    out.write(bytes)
                } ?: throw IllegalStateException("Cannot open MediaStore output stream")

                values.clear()
                values.put(MediaStore.Downloads.IS_PENDING, 0)
                context.contentResolver.update(itemUri, values, null, null)
            } else {
                @Suppress("DEPRECATION")
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                downloadsDir.mkdirs()
                var destFile = java.io.File(downloadsDir, destName)
                if (destFile.exists()) {
                    val base = destName.substringBeforeLast('.')
                    val ext = destName.substringAfterLast('.', "pdf")
                    destFile = java.io.File(downloadsDir, "\${base}_\${System.currentTimeMillis()}.\$ext")
                }
                destFile.writeBytes(bytes)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun readBytes(uri: android.net.Uri): ByteArray? = try {
        when (uri.scheme) {
            "content" -> context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            "file" -> uri.path?.let { java.io.File(it).readBytes() }
            else -> null
        }
    } catch (e: Exception) {
        null
    }
}
