@file:Suppress("NOTHING_TO_INLINE", "unused")

package com.fbkj.licencephoto.utils
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.Transformation

inline fun ImageView.load(
    uri: Uri, fragment: Fragment,
    vararg transformations: Transformation<Bitmap>
) =
    Glide.with(fragment)
        .load(uri)
        .transform(*transformations)
        .into(this)

inline fun ImageView.load(
    url: String, fragment: Fragment,
    vararg transformations: Transformation<Bitmap>
) =
    Glide.with(fragment)
        .load(url)
        .transform(*transformations)
        .into(this)

inline fun ImageView.load(
    url: String, fragment: Fragment,
    placeholderId: Int? = null, errId: Int? = null,
    transformation: Transformation<Bitmap>? = null
) =
    Glide.with(fragment)
        .load(url)
        .apply {
            placeholderId?.let { this.placeholder(placeholderId) }
            errId?.let { this.error(errId) }
            transformation?.let { this.transform(it) }
        }
        .into(this)

inline fun ImageView.load(
    url: String, context: Context? = null,
    placeholderId: Int? = null, errId: Int? = null,
    transformation: Transformation<Bitmap>? = null
) =
    Glide.with(context ?: this.context)
        .load(url)
        .apply {
            placeholderId?.let { this.placeholder(placeholderId) }
            errId?.let { this.error(errId) }
            transformation?.let { this.transform(it) }
        }
        .into(this)

inline fun ImageView.load(uri: Uri, context: Context? = null) =
    Glide.with(context ?: this.context)
        .load(uri)
        .into(this)

inline fun ImageView.load(
    url: String, context: Context? = null,
    vararg transformations: Transformation<Bitmap>
) =
    Glide.with(context ?: this.context)
        .load(url)
        .transform(*transformations)
        .into(this)

inline fun ImageView.load(
    url: String, context: Context? = null,
    placeholderId: Int? = null, errId: Int? = null,
    vararg transformations: Transformation<Bitmap>
) =
    Glide.with(context ?: this.context)
        .load(url)
        .apply {
            placeholderId?.let { this.placeholder(placeholderId) }
            errId?.let { this.error(errId) }
            transform(*transformations)
        }
        .into(this)

inline fun ImageView.load(
    url: String, fragment: Fragment,
    placeholderId: Int? = null, errId: Int? = null,
    vararg transformations: Transformation<Bitmap>
) =
    Glide.with(fragment)
        .load(url)
        .apply {
            placeholderId?.let { this.placeholder(placeholderId) }
            errId?.let { this.error(errId) }
            transform(*transformations)
        }
        .into(this)
