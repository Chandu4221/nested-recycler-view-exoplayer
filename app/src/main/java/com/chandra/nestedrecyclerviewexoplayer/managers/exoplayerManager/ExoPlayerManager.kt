package com.chandra.nestedrecyclerviewexoplayer.managers.exoplayerManager

import android.content.Context
import androidx.media3.common.MediaItem
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean

class ExoPlayerManager<T>(
    private val context: Context,
    private val initialPoolSize: Int = 3,
    private val maxPoolSize: Int = 5
) {

    init {
        // MAKE SURE INITIAL POOL SIZE IS NOT GREATER THAN MAX POOL SIZE AND POSITIVE
        require(initialPoolSize > 0) { "initialPoolSize must be positive." }
        require(maxPoolSize >= initialPoolSize) { "maxPoolSize must be >= initialPoolSize." }
    }

    // CREATE LIST OF AVAILABLE PLAYERS
    private val availablePlayers = ConcurrentLinkedQueue<ExoPlayerWrapper>()

    // CREATE LIST OF IN USE PLAYERS
    private val inUsePlayers = ConcurrentHashMap<T, ExoPlayerWrapper>()

    // FLAG TO PREVENT OPERATIONS AFTER THE POOL IS DESTROYED
    private var isPoolReleased = AtomicBoolean(false)

    // INITIALIZE THE PLAYERS
    init {
        repeat(initialPoolSize) {
            availablePlayers.add(createNewPlayer())
        }
    }

    // CREATE NEW PLAYER INSTANCE
    private fun createNewPlayer(): ExoPlayerWrapper {
        return ExoPlayerWrapper(context.applicationContext)
    }

    // ACQUIRE PLAYER
    @Synchronized
    fun acquirePlayer(playerId: T, mediaItem: MediaItem): ExoPlayerWrapper? {
        // IF POOL IS RELEASED
        if (isPoolReleased.get()) return null

        // IF PLAYER IS PRESENT IN USE PLAYERS RETURN THAT PLAYER
        if (inUsePlayers.containsKey(playerId)) {
            return inUsePlayers[playerId]
        }

        var acquiredPlayer: ExoPlayerWrapper?

        // GET PLAYER FROM AVAILABLE PLAYERS AND RETURN
        acquiredPlayer = availablePlayers.poll()

        // IF THERE ARE NO AVAILABLE PLAYERS AND POOL IS NOT FULL
        if (acquiredPlayer == null) {
            if (inUsePlayers.size + availablePlayers.size < maxPoolSize) {
                acquiredPlayer = createNewPlayer()
                availablePlayers.add(acquiredPlayer)
            }
        }

        // IF THERE IS A PLAYER AVAILABLE
        if (acquiredPlayer != null) {
            acquiredPlayer.setMediaItem(mediaItem)
            acquiredPlayer.prepare()
            inUsePlayers[playerId] = acquiredPlayer
            return acquiredPlayer
        }

        return null
    }

    // RELEASE PLAYER AND ADD IT TO AVAILABLE PLAYERS
    @Synchronized
    fun releasePlayer(playerId: T): Boolean {
        // IF POOL IS RELEASED
        if (isPoolReleased.get()) return false

        // REMOVE THE PLAYER FORM THE IN USE PLAYERS
        // IF NOT FOUND RETURN FALSE
        val releasedPlayerWrapper = inUsePlayers.remove(playerId) ?: return false

        // CHECK IF THE AVAILABLE PLAYER POOL IS NOT FULL
        if (availablePlayers.size < maxPoolSize) {
            releasedPlayerWrapper.stopAndReset()
            availablePlayers.offer(releasedPlayerWrapper)
        } else {
            releasedPlayerWrapper.release()
        }
        return true
    }

    // RELEASE ALL PLAYERS
    @Synchronized
    fun releaseAllPlayers() {
        // IF POOL IS RELEASED RETURN
        if (isPoolReleased.get()) return
        // RELEASE ALL AVAILABLE PLAYERS
        availablePlayers.forEach { wrapper -> wrapper.release() }
        // CLEAR AVAILABLE PLAYERS
        availablePlayers.clear()
        // RELEASE ALL IN USE PLAYERS
        inUsePlayers.values.forEach { wrapper -> wrapper.release() }
        // CLEAR IN USE PLAYERS
        inUsePlayers.clear()
        // SET POOL RELEASED FLAG TO TRUE
        isPoolReleased = AtomicBoolean(true)
    }

    // PAUSE ALL PLAYERS
    @Synchronized
    fun pauseAllPlayers() {
        // IF POOL IS RELEASED RETURN
        if (isPoolReleased.get()) return
        // PAUSE ALL AVAILABLE PLAYERS
        availablePlayers.forEach { wrapper -> wrapper.pause() }
        // PAUSE ALL IN USE PLAYERS
        inUsePlayers.values.forEach { wrapper -> wrapper.pause() }
    }

    // MUTE ALL PLAYERS
    @Synchronized
    fun muteAllPlayers() {
        // IF POOL IS RELEASED
        if (isPoolReleased.get()) return
        // MUTE ALL AVAILABLE PLAYERS
        (availablePlayers + inUsePlayers.values).forEach { wrapper -> wrapper.setIsMuted(muted = true) }
    }

    // UN MUTE ALL PLAYERS
    @Synchronized
    fun unMuteAllPlayers() {
        // IF POOL IS RELEASED
        if (isPoolReleased.get()) return
        // UN MUTE ALL AVAILABLE PLAYERS
        (availablePlayers + inUsePlayers.values).forEach { wrapper -> wrapper.setIsMuted(muted = false) }
    }

    // ARE ALL PLAYERS MUTED
    fun areAllPlayersMuted(): Boolean {
        // IF POOL IS RELEASED
        if (isPoolReleased.get()) return false
        val allPlayers = availablePlayers + inUsePlayers.values

        if (allPlayers.isEmpty()) return true

        return allPlayers.all { it.isMuted }
    }

    // LOG STATS
    fun logStats(): String {
        return """
            POOL RELEASED: ${isPoolReleased.get()}
            AVAILABLE PLAYER SIZE: ${availablePlayers.size}
            IN USE PLAYER SIZE: ${inUsePlayers.size}
            TOTAL PLAYERS SIZE: ${availablePlayers.size + inUsePlayers.size}
            ----------------------------------------------------------------
            AVAILABLE PLAYERS: ${availablePlayers.map { it.hashCode() }}
            IN USE PLAYERS: ${inUsePlayers.map { it.key }}
        """
    }
}