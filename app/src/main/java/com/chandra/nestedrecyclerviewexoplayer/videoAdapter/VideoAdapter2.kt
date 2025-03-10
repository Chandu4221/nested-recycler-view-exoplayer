package com.chandra.nestedrecyclerviewexoplayer.videoAdapter

import android.graphics.Rect
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chandra.nestedrecyclerviewexoplayer.ExoPlayerPool
import com.chandra.nestedrecyclerviewexoplayer.databinding.PostItemLayoutBinding
import com.chandra.nestedrecyclerviewexoplayer.diffUtil.AdapterDiffUtilCallback

class VideoAdapter2(
    var items: List<VideoItem>,
    val playerPool: ExoPlayerPool<Int>
) : RecyclerView.Adapter<VideoAdapter2.ViewHolder2>() {

    private val TAG = this::class.java.simpleName

    // SCROLL LISTENER
    private val recyclerViewScrollListener = object : RecyclerView.OnScrollListener() {
        private val visibilityThreshold: Float = 0.70f // 70% visibility

        private fun checkVisibleItems(recyclerView: RecyclerView) {
            for (i in 0 until recyclerView.childCount) {
                val child = recyclerView.getChildAt(i)
                val position = recyclerView.getChildAdapterPosition(child)

                if (position != RecyclerView.NO_POSITION) {
                    val viewHolder =
                        recyclerView.findViewHolderForAdapterPosition(position) as? ViewHolder2 // Assuming ViewHolder2
                    viewHolder?.let {
                        if (isViewAtLeastPercentVisible(child, visibilityThreshold)) {
                            it.startPlayback()
                        } else {
                            it.pausePlayback()
                        }
                    }
                }
            }
        }

        private fun isViewAtLeastPercentVisible(view: View, percent: Float): Boolean {
            val visibleRect = Rect()
            view.getLocalVisibleRect(visibleRect)

            val viewHeight = view.height
            val visibleHeight = visibleRect.height().toFloat()

            return visibleHeight / viewHeight >= percent
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            checkVisibleItems(recyclerView)
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            // LAYOUT MANAGER
            val layoutManager = recyclerView.layoutManager as? LinearLayoutManager
            // FULLY VISIBLE ITEM
            // FIRST COMPLETE VISIBLE ITEM
            val firstVisibleItemPosition =
                layoutManager?.findFirstCompletelyVisibleItemPosition()
            val lastVisibleItemPosition =
                layoutManager?.findLastCompletelyVisibleItemPosition()
            val totalItemCount = recyclerView.layoutManager?.itemCount
            if (firstVisibleItemPosition == lastVisibleItemPosition && firstVisibleItemPosition != RecyclerView.NO_POSITION) {
                firstVisibleItemPosition?.let { firstVisibleItemPos ->
                    // GET VIEW HOLDER AT THE VISIBLE ITEM POSITION
                    // GET VIEW HOLDER AT THE VISIBLE ITEM POSITION
                    val viewHolder =
                        recyclerView.findViewHolderForAdapterPosition(firstVisibleItemPos) as? ViewHolder2
                    Log.d(TAG, playerPool.logStats())
                }
            }
        }
    }

    inner class ViewHolder2(val binding: PostItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        var exoplayer: ExoPlayer? = null

        init {
            binding.videoPlayerView.useController = false
        }

        fun bindPlayer(position: Int) {
            val videoItem = items[position]
            val mediaItem = MediaItem.fromUri(videoItem.link)
            exoplayer = playerPool.pool(id = position, mediaItem = mediaItem)
            binding.videoPlayerView.player = exoplayer
        }

        fun startPlayback() {
            exoplayer?.play()
        }

        fun pausePlayback() {
            exoplayer?.pause()
        }

        fun unbindPlayer(position: Int) {
            exoplayer?.stop()
            binding.videoPlayerView.player = null
            playerPool.releaseResource(id = position)
            exoplayer = null
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder2 {
        val binding = PostItemLayoutBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder2(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder2, position: Int) {

    }

    override fun getItemCount(): Int = items.size

    // === FREQUENTLY CALLED ===
    override fun onViewAttachedToWindow(holder: ViewHolder2) {
        super.onViewAttachedToWindow(holder)
        val position = holder.bindingAdapterPosition
        holder.bindPlayer(position)
        Log.d(TAG, "onViewAttachedToWindow: $position ${holder.exoplayer.hashCode()} ")
    }

    override fun onViewDetachedFromWindow(holder: ViewHolder2) {
        super.onViewDetachedFromWindow(holder)
        val position = holder.bindingAdapterPosition
        holder.unbindPlayer(position)
        Log.d(TAG, "onViewDetachedFromWindow: $position")
    }
    // === FREQUENTLY CALLED ===

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        recyclerView.addOnScrollListener(recyclerViewScrollListener)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        recyclerView.removeOnScrollListener(recyclerViewScrollListener)
    }

    fun update(newItems: List<VideoItem>) {
        val diffCallback = AdapterDiffUtilCallback(items, newItems)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        items = newItems
        diffResult.dispatchUpdatesTo(this)
    }

}