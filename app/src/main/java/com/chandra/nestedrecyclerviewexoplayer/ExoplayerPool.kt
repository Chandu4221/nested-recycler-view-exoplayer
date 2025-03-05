package com.chandra.nestedrecyclerviewexoplayer

import android.content.Context
import android.util.Log
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer

class ExoPlayerPool(private val context: Context, private val poolSize: Int) {
    private val TAG = this::class.java.simpleName

    private val availablePlayers: MutableList<ExoPlayer> = mutableListOf()
    private val inUsePlayers: MutableList<ExoPlayer> = mutableListOf()
    var activeRunningPlayer: ExoPlayer? = null

    init {
        Log.d(TAG, "Initializing pool with size: $poolSize")
        require(poolSize > 0) { "Pool size must be greater than 0" }
        for (i in 0 until poolSize) {
            val player = createNewPlayer()
            availablePlayers.add(player)
            Log.v(
                TAG,
                "Created initial player $i - Available: ${availablePlayers.size}, InUse: ${inUsePlayers.size}"
            )
        }
        Log.i(TAG, "Pool initialization complete - Total players created: $poolSize")
    }

    private fun createNewPlayer(): ExoPlayer {
        val player = ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_ONE
        }
        Log.d(TAG, "Created new player instance: ${player.hashCode()}")
        return player
    }

    fun acquirePlayer(): ExoPlayer {
        val player: ExoPlayer = if (availablePlayers.isNotEmpty()) {
            // Take from available pool
            availablePlayers.removeAt(0)
        } else if (inUsePlayers.isNotEmpty()) {
            // Round-robin: Take the first in-use player, stop it, and reuse
            val reusedPlayer = inUsePlayers.removeAt(0)
            reusedPlayer.stop()
            reusedPlayer.clearMediaItems()
            if (reusedPlayer == activeRunningPlayer) {
                activeRunningPlayer = null
                Log.i(TAG, "Reused active player ${reusedPlayer.hashCode()} - stopped and cleared")
            } else {
                Log.i(TAG, "Reused in-use player ${reusedPlayer.hashCode()} - stopped and cleared")
            }
            reusedPlayer
        } else {
            throw IllegalStateException("Pool is empty - this should never happen with proper initialization")
        }

        inUsePlayers.add(player)
        Log.i(
            TAG, "Acquired player ${player.hashCode()} - " +
                    "Available: ${availablePlayers.size}, InUse: ${inUsePlayers.size}"
        )
        return player
    }

    fun releasePlayer(player: ExoPlayer) {
        Log.d(TAG, "Releasing player ${player.hashCode()}")
        player.stop()
        player.clearMediaItems()
        if (player == activeRunningPlayer) {
            activeRunningPlayer = null
            Log.i(TAG, "Stopped and cleared active player ${player.hashCode()}")
        }
        if (inUsePlayers.remove(player)) {
            availablePlayers.add(player)
            Log.i(
                TAG, "Player ${player.hashCode()} released successfully - " +
                        "Available: ${availablePlayers.size}, InUse: ${inUsePlayers.size}"
            )
        } else {
            Log.w(TAG, "Attempted to release player ${player.hashCode()} that wasn't in use")
        }
    }

    fun setActivePlayer(player: ExoPlayer?) {
        if (player != activeRunningPlayer) {
            // Stop and release the previous active player
            activeRunningPlayer?.let {
                it.stop()
                it.clearMediaItems()
                if (inUsePlayers.contains(it)) {
                    inUsePlayers.remove(it)
                    availablePlayers.add(it)
                    Log.i(
                        TAG, "Previous active player ${it.hashCode()} stopped and returned to pool"
                    )
                }
            }
            activeRunningPlayer = player
            if (player != null) {
                Log.i(TAG, "Set new active player ${player.hashCode()}")
            } else {
                Log.i(TAG, "Cleared active player")
            }
        }
    }

    fun releaseAllPlayers() {
        Log.d(TAG, "Releasing all in-use players")
        val iterator = inUsePlayers.iterator()
        while (iterator.hasNext()) {
            val player = iterator.next()
            player.stop()
            player.clearMediaItems()
            if (player == activeRunningPlayer) {
                activeRunningPlayer = null
            }
            availablePlayers.add(player)
            iterator.remove()
            Log.v(TAG, "Released player ${player.hashCode()} to available pool")
        }
        Log.i(
            TAG,
            "All players released - Available: ${availablePlayers.size}, InUse: ${inUsePlayers.size}"
        )
    }

    fun shutdown() {
        Log.d(TAG, "Shutting down pool")
        for (player in availablePlayers) {
            player.release()
            Log.v(TAG, "Released available player ${player.hashCode()}")
        }
        for (player in inUsePlayers) {
            player.release()
            Log.v(TAG, "Released in-use player ${player.hashCode()}")
        }
        activeRunningPlayer = null
        availablePlayers.clear()
        inUsePlayers.clear()
        Log.i(TAG, "Pool shutdown complete - All players released")
    }

    fun logPoolStatus() {
        Log.d(
            TAG,
            "Pool Status - Available: ${availablePlayers.size}, InUse: ${inUsePlayers.size}, " +
                    "Active: ${activeRunningPlayer?.hashCode() ?: "None"}"
        )
        Log.v(TAG, "Available players: ${availablePlayers.map { it.hashCode() }}")
        Log.v(TAG, "In-use players: ${inUsePlayers.map { it.hashCode() }}")
    }
}