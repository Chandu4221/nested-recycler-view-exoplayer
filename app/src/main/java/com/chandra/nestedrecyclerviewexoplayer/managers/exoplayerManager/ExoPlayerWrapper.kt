package com.chandra.nestedrecyclerviewexoplayer.managers.exoplayerManager

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer

/**
 * A wrapper around ExoPlayer providing convenient state management and access
 * to common player controls and listener registration.
 *
 * @param context The application context.
 * @param initialMuteState The initial mute state for the player.
 */
class ExoPlayerWrapper(
    private val context: Context,
    initialMuteState: Boolean = false
) {
    // INITIALIZE THE PLAYER
    val player: ExoPlayer = ExoPlayer.Builder(context).build().apply {
        repeatMode = Player.REPEAT_MODE_ONE
        playWhenReady = false
    }

    // PLAYER MUTE STATE
    var isMuted: Boolean = initialMuteState

    // PLAYER PLAYBACK STATE (IDLE, BUFFERING, READY, ENDED)
    var playbackState: Int = player.playbackState
        private set

    // Reflects if playback should proceed when ready (user's intent to play)
    var playWhenReady: Boolean = player.playWhenReady
        private set

    // Convenience property: Indicates if media is actively playing
    val isPlaying: Boolean
        get() = playbackState == Player.STATE_READY && playWhenReady

    // Convenience property: Indicates if the player is buffering
    val isBuffering: Boolean
        get() = playbackState == Player.STATE_BUFFERING

    // Convenience property: Indicates if the playback has ended
    val isEnded: Boolean
        get() = playbackState == Player.STATE_ENDED

    // Convenience property: Indicates if the player is idle
    val isIdle: Boolean
        get() = playbackState == Player.STATE_IDLE

    // Currently loaded media item
    var currentMediaItem: MediaItem? = null
        private set

    // Last encountered player error
    var playerError: PlaybackException? = null
        private set

    private val internalListener = object : Player.Listener {
        override fun onPlaybackStateChanged(state: Int) {
            playbackState = state
            // CLEAR THE ERROR STATE IF THE PLAYER IS NOT IDLE
            if (state != Player.STATE_IDLE) {
                playerError = null
            }
        }

        override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
            this@ExoPlayerWrapper.playWhenReady = playWhenReady
        }

        override fun onPlayerError(error: PlaybackException) {
            playerError = error
        }
        // CAN ADD OTHER OVERRIDE METHODS
    }

    init {
        updateVolume()
        addInternalPlayerListener()
    }

    private fun addInternalPlayerListener() {
        player.addListener(internalListener)
    }

    // UPDATE PLAYER VOLUME
    private fun updateVolume() {
        player.volume = if (isMuted) 0f else 1f
    }

    /**
     * Sets the mute state of the player.
     * @param muted True to mute, false to un mute.
     */
    fun setIsMuted(muted: Boolean) {
        // CHECK IF EXISTING STATE SAME AS RECEIVED STATE
        // THEN NO NEED TO UPDATE THE STATE
        if (isMuted != muted) {
            isMuted = muted
            updateVolume()
        }
    }

    /**
     * Sets a single media item for playback, replacing any existing items.
     * Also prepares the player.
     * @param mediaItem The MediaItem to play.
     * @param playWhenReady Whether playback should start immediately when ready. Default is true.
     */
    fun setMediaItem(mediaItem: MediaItem, playWhenReady: Boolean = false) {
        currentMediaItem = mediaItem
        player.setMediaItem(mediaItem)
        player.playWhenReady = playWhenReady // Set desired play state *before* prepare
        player.prepare() // Prepare the player for the new media item
    }

    /**
     * Adds a media item to the end of the playlist.
     * @param mediaItem The MediaItem to add.
     */
    fun addMediaItem(mediaItem: MediaItem) {
        player.addMediaItem(mediaItem)
        // Optionally update currentMediaItem if it was null
        if (currentMediaItem == null) {
            currentMediaItem = player.currentMediaItem
        }
    }

    /**
     * Clears the playlist.
     */
    fun clearMediaItems() {
        player.clearMediaItems()
        currentMediaItem = null // Reset tracked media item
    }

    // --- Playback Control ---

    /**
     * Starts or resumes playback.
     * Equivalent to setting playWhenReady = true.
     */
    fun play() {
        player.playWhenReady = true
    }

    /**
     * Pauses playback.
     * Equivalent to setting playWhenReady = false.
     */
    fun pause() {
        player.playWhenReady = false
    }

    /**
     * Stops playback, clears the playlist, resets position, and resets playWhenReady state.
     */
    fun stopAndReset() {
        player.stop() // Stops playback and resets player state to IDLE
        player.clearMediaItems() // Clear the playlist (optional, but common for a full stop/reset)
        player.seekTo(0) // Reset position to the beginning
        player.playWhenReady = false // Ensure it doesn't auto-play on next prepare
        currentMediaItem = null
        playbackState = player.playbackState // Update state after stop
        playWhenReady = player.playWhenReady // Update state after stop
        playerError = null // Clear any previous error
    }

    /**
     * Prepares the player to begin playback. Call this after setting media items
     * if you haven't already called setMediaItem(..., playWhenReady=...).
     */
    fun prepare() {
        player.prepare()
    }

    /**
     * Seeks to a specific position within the current media item.
     * @param positionMs The position in milliseconds.
     */
    fun seekTo(positionMs: Long) {
        player.seekTo(positionMs)
    }

    // --- Listener Management ---

    /**
     * Adds an external Player.Listener to receive events directly from ExoPlayer.
     * Allows users of this wrapper to react to any player event.
     * @param listener The listener to add.
     */
    fun addListener(listener: Player.Listener) {
        player.addListener(listener)
    }

    /**
     * Removes an external Player.Listener.
     * @param listener The listener to remove.
     */
    fun removeListener(listener: Player.Listener) {
        player.removeListener(listener)
    }

    // --- Resource Management ---

    /**
     * Releases the ExoPlayer instance and its resources.
     * This wrapper instance should not be used after calling release().
     */
    fun release() {
        player.removeListener(internalListener) // Clean up internal listener
        player.release()
        // Reset state variables after release (optional, but good practice)
        currentMediaItem = null
        playbackState = Player.STATE_IDLE
        playWhenReady = false
        playerError = null
    }
}