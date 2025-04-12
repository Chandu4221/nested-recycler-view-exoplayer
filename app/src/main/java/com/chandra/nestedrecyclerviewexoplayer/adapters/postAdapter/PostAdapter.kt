package com.chandra.nestedrecyclerviewexoplayer.adapters.postAdapter

import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.chandra.nestedrecyclerviewexoplayer.adapters.postContentAdapter.PostContentAdapter
import com.chandra.nestedrecyclerviewexoplayer.databinding.PostItemLayoutBinding
import com.chandra.nestedrecyclerviewexoplayer.diffUtil.AdapterDiffUtilCallback
import com.chandra.nestedrecyclerviewexoplayer.managers.exoplayerManager.ExoPlayerManager
import com.chandra.nestedrecyclerviewexoplayer.managers.glideImageManager.GlideImageManager

class PostAdapter(
    private var items: List<PostItem>,
    private val glideImageManager: GlideImageManager,
    private val exoPlayerManager: ExoPlayerManager<String>
) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {
    // TAG
    private val TAG = this::class.java.simpleName

    // WHAT PERCENTAGE OF VIEW SHOULD BE VISIBLE
    private val visibilityThreshold: Float = 0.70f

    // SCROLL LISTENER
    private val postAdapterScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            when (newState) {
                RecyclerView.SCROLL_STATE_IDLE -> {
                    checkVisibleItems(recyclerView)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val postItemLayoutBinding =
            PostItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(postItemLayoutBinding = postItemLayoutBinding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val postItem = items[position]
        holder.bindContentAdapter(postItem = postItem)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        recyclerView.addOnScrollListener(postAdapterScrollListener)
        // START PLAYBACK FOR THE FIRST VIDEO
        recyclerView.post {
            if (recyclerView.isAttachedToWindow) {
                checkVisibleItems(recyclerView)
            }
        }
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        recyclerView.removeOnScrollListener(postAdapterScrollListener)
    }

    override fun onViewAttachedToWindow(holder: PostViewHolder) {
        super.onViewAttachedToWindow(holder)
        val parentPosition = holder.bindingAdapterPosition
        val childViewHolder = holder.getChildViewHolder()
        val childPosition = childViewHolder?.bindingAdapterPosition
        when (childViewHolder) {
            is PostContentAdapter.PostContentImageViewHolder -> {}
            is PostContentAdapter.PostContentVideoViewHolder -> {
                if (childPosition != null) {
                    childViewHolder.bindPlayer(childPosition)
                }
            }
        }
    }

    override fun onViewDetachedFromWindow(holder: PostViewHolder) {
        super.onViewDetachedFromWindow(holder)
        val parentPosition = holder.bindingAdapterPosition
        val childViewHolder = holder.getChildViewHolder()
        val childPosition = childViewHolder?.bindingAdapterPosition
        when (childViewHolder) {
            is PostContentAdapter.PostContentImageViewHolder -> {}
            is PostContentAdapter.PostContentVideoViewHolder -> {
                if (childPosition != null) {
                    childViewHolder.unbindPlayer(childPosition)
                }
            }
        }
    }

    // UPDATE THE POST ITEMS ADAPTER
    fun update(newItems: List<PostItem>) {
        val diffCallback = AdapterDiffUtilCallback(items, newItems)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        items = newItems
        diffResult.dispatchUpdatesTo(this)
    }

    // FUNCTION TO CHECK THE VIEW VISIBILITY BASED ON PERCENTAGE
    // THAT IS IS VIEW 70% VISIBLE
    private fun isViewAtLeastPercentVisible(view: View, percent: Float): Boolean {
        val visibleRect = Rect()
        view.getLocalVisibleRect(visibleRect)
        val viewHeight = view.height
        val visibleHeight = visibleRect.height().toFloat()
        return visibleHeight / viewHeight >= percent
    }

    // FUNCTION TO CHECK THE VISIBLE ITEMS
    // AND CONTROL THE VIDE PLAYBACK
    private fun checkVisibleItems(recyclerView: RecyclerView) {
        for (i in 0 until recyclerView.childCount) {
            // FIND THE VIEW IN THE RECYCLER VIEW AT INDEX i
            val cardView = recyclerView.getChildAt(i)
            // FIND THE ADAPTER POSITION OF THE VIEW
            val cardViewAdapterPosition = recyclerView.getChildAdapterPosition(cardView)
            // IF ADAPTER POSITION IS NOT -1
            if (cardViewAdapterPosition != RecyclerView.NO_POSITION) {
                // FIND THE VIEW HOLDER AT THAT POSITION
                val viewHolderAtPosition =
                    recyclerView.findViewHolderForAdapterPosition(cardViewAdapterPosition)
                // IF VIEW HOLDER IS POST VIEW HOLDER
                when (viewHolderAtPosition) {
                    is PostViewHolder -> {
                        val childViewHolder = viewHolderAtPosition.getChildViewHolder()
                        // IF PARENT VIEW HOLDER IS VISIBLE AT LEAST 70%
                        val isVisible = isViewAtLeastPercentVisible(
                            viewHolderAtPosition.itemView,
                            visibilityThreshold
                        )
                        if (isVisible) {
                            when (childViewHolder) {
                                is PostContentAdapter.PostContentImageViewHolder -> {}
                                is PostContentAdapter.PostContentVideoViewHolder -> {
                                    childViewHolder.startPlayback()
                                }
                            }
                        } else {
                            when (childViewHolder) {
                                is PostContentAdapter.PostContentImageViewHolder -> {}
                                is PostContentAdapter.PostContentVideoViewHolder -> {
                                    childViewHolder.pausePlayback()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // VIEW HOLDER CLASS
    inner class PostViewHolder(val postItemLayoutBinding: PostItemLayoutBinding) :
        RecyclerView.ViewHolder(postItemLayoutBinding.root) {
        // INITIALIZE THE CONTENT ADAPTER
        private var contentAdapter: PostContentAdapter = PostContentAdapter(
            items = emptyList(),
            glideImageManager = glideImageManager,
            exoPlayerManager = exoPlayerManager
        )

        init {
            val postContentScrollListener = object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                        val visiblePosition = layoutManager.findFirstCompletelyVisibleItemPosition()
                        if (visiblePosition != RecyclerView.NO_POSITION) {
                            updateActiveItemIndex(visiblePosition)
                        }
                    }
                }
            }

            val snapHelper = PagerSnapHelper()
            snapHelper.attachToRecyclerView(postItemLayoutBinding.postContentRecyclerView)
            // SETUP RECYCLER VIEW ADAPTER AND LAYOUT MANAGER
            postItemLayoutBinding.postContentRecyclerView.apply {
                adapter = contentAdapter
                layoutManager = LinearLayoutManager(
                    postItemLayoutBinding.postContentRecyclerView.context,
                    LinearLayoutManager.HORIZONTAL,
                    false
                )
                addOnScrollListener(postContentScrollListener)
            }
        }

        fun bindContentAdapter(postItem: PostItem) {
            contentAdapter.update(newItems = postItem.content)
            updateActiveItemIndex(position = 0)
        }

        // FUNCTION TO UPDATE THE ACTIVE ITEM
        private fun updateActiveItemIndex(position: Int) {
            val item = items[bindingAdapterPosition]
            val contentItemsSize = item.content.size
            if (contentItemsSize > 1) { // contentAdapter.itemCount > 1
                postItemLayoutBinding.postCounter.visibility = View.VISIBLE
                postItemLayoutBinding.postCounter.text =
                    "${position + 1}/${contentItemsSize}"
            } else {
                postItemLayoutBinding.postCounter.visibility = View.GONE
            }
        }

        //  GET THE CHILD VIEW HOLDER
        fun getChildViewHolder(): RecyclerView.ViewHolder? {
            val layoutManager =
                postItemLayoutBinding.postContentRecyclerView.layoutManager as? LinearLayoutManager
            val visiblePosition =
                layoutManager?.findFirstVisibleItemPosition() ?: RecyclerView.NO_POSITION
            return if (visiblePosition != RecyclerView.NO_POSITION) {
                val childViewHolder =
                    postItemLayoutBinding.postContentRecyclerView.findViewHolderForAdapterPosition(
                        visiblePosition
                    )
                childViewHolder
            } else {
                null
            }
        }
    }
}