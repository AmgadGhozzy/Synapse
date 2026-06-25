package io.synapse.ai.core.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.SystemClock
import androidx.annotation.RawRes
import io.synapse.ai.R
import io.synapse.ai.core.audio.model.SoundType
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.EnumMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * App-scoped sound effect manager.
 *
 * - Uses [SoundPool] for low-latency playback.
 * - Preloads every sound once during initialization.
 * - Supports multi-stream playback.
 * - Debounces repeated taps on the same sound.
 * - Drops lower-priority sounds if a stronger sound just played.
 */
@Singleton
class SoundManager @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val appContext = context.applicationContext

    private val soundPool = SoundPool.Builder()
        .setMaxStreams(MAX_STREAMS)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private val soundIdsByType = EnumMap<SoundType, Int>(SoundType::class.java)
    private val loadedSoundIds = mutableSetOf<Int>()
    private val lastPlayedAtByType = EnumMap<SoundType, Long>(SoundType::class.java)

    private var lastPriorityPlayedAtMs = 0L
    private var lastPriorityPlayed = Int.MIN_VALUE

    init {
        soundPool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == LOAD_SUCCESS) {
                synchronized(loadedSoundIds) {
                    loadedSoundIds += sampleId
                }
            }
        }
        preload()
    }

    /**
     * Loads all registered sounds into [SoundPool] up front so first playback
     * does not pay the decode/load cost.
     */
    @Synchronized
    fun preload() {
        if (soundIdsByType.isNotEmpty()) return

        SoundCatalog.specs.forEach { (soundType, spec) ->
            val soundId = soundPool.load(appContext, spec.resId, LOAD_PRIORITY)
            soundIdsByType[soundType] = soundId
        }
    }

    /**
     * Plays a short UI sound if it passes both per-sound debounce and the
     * recent global priority gate.
     */
    @Synchronized
    fun play(soundType: SoundType) {
        val spec = SoundCatalog.specs[soundType] ?: return
        val soundId = soundIdsByType[soundType] ?: return

        val now = SystemClock.elapsedRealtime()

        val lastPlayedAtMs = lastPlayedAtByType[soundType]
        val elapsedSinceLastPlayMs = lastPlayedAtMs?.let { now - it }
        if (elapsedSinceLastPlayMs != null && elapsedSinceLastPlayMs < spec.debounceMs) return

        if (shouldSuppressForPriority(nowMs = now, newPriority = spec.streamPriority)) return

        val isLoaded = synchronized(loadedSoundIds) { soundId in loadedSoundIds }
        if (!isLoaded) return

        val streamId = soundPool.play(
            soundId,
            spec.volume,
            spec.volume,
            spec.streamPriority,
            0,
            DEFAULT_PLAYBACK_RATE,
        )

        if (streamId != 0) {
            lastPlayedAtByType[soundType] = now
            lastPriorityPlayedAtMs = now
            lastPriorityPlayed = spec.streamPriority
        }
    }

    @Synchronized
    fun release() {
        soundPool.release()
        soundIdsByType.clear()
        loadedSoundIds.clear()
        lastPlayedAtByType.clear()
        lastPriorityPlayedAtMs = 0L
        lastPriorityPlayed = Int.MIN_VALUE
    }

    private fun shouldSuppressForPriority(nowMs: Long, newPriority: Int): Boolean {
        val isContending = nowMs - lastPriorityPlayedAtMs <= PRIORITY_CONTENTION_WINDOW_MS
        return isContending && newPriority < lastPriorityPlayed
    }

    private data class SoundSpec(
        @param:RawRes val resId: Int,
        val streamPriority: Int,
        val debounceMs: Long,
        val volume: Float = 1f,
    )

    /**
     * Single place to register sound assets and tuning.
     *
     * Adding a new sound is intentionally a one-entry change here plus a raw
     * resource and enum value.
     */
    private object SoundCatalog {
        val specs: Map<SoundType, SoundSpec> = linkedMapOf(
            SoundType.CORRECT_SOFT to SoundSpec(
                resId = R.raw.sfx_correct_soft,
                streamPriority = 20,
                debounceMs = 120L,
                volume = 0.92f,
            ),
            SoundType.CORRECT_REWARD to SoundSpec(
                resId = R.raw.sfx_correct_reward,
                streamPriority = 30,
                debounceMs = 180L,
            ),
            SoundType.WRONG_SOFT to SoundSpec(
                resId = R.raw.sfx_wrong_soft,
                streamPriority = 40,
                debounceMs = 150L,
                volume = 0.95f,
            ),
            SoundType.WRONG_REPEAT to SoundSpec(
                resId = R.raw.sfx_wrong_repeat,
                streamPriority = 50,
                debounceMs = 220L,
            ),
            SoundType.LEECH_WARNING to SoundSpec(
                resId = R.raw.sfx_leech_warning,
                streamPriority = 60,
                debounceMs = 320L,
            ),
            SoundType.CARD_FLIP to SoundSpec(
                resId = R.raw.sfx_card_flip,
                streamPriority = 10,
                debounceMs = 80L,
                volume = 0.85f,
            ),
        )
    }

    companion object {
        private const val MAX_STREAMS = 6
        private const val DEFAULT_PLAYBACK_RATE = 1f
        private const val LOAD_PRIORITY = 1
        private const val LOAD_SUCCESS = 0
        private const val PRIORITY_CONTENTION_WINDOW_MS = 150L
    }
}
