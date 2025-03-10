package com.chandra.nestedrecyclerviewexoplayer

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.chandra.nestedrecyclerviewexoplayer.databinding.ActivityMainBinding
import com.chandra.nestedrecyclerviewexoplayer.videoAdapter.VideoAdapter2
import com.chandra.nestedrecyclerviewexoplayer.videoAdapter.VideoItem

class MainActivity : AppCompatActivity() {

    // TAG
    private val TAG = this::class.java.simpleName

    // VIEW BINDING
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: VideoAdapter2
    private lateinit var playerPool: ExoPlayerPool<Int>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // CREATE PLAYER POOL
        playerPool = ExoPlayerPool(context = this, poolSize = 3)

        // CREATE THE ADAPTER
        adapter = VideoAdapter2(items = emptyList(), playerPool = playerPool)
        binding.videoRecyclerView.adapter = adapter
        val videoLayoutManager = LinearLayoutManager(this)
        binding.videoRecyclerView.layoutManager = videoLayoutManager
        adapter.update(newItems = SampleVideoLinks.links.mapIndexed { index, s ->
            VideoItem(link = s, index = index)
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        playerPool.releaseAllPlayers()
    }
}