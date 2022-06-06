package com.fbkj.licencephoto.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
- @author:  LZC
- @time:  2021/7/5
- @desc:
 */

@Parcelize
data class NewOrder(
    var address: Address?,
    var addressId: Int,
    var amount: Int,
    var content: String,
    var createAt: String,
    var expressCompany: String,
    var expressNo: String,
    var id: Int,
    var multibgs: Boolean,
    var outTradeNo: String,
    var payPlatform: Int,
    var photo: String,
    var photoBackground: String,
    var photoPixel: String,
    var photoRembg: String,
    var photoSize: String,
    var photoSpecification: String,
    var status: Int,
    var successAt: String,
    var type: Int,//订单类型1为电子2为冲印
    var userId: Int
    ): Parcelable


@Parcelize
data class Address(
    var address: String?,
    var contactName: String?,
    var default: Boolean?,
    var id: Int?,
    var phone: String?,
    var postalCode: String?,
    var userId: Int?
): Parcelable