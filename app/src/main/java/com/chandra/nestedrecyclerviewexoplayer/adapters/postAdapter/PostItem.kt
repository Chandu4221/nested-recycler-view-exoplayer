package com.chandra.nestedrecyclerviewexoplayer.adapters.postAdapter

import com.chandra.nestedrecyclerviewexoplayer.adapters.postContentAdapter.PostContentItem
import com.chandra.nestedrecyclerviewexoplayer.diffUtil.Identifiable
import java.util.UUID

data class PostItem(
    override val id: String = UUID.randomUUID().toString(),
    val content: List<PostContentItem>
) : Identifiable
