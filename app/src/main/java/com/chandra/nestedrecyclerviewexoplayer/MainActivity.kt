package com.chandra.nestedrecyclerviewexoplayer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.chandra.nestedrecyclerviewexoplayer.adapters.postAdapter.PostAdapter
import com.chandra.nestedrecyclerviewexoplayer.databinding.ActivityMainBinding
import com.chandra.nestedrecyclerviewexoplayer.managers.exoplayerManager.ExoPlayerManager
import com.chandra.nestedrecyclerviewexoplayer.managers.glideImageManager.GlideImageManager
import com.chandra.nestedrecyclerviewexoplayer.sampleData.SampleData

class MainActivity : AppCompatActivity() {

    // TAG
    private val TAG = this::class.java.simpleName

    // VIEW BINDING
    private lateinit var binding: ActivityMainBinding
    private lateinit var exoPlayerManager: ExoPlayerManager<String>

    private lateinit var postAdapter: PostAdapter
    private val glideImageManager = GlideImageManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // CREATE PLAYER POOL
        exoPlayerManager = ExoPlayerManager(context = this, initialPoolSize = 3, maxPoolSize = 5)

        // CREATE THE ADAPTER
        postAdapter = PostAdapter(
            items = emptyList(),
            glideImageManager = glideImageManager,
            exoPlayerManager = exoPlayerManager
        )
        binding.postRecyclerView.adapter = postAdapter
        val postLayoutManager = LinearLayoutManager(this)
        binding.postRecyclerView.layoutManager = postLayoutManager
        postAdapter.update(newItems = SampleData.posts)
    }

    override fun onStop() {
        super.onStop()
        exoPlayerManager.pauseAllPlayers()
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayerManager.releaseAllPlayers()
    }
}