package com.chandra.nestedrecyclerviewexoplayer

import android.content.Context
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer

class ExoPlayerManager(private val context: Context, private val poolSize: Int) {
    private val availablePlayers: ArrayDeque<ExoPlayer> = ArrayDeque(poolSize)
    private val inUsePlayers: ArrayDeque<ExoPlayer> = ArrayDeque()
    var activeRunningPlayer: ExoPlayer? = null

    init {
        require(poolSize > 0) { "Pool size must be greater than 0" }
        repeat(poolSize) {
            availablePlayers.add(createNewPlayer())
        }
    }

    private fun createNewPlayer(): ExoPlayer {
        return ExoPlayer.Builder(context)
            .build()
            .apply { repeatMode = Player.REPEAT_MODE_ONE }
    }

    fun acquirePlayer(): ExoPlayer {
        val player = availablePlayers.removeFirstOrNull() ?: inUsePlayers.removeFirst().also { p ->
            stopAndClear(p)
            if (p == activeRunningPlayer) {
                activeRunningPlayer = null
            }
        }
        inUsePlayers.add(player)
        return player
    }

    fun releasePlayer(player: ExoPlayer) {
        stopAndClear(player)
        if (player == activeRunningPlayer) {
            activeRunningPlayer = null
        }
        if (inUsePlayers.remove(player)) {
            availablePlayers.add(player)
        }
    }

    fun setActivePlayer(player: ExoPlayer?) {
        if (player != activeRunningPlayer) {
            activeRunningPlayer?.let { stopAndRelease(it) }
            activeRunningPlayer = player
        }
    }

    fun releaseAllPlayers() {
        inUsePlayers.forEach { stopAndClear(it) }
        availablePlayers.addAll(inUsePlayers)
        inUsePlayers.clear()
        activeRunningPlayer = null
    }

    fun shutdown() {
        (availablePlayers + inUsePlayers).forEach { it.release() }
        availablePlayers.clear()
        inUsePlayers.clear()
        activeRunningPlayer = null
    }

    private fun stopAndClear(player: ExoPlayer) {
        player.stop()
        player.clearMediaItems()
    }

    private fun stopAndRelease(player: ExoPlayer) {
        stopAndClear(player)
        if (inUsePlayers.remove(player)) {
            availablePlayers.add(player)
        }
    }
}