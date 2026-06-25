package io.synapse.ai.core.audio.model

/**
 * Short UI sound effects used across the app.
 *
 * Keep this enum small and semantic. The concrete audio assets live in the
 * audio framework layer so sounds can be swapped without touching feature code.
 */
enum class SoundType {
    CORRECT_SOFT,
    CORRECT_REWARD,
    WRONG_SOFT,
    WRONG_REPEAT,
    LEECH_WARNING,
    CARD_FLIP,
}
