package com.chandra.nestedrecyclerviewexoplayer.managers.glideImageManager

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.chandra.nestedrecyclerviewexoplayer.R

object GlideImageManager {

    fun loadImage(imageUrl: String, imageView: ImageView) {
        Glide.with(imageView.context)
            .load(imageUrl)
            .apply(
                RequestOptions()
                    .placeholder(R.drawable.placeholder_image) // Optional: Add a placeholder drawable
                    .error(R.drawable.error_image) // Optional: Add an error drawable
                    .diskCacheStrategy(DiskCacheStrategy.ALL) // Cache both original & resized images
                    .centerCrop() // Scale image to fill the ImageView
            )
            .into(imageView)
    }
}