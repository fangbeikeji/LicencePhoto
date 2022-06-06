package com.fbkj.licencephoto.model

/**
- @author:  LZC
- @time:  2021/7/2
- @desc:
 */
data class WxOrderCallBack(
    var params: Params,
    var trade_no: String,
    var order: Order
)

data class Order(
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
)


data class Params(
    var appid: String,
    var noncestr: String,
    var `package`: String,
    var partnerid: String,
    var paySign: String,
    var prepayid: String,
    var timestamp: String
)