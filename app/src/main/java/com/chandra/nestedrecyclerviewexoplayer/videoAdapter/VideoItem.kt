package com.chandra.nestedrecyclerviewexoplayer.videoAdapter

import com.chandra.nestedrecyclerviewexoplayer.diffUtil.Identifiable
import java.util.UUID

data class VideoItem(
    override val id: String = UUID.randomUUID().toString(),
    val index: Int,
    val link: String
) : Identifiable