package com.chandra.nestedrecyclerviewexoplayer

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer

class ExoPlayerPool<T>(
    private val context: Context,
    private val poolSize: Int = 3,
    private val maxPoolSize: Int = 5
) {
    private val availablePlayers = ArrayDeque<ExoPlayer>(poolSize)
    private val assignedPlayers = HashMap<T, ExoPlayer>()
    private var isReleased = false

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