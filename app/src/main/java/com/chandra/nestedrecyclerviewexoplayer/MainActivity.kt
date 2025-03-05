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
    }

    override fun onStop() {
        super.onStop()
        exoPlayerPool.releaseAllPlayers()
        Log.d(TAG, "ON_STOP")
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayerPool.shutdown()
        Log.d(TAG, "ON_DESTROY")
    }
}