package com.fbkj.licencephoto.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
- @author:  LZC
- @time:  2021/6/24
- @desc:
 */

@Parcelize
class UserModel(
    var id: String,
    var nickname: String,
    var phone: String,
    var createAt: String,
    var token: String,
): Parcelable