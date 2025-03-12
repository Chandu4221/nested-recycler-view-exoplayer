package com.chandra.nestedrecyclerviewexoplayer.sampleData

import com.chandra.nestedrecyclerviewexoplayer.adapters.postAdapter.PostItem
import com.chandra.nestedrecyclerviewexoplayer.adapters.postContentAdapter.PostContentItem

object SampleData {
    val posts = listOf(
        PostItem(
            content = listOf(
                PostContentItem.Image(imageUrl = "https://cdn.jsdelivr.net/gh/faker-js/assets-person-portrait/male/512/10.jpg"),
                PostContentItem.Video(videoUrl = "https://storage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4")
            )
        ), // POST ITEM - 1
        PostItem(
            content = listOf(
                PostContentItem.Image(imageUrl = "https://cdn.jsdelivr.net/gh/faker-js/assets-person-portrait/male/512/20.jpg"),
                PostContentItem.Video(videoUrl = "https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyrides.mp4")
            )
        ), // POST ITEM - 2
        PostItem(
            content = listOf(
                PostContentItem.Image(imageUrl = "https://cdn.jsdelivr.net/gh/faker-js/assets-person-portrait/male/512/30.jpg"),
            )
        ), // POST ITEM - 3
        PostItem(
            content = listOf(
                PostContentItem.Video(videoUrl = "https://storage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4")
            )
        ), // POST ITEM - 4
        PostItem(
            content = listOf(
                PostContentItem.Image(imageUrl = "https://cdn.jsdelivr.net/gh/faker-js/assets-person-portrait/male/512/40.jpg"),
                PostContentItem.Video(videoUrl = "https://storage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4")
            )
        ), // POST ITEM - 5
        PostItem(
            content = listOf(
                PostContentItem.Video(videoUrl = "https://storage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4"),
                PostContentItem.Video(videoUrl = "https://storage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"),
            )
        ), // POST ITEM - 6
        PostItem(
            content = listOf(
                PostContentItem.Image(imageUrl = "https://cdn.jsdelivr.net/gh/faker-js/assets-person-portrait/male/512/50.jpg"),
                PostContentItem.Video(videoUrl = "https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4"),
                PostContentItem.Video(videoUrl = "https://storage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4"),
            )
        ), // POST ITEM - 7
    )
}