package com.chandra.nestedrecyclerviewexoplayer.adapters.postAdapter

import android.graphics.Rect
import android.util.Log
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

    // SCROLL LISTENER
    private val postAdapterScrollListener = object : RecyclerView.OnScrollListener() {
        private val visibilityThreshold: Float = 0.70f // 70% visibility
        private fun checkVisibleItems(recyclerView: RecyclerView) {
            for (i in 0 until recyclerView.childCount) {
                val child = recyclerView.getChildAt(i)
                val position = recyclerView.getChildAdapterPosition(child)
                if (position != RecyclerView.NO_POSITION) {
                    val viewHolder =
                        recyclerView.findViewHolderForAdapterPosition(position)
                    when (viewHolder) {
                        is PostViewHolder -> {
                            // IF POST VIEW HOLDER IS VISIBLE 70%
                            val isVisible = isViewAtLeastPercentVisible(child, visibilityThreshold)
                            val childViewHolder = viewHolder.getChildViewHolder()
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

        private fun isViewAtLeastPercentVisible(view: View, percent: Float): Boolean {
            val visibleRect = Rect()
            view.getLocalVisibleRect(visibleRect)

            val viewHeight = view.height
            val visibleHeight = visibleRect.height().toFloat()

            return visibleHeight / viewHeight >= percent
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            // checkVisibleItems(recyclerView)
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            /**********
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
            recyclerView.findViewHolderForAdapterPosition(firstVisibleItemPos)
            when (viewHolder) {
            is PostViewHolder -> {}
            }
            }
            }
             ************/
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
        Log.d(TAG, "onViewAttachedToWindow: PARENT_POS $parentPosition CHILD_POS $childPosition")
        when (childViewHolder) {
            is PostContentAdapter.PostContentImageViewHolder -> {}
            is PostContentAdapter.PostContentVideoViewHolder -> {
                childViewHolder.bindPlayer(childPosition!!)
            }
        }
    }

    override fun onViewDetachedFromWindow(holder: PostViewHolder) {
        super.onViewDetachedFromWindow(holder)
        val parentPosition = holder.bindingAdapterPosition
        val childViewHolder = holder.getChildViewHolder()
        val childPosition = childViewHolder?.bindingAdapterPosition
        Log.d(TAG, "onViewAttachedToWindow: PARENT_POS $parentPosition CHILD_POS $childPosition")
        when (childViewHolder) {
            is PostContentAdapter.PostContentImageViewHolder -> {}
            is PostContentAdapter.PostContentVideoViewHolder -> {
                childViewHolder.unbindPlayer(childPosition!!)
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
            updateActiveItemIndex(position = 0)
        }

        fun bindContentAdapter(postItem: PostItem) {
            contentAdapter.update(newItems = postItem.content)
        }

        // FUNCTION TO UPDATE THE ACTIVE ITEM
        private fun updateActiveItemIndex(position: Int) {
            if (contentAdapter.itemCount > 1) {
                postItemLayoutBinding.postCounter.visibility = View.VISIBLE
                postItemLayoutBinding.postCounter.text =
                    "${position + 1}/${contentAdapter.itemCount}"
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