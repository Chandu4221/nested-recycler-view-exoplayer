package com.chandra.nestedrecyclerviewexoplayer

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.chandra.nestedrecyclerviewexoplayer.databinding.ActivityMainBinding
import com.chandra.nestedrecyclerviewexoplayer.videoAdapter.VideoAdapter
import com.chandra.nestedrecyclerviewexoplayer.videoAdapter.VideoItem

class MainActivity : AppCompatActivity() {

    // TAG
    private val TAG = this::class.java.simpleName

    // VIEW BINDING
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: VideoAdapter
    private lateinit var exoPlayerPool: ExoPlayerManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "ON_CREATE")
        // EDGE TO EDGE SUPPORT
        // enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        exoPlayerPool = ExoPlayerManager(context = this, poolSize = 1)
        // CREATE THE ADAPTER
        adapter =
            VideoAdapter(
                playerPool = exoPlayerPool,
                items = emptyList()
            )
        binding.videoRecyclerView.adapter = adapter
        val videoLayoutManager = LinearLayoutManager(this)
        binding.videoRecyclerView.layoutManager = videoLayoutManager
        adapter.update(newItems = SampleVideoLinks.links.mapIndexed { index, s ->
            VideoItem(
                link = s,
                index = index
            )
        })


        /************
        binding.videoRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        super.onScrollStateChanged(recyclerView, newState)

        // FULLY VISIBLE ITEM
        val firstVisibleItemPosition =
        videoLayoutManager.findFirstCompletelyVisibleItemPosition()
        val lastVisibleItemPosition =
        videoLayoutManager.findLastCompletelyVisibleItemPosition()
        val totalItemCount = videoLayoutManager.itemCount

        Log.d(TAG, "FIRST $firstVisibleItemPosition LAST $lastVisibleItemPosition ")

        // Ensure firstVisibleItemPosition is valid
        //                if (firstVisibleItemPosition != RecyclerView.NO_POSITION) {
        //                    val viewHolder =
        //                        recyclerView.findViewHolderForAdapterPosition(firstVisibleItemPosition)
        //
        //                    if (viewHolder is VideoAdapter.ViewHolder) {
        //                        // Now it's safe to use viewHolder
        //                        Log.d(TAG, "ViewHolder found for position: $firstVisibleItemPosition")
        //                    } else {
        //                        Log.d(TAG, "ViewHolder is null or not of type VideoAdapter.ViewHolder")
        //                    }
        //                }

        if (firstVisibleItemPosition == lastVisibleItemPosition && firstVisibleItemPosition != RecyclerView.NO_POSITION) {
        val viewHolder =
        recyclerView.findViewHolderForAdapterPosition(firstVisibleItemPosition) as VideoAdapter.ViewHolder
        viewHolder.completeVisibleItem(firstVisibleItemPosition)
        Log.d(TAG, "VIEW HOLDER VISIBLE AT : $firstVisibleItemPosition")
        }
        //
        //                when {
        //                    firstVisibleItemPosition == 0 -> {
        //                        Log.d(TAG, "First item is fully visible")
        //                        val viewHolder =
        //                            recyclerView.findViewHolderForAdapterPosition(firstVisibleItemPosition) as VideoAdapter.ViewHolder
        //                        viewHolder.completeVisibleItem(firstVisibleItemPosition)
        //
        //                    }
        //
        //                    lastVisibleItemPosition == totalItemCount - 1 -> {
        //                        Log.d(TAG, "Last item is fully visible")
        //                        val viewHolder =
        //                            recyclerView.findViewHolderForAdapterPosition(lastVisibleItemPosition) as VideoAdapter.ViewHolder
        //                        viewHolder.completeVisibleItem(lastVisibleItemPosition)
        //
        //                    }
        //
        //                    firstVisibleItemPosition > 0 && lastVisibleItemPosition < totalItemCount - 1 -> {
        //                        Log.d(TAG, "Middle items are visible")
        //                        val viewHolder =
        //                            recyclerView.findViewHolderForAdapterPosition(firstVisibleItemPosition) as VideoAdapter.ViewHolder
        //                        viewHolder.completeVisibleItem(firstVisibleItemPosition)
        //
        //                    }
        //                }
        }
        })
         ************/
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayerPool.shutdown()
        Log.d(TAG, "ON_DESTROY")
    }
}