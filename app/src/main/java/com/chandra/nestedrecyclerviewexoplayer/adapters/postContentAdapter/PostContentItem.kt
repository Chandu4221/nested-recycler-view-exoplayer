package com.chandra.nestedrecyclerviewexoplayer.adapters.postContentAdapter

import com.chandra.nestedrecyclerviewexoplayer.diffUtil.Identifiable
import java.util.UUID


/**
 * ENUM CLASS FOR DIFFERENT CONTENT TYPES
 * */
enum class PostContentItemType {
    IMAGE,
    VIDEO
}

/**
 * SEALED CLASS FOR DIFFERENT TYPES OF POSTS
 * */
sealed class PostContentItem(val contentType: PostContentItemType) : Identifiable {
    abstract override val id: String

    data class Image(
        val imageUrl: String,
        override val id: String = UUID.randomUUID().toString()
    ) : PostContentItem(contentType = PostContentItemType.IMAGE), Identifiable

    data class Video(
        val videoUrl: String,
        override val id: String = UUID.randomUUID().toString()
    ) : PostContentItem(contentType = PostContentItemType.VIDEO), Identifiable
}