package com.fbkj.licencephoto.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UpdateModel(
    var apk_url: String,
    var create_time: String,
    var description: String,
    var id: Int,
    var must_update: Boolean,
    var pkg_id: Int,
    var version_code: Int,
    var version_name: String,
): Parcelable