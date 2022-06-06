package com.fbkj.licencephoto.network

import com.fbkj.licencephoto.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.*

/**
- @author:  LZC
- @time:  2021/6/2
- @desc:
 */

interface ServerApi {
    @FormUrlEncoded
    @POST("/login.quick")
    suspend fun quickLogin(@Field("um_token") umToken: String): NetResponse<UserModel>

    @GET("/user/address/list")
    suspend fun addressList(
        @Query("size") size: Int,
        @Query("page") page: Int
    ): NetResponse<List<AddressModel>>

    @FormUrlEncoded
    @POST("/user/address")
    suspend fun addAddress(
        @Field("postal_code") postalCode: String,
        @Field("contact_name") contactName: String,
        @Field("phone") phone: String,
        @Field("address") address: String,
        @Field("default") isDefault: Boolean,
    ): NetResponse<Any>

    @FormUrlEncoded
    @PATCH("/user/address")
    suspend fun updateAddress(
        @Field("address_id") addressId: String,
        @Field("postal_code") postalCode: String,
        @Field("contact_name") contactName: String,
        @Field("phone") phone: String,
        @Field("address") address: String,
        @Field("default") isDefault: Boolean,
    ): NetResponse<Any>

    @DELETE("/user/address")
    suspend fun deleteAddress(@Query("address_id") addressId: Int): NetResponse<Any>

    /**
     * @param status 支付状态 0 1 -1
     * @param type 类型 电子，冲印
     * */
    @GET("/user/orders")
    suspend fun getOrderList(
        @Query("status") status: Int?,
        @Query("type") type: Int?,
        @Query("size") size: Int,
        @Query("page") page: Int
    ): NetResponse<List<NewOrder>>

    /**
     * 网络图片下载
     * */
    @Streaming
    @GET
    suspend fun downloadBitmap(@Url url: String) : ResponseBody

    @DELETE("/user/order")
    suspend fun deleteOrder(@Query("order_id") orderId: Int): NetResponse<Any>

    /**
     * @param type 下单类型，1是电子证件，2是打印
     * @param content 订单内容，可对尺寸什么的做补充
     * @param addressId 地址id， 当type是2的时候，需要传，因为需要快递
     * @param isChangeClothes 换装， 1换 0不换
     * @param couponId 优惠卷， 1有 0没
     */
    @Multipart
    @POST("/pay/wechat")
    suspend fun fetchOrderForWxPay(
        @Part("multibgs") multiBgs: Int,
        @Part("type") type: Int,
        @Part("change_clothes") isChangeClothes:Int,
        @Part("photo_size") photoSize: RequestBody,
        @Part("content") content: RequestBody,
        @Part("photo_specification") specification: RequestBody,
        @Part("photo_pixel") pixel: RequestBody,
        @Part("photo_background") background: RequestBody,
        @Part("address_id") addressId: Int?,
        @Part("print_numbers") printNumbers: Int?,
        @Part("coupon_id") couponId: Int,
        @Part file: MultipartBody.Part,
        @Part photo_rembg: MultipartBody.Part
    ): NetResponse<WxOrderCallBack>


    /**
     * @param type 下单类型，1是电子证件，2是打印
     * @param content 订单内容，可对尺寸什么的做补充
     * @param addressId 地址id， 当type是2的时候，需要传，因为需要快递
     * @param isChangeClothes 换装， 1换 0不换
     */
    @Multipart
    @POST("/pay/alipay")
    suspend fun fetchOrderForAliPay(
        @Part("multibgs") multiBgs: Int,
        @Part("type") type: Int,
        @Part("change_clothes") isChangeClothes:Int,
        @Part("photo_size") photoSize: RequestBody,
        @Part("content") content: RequestBody,
        @Part("photo_specification") specification: RequestBody,
        @Part("photo_pixel") pixel: RequestBody,
        @Part("photo_background") background: RequestBody,
        @Part("address_id") addressId: Int?,
        @Part("print_numbers") printNumbers: Int?,
        @Part("coupon_id") couponId: Int,
        @Part file: MultipartBody.Part,
        @Part photo_rembg: MultipartBody.Part
    ): NetResponse<AliOrderParams>

    @GET("/pay/wechat.query")
    suspend fun wxOrderQuery(@Query("trade_no") tradeNo: String): NetResponse<AllPayResult>

    @GET("/pay/alipay.query")
    suspend fun aliOrderQuery(@Query("trade_no") tradeNo: String): NetResponse<AllPayResult>

    /**
     * 用大佬的api抠图
     * */
    @Multipart
    @POST("http://rembg.fbkjapp.com")
    suspend fun rembg(@Part file: MultipartBody.Part): ResponseBody

    /**
     * @param pkg 包名
     * @param contact 联系方式
     * @param content 内容
     */
    @FormUrlEncoded
    @POST("http://public.fbkjapp.com/feedback")
    suspend fun recommend(
        @Field("pkg") pkg: String,
        @Field("contact") contact: String,
        @Field("content") content: String
    ): NetResponse<Any>

    @GET("http://public.fbkjapp.com/android/update")
    suspend fun checkUpdate(
        @Query("pkg") packageName: String,
        @Query("version_code") versionCode: Int,
    ): NetResponse<UpdateModel>

    @GET("/auth/login.sms")
    suspend fun fetchSms(
        @Header("sign") sign: String,
        @Query("phone") phone: String
    ): NetResponse<String>

    @GET("/pay/wechat.repay")
    suspend fun wxRePay(@Query("order_id") orderId: Int): NetResponse<WxOrderReCall>

    @GET("/pay/alipay.repay")
    suspend fun aliRePay(@Query("order_id") orderId: Int): NetResponse<AliOrderReCall>

    @GET("/setting/price")
    suspend fun getFlushPrice(@Query("type") type: Int): NetResponse<PriceModel>

    @GET("/setting/price.1")
    suspend fun getElePrice(@Query("type") type: Int): NetResponse<List<PriceModel>>

    @FormUrlEncoded
    @POST("/auth/login.code")
    suspend fun smsLogin(
        @Field("phone") phone: String,
        @Field("code") smsCode: String
    ): NetResponse<UserModel>

    @POST("/login.snap")
    suspend fun snapLogin(): NetResponse<SnapLoginModel>

    /**
     * 订单退款申请或反馈
     * */
    @FormUrlEncoded
    @POST("/order/feedback")
    suspend fun paybackOrFeedback(
        @Field("type") type: Int,//1是退款申请，2是反馈
        @Field("order_id") order_id: Int,//订单ID
        @Field("username") username: String,//联系人
        @Field("contact") contact: String,//联系方式，没有限制方式
        @Field("description") description: String//退款原因/反馈内容
    ): NetResponse<Any>
}