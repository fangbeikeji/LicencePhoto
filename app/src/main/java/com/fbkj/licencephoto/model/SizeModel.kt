package com.fbkj.licencephoto.model

/**
- @author:  LZC
- @time:  2021/6/3
- @desc:
 */

data class SizeModel(
    var fetcher: List<NormalData>
)

data class NormalData(
    var sizePixel: String,
    var sizeIndex: Int,
    var sizeName: String,
    var sizeUnit: String,
    var choose: Boolean=false
)