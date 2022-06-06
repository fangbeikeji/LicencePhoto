package com.fbkj.licencephoto.model

/**
- @author:  LZC
- @time:  2021/6/17
- @desc:
 */
data class ReloadModel (
    var data: List<ReloadData>
)
data class ReloadData(
    var dressName: String ,
    var dressIndex: Int ,
    var url: String ,
    var image: Int,
    var choose: Boolean=false
)