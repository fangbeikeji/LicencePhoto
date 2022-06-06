package com.fbkj.licencephoto.model

/**
- @author:  LZC
- @time:  2021/6/22
- @desc:
 */
data class AddressModel(
    var address: String ,
    var contactName: String,
    var default: Boolean,
    var id: Int,
    var phone: String,
    var postalCode: String,
    var userId: Int,
)