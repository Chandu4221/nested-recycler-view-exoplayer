package com.chandra.nestedrecyclerviewexoplayer.adapters.postContentAdapter

import android.graphics.Rect
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.chandra.nestedrecyclerviewexoplayer.databinding.PostContentImageItemBinding
import com.chandra.nestedrecyclerviewexoplayer.databinding.PostContentVideoItemBinding
import com.chandra.nestedrecyclerviewexoplayer.diffUtil.AdapterDiffUtilCallback
import com.chandra.nestedrecyclerviewexoplayer.managers.exoplayerManager.ExoPlayerManager
import com.chandra.nestedrecyclerviewexoplayer.managers.glideImageManager.GlideImageManager

class PostContentAdapter(
    private var items: List<PostContentItem>,
    private val glideImageManager: GlideImageManager,
    private val exoPlayerManager: ExoPlayerManager<String>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    // TAG
    private val TAG = this::class.java.simpleName

    // SCROLL LISTENER
    private val postContentScrollListener = object : RecyclerView.OnScrollListener() {
        private val visibilityThreshold: Float = 0.70f // 70% visibility
        private fun checkVisibleItems(recyclerView: RecyclerView) {
            for (i in 0 until recyclerView.childCount) {
                val child = recyclerView.getChildAt(i)
                val position = recyclerView.getChildAdapterPosition(child)

                if (position != RecyclerView.NO_POSITION) {
                    val viewHolder = recyclerView.findViewHolderForAdapterPosition(position)
                    when (viewHolder) {
                        is PostContentImageViewHolder -> {}

                        is PostContentVideoViewHolder -> {
                            if (isViewAtLeastPercentVisible(child, visibilityThreshold)) {
                                viewHolder.startPlayback()
                            } else {
                                viewHolder.pausePlayback()
                            }
                        }
                    }
                }
            }
        }

        private fun isViewAtLeastPercentVisible(view: View, percent: Float): Boolean {
            val visibleRect = Rect()
            view.getLocalVisibleRect(visibleRect)
            val viewWidth = view.width
            val visibleWidth = visibleRect.width().toFloat()
            return visibleWidth / viewWidth >= percent
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            checkVisibleItems(recyclerView)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            PostContentItemType.IMAGE.ordinal -> {
                val postContentImageItemBinding =
                    PostContentImageItemBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                PostContentImageViewHolder(postContentImageItemBinding)
            }

            PostContentItemType.VIDEO.ordinal -> {
                val postContentVideoItemBinding =
                    PostContentVideoItemBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                PostContentVideoViewHolder(postContentVideoItemBinding)
            }

            else -> {
                throw IllegalArgumentException("Invalid view type")
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        return when (val item = items[position]) {
            is PostContentItem.Image -> {
                val postContentImageViewHolder = holder as PostContentImageViewHolder
                postContentImageViewHolder.bind(position = position)
            }

            is PostContentItem.Video -> {}
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is PostContentItem.Image -> PostContentItemType.IMAGE.ordinal
            is PostContentItem.Video -> PostContentItemType.VIDEO.ordinal
        }
    }

    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        super.onViewAttachedToWindow(holder)
        when (holder) {
            is PostContentImageViewHolder -> {}
            is PostContentVideoViewHolder -> {
                val position = holder.bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    holder.bindPlayer(position)
                }
            }
        }
    }

    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        super.onViewDetachedFromWindow(holder)
        when (holder) {
            is PostContentImageViewHolder -> {}
            is PostContentVideoViewHolder -> {
                val position = holder.bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    holder.unbindPlayer(position)
                }
            }
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        recyclerView.addOnScrollListener(postContentScrollListener)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        recyclerView.removeOnScrollListener(postContentScrollListener)
    }

    fun update(newItems: List<PostContentItem>) {
        val diffCallback = AdapterDiffUtilCallback(items, newItems)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        items = newItems
        diffResult.dispatchUpdatesTo(this)
    }

    // VIEW HOLDERS
    inner class PostContentImageViewHolder(private val postContentImageItemBinding: PostContentImageItemBinding) :
        RecyclerView.ViewHolder(postContentImageItemBinding.root) {

        fun bind(position: Int) {
            val item = items[position] as PostContentItem.Image
            Log.d(TAG, "IMAGE_URL ${item.imageUrl}")
            // LOAD THE IMAGE URL
            glideImageManager.loadImage(
                item.imageUrl,
                postContentImageItemBinding.postContentImageView
            )
        }
    }

    inner class PostContentVideoViewHolder(private val postContentVideoItemBinding: PostContentVideoItemBinding) :
        RecyclerView.ViewHolder(postContentVideoItemBinding.root) {
        private var exoPlayer: ExoPlayer? = null

        init {
            // DISABLE DEFAULT CONTROLS
            postContentVideoItemBinding.postContentVideoView.useController = false
        }

        fun bindPlayer(position: Int) {
            val item = items[position] as PostContentItem.Video
            Log.d(TAG, "VIDEO_URL ${item.videoUrl}")
            val mediaItem = MediaItem.fromUri(item.videoUrl)
            exoPlayer = exoPlayerManager.pool(id = item.id, mediaItem = mediaItem)
            postContentVideoItemBinding.postContentVideoView.player = exoPlayer
        }

        fun startPlayback() {
            exoPlayer?.play()
        }

        fun pausePlayback() {
            exoPlayer?.pause()
        }

        fun unbindPlayer(position: Int) {
            val item = items[position] as PostContentItem.Video
            exoPlayer?.stop()
            postContentVideoItemBinding.postContentVideoView.player = null
            exoPlayerManager.releaseResource(id = item.id)
            exoPlayer = null
        }

    }
}