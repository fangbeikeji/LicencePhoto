package com.fbkj.licencephoto.model

/**
- @author:  LZC
- @time:  2021/6/2
- @desc:
 */
data class NetResponse<T>(
    var status: Int,
    var msg: String?,
    var data: T?
)
