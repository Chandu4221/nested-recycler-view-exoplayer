package com.chandra.nestedrecyclerviewexoplayer.videoAdapter

/****************
class VideoAdapter(
private val playerPool: ExoPlayerManager,
var items: List<VideoItem>
) :
RecyclerView.Adapter<VideoAdapter.ViewHolder>() {
// TAG
private val TAG = this::class.java.simpleName

private var initialPosition = RecyclerView.NO_POSITION

// SCROLL LISTENER
private val videoAdapterScrollListener = object : RecyclerView.OnScrollListener() {
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
val viewHolder =
recyclerView.findViewHolderForAdapterPosition(firstVisibleItemPos) as VideoAdapter.ViewHolder
viewHolder.completeVisibleItem(position = firstVisibleItemPos)
}
}
}
}

inner class ViewHolder(val binding: PostItemLayoutBinding) :
RecyclerView.ViewHolder(binding.root) {

private var exoPlayer: ExoPlayer? = null

init {
binding.videoPlayerView.apply {
player = null
useController = false
}
}

private fun preparePlayer(item: VideoItem) {
// RELEASE PLAYER IF ANY ATTACHED
releasePlayer()
// ACQUIRE PLAYER FROM THE POOL
exoPlayer = playerPool.acquirePlayer()
// ATTACH PLAYER TO THE VIEW
binding.videoPlayerView.player = exoPlayer
exoPlayer?.apply {
// SET THE MEDIA ITEM
setMediaItem(MediaItem.fromUri(item.link))
// PREPARE THE PLAYER
prepare()
// PLAY WHEN READY
playWhenReady = true
// PLAY
play()
// SET ACTIVE PLAYER IN THE PLAYER MANAGER
playerPool.setActivePlayer(this)
}
}

fun completeVisibleItem(position: Int) {
Log.d(TAG, "COMPLETE_VISIBLE_ITEM $position ")
Log.d(TAG, "BINDING_ADAPTER_POSITION $bindingAdapterPosition  ")

if (exoPlayer == null || !exoPlayer!!.isPlaying) {
preparePlayer(items[position])
}

/****************
 * CODE TO DETERMINE THE VISIBILITY OF POST AT LEAST 75% VISIBLE
val recyclerView = holder.itemView.parent as? RecyclerView
if (recyclerView != null) {
// Get the ViewHolder's view dimensions and position
val view = holder.itemView
val viewTop = view.top
val viewBottom = view.bottom
val viewHeight = view.height

// Get the RecyclerView's visible area
val recyclerTop = recyclerView.paddingTop
val recyclerBottom = recyclerView.height - recyclerView.paddingBottom

// Calculate the visible height of the view
val visibleTop = maxOf(viewTop, recyclerTop)
val visibleBottom = minOf(viewBottom, recyclerBottom)
val visibleHeight = maxOf(0, visibleBottom - visibleTop)

// Calculate visibility percentage
val visibilityPercentage = if (viewHeight > 0) {
(visibleHeight.toFloat() / viewHeight.toFloat()) * 100f
} else {
0f
}

Log.d(
TAG,
"ViewHolder at position ${holder.bindingAdapterPosition}: " +
"Visibility = $visibilityPercentage%, " +
"ViewTop = $viewTop, ViewBottom = $viewBottom, " +
"RecyclerTop = $recyclerTop, RecyclerBottom = $recyclerBottom"
)

// Check if at least 75% visible
if (visibilityPercentage >= 75f) {
holder.preparePlayer(items[holder.bindingAdapterPosition])
Log.d(
TAG,
"Preparing player for position ${holder.bindingAdapterPosition} (75%+ visible)"
)
} else {
Log.d(
TAG,
"Skipping player prep for position ${holder.bindingAdapterPosition} (less than 75% visible)"
)
}
} else {
// Fallback: Prepare player if we can't determine visibility (shouldn't happen)
// holder.preparePlayer(items[holder.bindingAdapterPosition])
Log.w(
TAG,
"Could not determine RecyclerView bounds for position ${holder.bindingAdapterPosition}"
)
}
***************/
}

fun releasePlayer() {
exoPlayer?.let {
if (it == playerPool.activeRunningPlayer) {
playerPool.setActivePlayer(null)
} else {
playerPool.releasePlayer(it)
}
exoPlayer = null
binding.videoPlayerView.player = null
}
}

}

// 2ND
// Called to create ViewHolders as needed.
override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
val binding =
PostItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
return ViewHolder(binding)
}

// 3RD
// Called to bind data to ViewHolders.
override fun onBindViewHolder(holder: ViewHolder, position: Int) {
val item = items[position]
holder.binding.postAuthorName.text = "Index ${item.index}"
}

override fun getItemCount(): Int {
return items.size
}

override fun onViewRecycled(holder: ViewHolder) {
super.onViewRecycled(holder)
holder.releasePlayer()
}

// Called for each ViewHolder as it becomes visible (e.g., when scrolling or initially populating the RecyclerView).
// Repeated multiple times as views enter the visible area
override fun onViewAttachedToWindow(holder: ViewHolder) {
super.onViewAttachedToWindow(holder)
Log.d(TAG, "VIEW_ATTACHED_TO_WINDOW ${holder.bindingAdapterPosition}")
if (initialPosition == RecyclerView.NO_POSITION) {
holder.completeVisibleItem(holder.bindingAdapterPosition)
initialPosition = holder.bindingAdapterPosition
}
}

// Called for each ViewHolder as it leaves the visible area (e.g., when scrolling out of view).
// Paired with onViewAttachedToWindow and repeated as views exit.
override fun onViewDetachedFromWindow(holder: ViewHolder) {
super.onViewDetachedFromWindow(holder)
holder.releasePlayer()
Log.d(TAG, "VIEW_DETACHED_FROM_WINDOW ${holder.bindingAdapterPosition}")
}

// 1ST
// Called once when the adapter is set on the RecyclerView.
// Happens before any views are created or bound
override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
super.onAttachedToRecyclerView(recyclerView)
recyclerView.addOnScrollListener(videoAdapterScrollListener)
}

// LAST
// Called once when the adapter is detached from the RecyclerView.
// Happens after all views are detached and recycled.
override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
super.onDetachedFromRecyclerView(recyclerView)
recyclerView.removeOnScrollListener(videoAdapterScrollListener)
playerPool.releaseAllPlayers()
}

// =====================================================
fun update(newItems: List<VideoItem>) {
val diffCallback = AdapterDiffUtilCallback(items, newItems)
val diffResult = DiffUtil.calculateDiff(diffCallback)
items = newItems
diffResult.dispatchUpdatesTo(this)
}
}
 ******/