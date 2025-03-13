package com.chandra.nestedrecyclerviewexoplayer.managers.exoplayerManager

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer

class ExoPlayerManager<T>(
    private val context: Context,
    private val poolSize: Int = 3,
    private val maxPoolSize: Int = 5
) {
    private val availablePlayers = ArrayDeque<ExoPlayer>(poolSize)
    private val assignedPlayers = HashMap<T, ExoPlayer>()
    private var isReleased = false

    private var isMuted = false
    private var muteStateListener: ((Boolean) -> Unit)? = null

    private val availablePlayerCount: Int
        get() = availablePlayers.size

    private val totalActivePlayers: Int
        get() = assignedPlayers.size + availablePlayers.size

    private val totalPlayers: Int
        get() = assignedPlayers.size + availablePlayers.size + if (isReleased) 0 else 1

    init {
        require(poolSize > 0) { "Pool size must be greater than 0" }
        require(maxPoolSize >= poolSize) { "Max pool size must be >= initial pool size" }
        repeat(poolSize) {
            availablePlayers.add(createNewPlayer())
        }
    }

    fun setMuteStateListener(listener: (Boolean) -> Unit) {
        muteStateListener = listener
        muteStateListener?.invoke(isMuted)
    }

    fun isMuted(): Boolean = isMuted


    @Synchronized
    fun pool(id: T, mediaItem: MediaItem): ExoPlayer {
        if (isReleased) {
            throw IllegalStateException("Pool is released")
        }
        // IF ALREADY ASSIGNED, RETURN THE PLAYER
        assignedPlayers[id]?.let { return it }

        // ACQUIRE PLAYER FROM THE POOL
        val player = getReusablePlayer() ?: acquireNewPlayer()
        assignedPlayers[id] = player

        player.apply {
            setMediaItem(mediaItem)
            prepare()
        }

        return player
    }

    @Synchronized
    fun releaseResource(id: T) {
        if (isReleased) return

        assignedPlayers.remove(id)?.let { player ->
            player.apply {
                stop()
                clearMediaItems()
                seekTo(0)
                playWhenReady = false
            }
            if (availablePlayers.size < maxPoolSize) {
                availablePlayers.add(player)
            } else {
                player.release()
            }
        }
    }

    @Synchronized
    fun releaseAllPlayers() {
        if (isReleased) return
        isReleased = true
        assignedPlayers.values.forEach { it.release() }
        availablePlayers.forEach { it.release() }
        assignedPlayers.clear()
        availablePlayers.clear()
    }

    // NEW FUNCTION TO PAUSE ALL ACTIVE PLAYERS
    @Synchronized
    fun pauseAllPlayers() {
        if (isReleased) return

        // Pause all assigned players
        assignedPlayers.values.forEach { player ->
            if (player.isPlaying) {
                player.pause()
            }
        }

        // Pause any available players that might be playing
        availablePlayers.forEach { player ->
            if (player.isPlaying) {
                player.pause()
            }
        }
    }


    // NEW FUNCTION TO MUTE ALL ACTIVE PLAYERS
    @Synchronized
    fun muteAllPlayers() {
        if (isReleased) return

        // MUTE ALL ASSIGNED PLAYERS
        assignedPlayers.values.forEach { player ->
            player.volume = 0f  // 0f is muted, 1f is full volume
        }

        // MUTE ANY AVAILABLE PLAYERS
        availablePlayers.forEach { player ->
            player.volume = 0f
        }

        // SET IS MUTED STATUS
        isMuted = true

        // INVOKE THE LISTENER
        muteStateListener?.invoke(isMuted)
    }

    // NEW FUNCTION TO UN MUTE ALL ACTIVE PLAYERS
    @Synchronized
    fun unMuteAllPlayers() {
        if (isReleased) return

        // UN MUTE ALL ASSIGNED PLAYERS
        assignedPlayers.values.forEach { player ->
            player.volume = 1f  // Restore to full volume
        }

        // UN MUTED ANY AVAILABLE PLAYERS
        availablePlayers.forEach { player ->
            player.volume = 1f
        }

        // UN SET IS MUTED STATE
        isMuted = false

        // INVOKE THE LISTENER
        muteStateListener?.invoke(isMuted)
    }

    private fun getReusablePlayer(): ExoPlayer? {
        return availablePlayers.firstOrNull { !it.isPlaying }?.also {
            availablePlayers.remove(it)
        }
    }

    private fun acquireNewPlayer(): ExoPlayer {
        return availablePlayers.removeFirstOrNull() ?: run {
            if (assignedPlayers.size + availablePlayers.size < maxPoolSize) {
                createNewPlayer()
            } else {
                throw IllegalStateException("Player pool exhausted")
            }
        }
    }

    private fun createNewPlayer(): ExoPlayer {
        return ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_ONE
            volume = if (isMuted) 0f else 1f
        }
    }

    // LOG STATS
    fun logStats(): String {
        return """
            Available Players: ${availablePlayerCount},
            Total Active Players: $totalActivePlayers
            Total Players: $totalPlayers
            Available Players: ${availablePlayers.map { it.hashCode() }}
            Assigned Players: ${assignedPlayers.map { it.key }}
            """.trimIndent()
    }
}