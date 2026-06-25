package io.synapse.ai.core.audio

import javax.inject.Inject
import javax.inject.Singleton

enum class AudioSessionType {
    SUMMARY_READ_ALOUD,
    CARD_TTS,
    QUIZ_EXPLANATION
}

interface AudioClient {
    val sessionType: AudioSessionType
    /** Called when another audio client requests focus, meaning this client must pause. */
    fun onAudioFocusLost()
}

/**
 * Central coordinator for all in-app audio playback.
 * Prevents "civil war" between multiple ExoPlayers/MediaPlayers (e.g., Summary Read Aloud vs Card TTS).
 */
@Singleton
class AudioSessionManager @Inject constructor() {

    private var currentClient: AudioClient? = null

    @Synchronized
    fun requestFocus(client: AudioClient): Boolean {
        if (currentClient != client) {
            currentClient?.onAudioFocusLost()
            currentClient = client
        }
        return true
    }

    @Synchronized
    fun abandonFocus(client: AudioClient) {
        if (currentClient == client) {
            currentClient = null
        }
    }
}
