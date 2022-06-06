package com.fbkj.licencephoto.model

/**
- @author:  LZC
- @time:  2021/7/12
- @desc:
 */
data class SnapLoginModel(
    var createAt: String,
    var id: Int,
    var isSnap: Boolean,
    var nickname: String,
    var phone: String,
    var token: String
)