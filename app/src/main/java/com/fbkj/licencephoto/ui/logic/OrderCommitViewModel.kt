package com.fbkj.licencephoto.ui.logic

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.fbkj.licencephoto.base.BaseViewModel
import com.fbkj.licencephoto.local.LastClickRecord
import com.fbkj.licencephoto.model.*
import com.fbkj.licencephoto.network.RetrofitHelper
import com.fbkj.licencephoto.utils.ContextHolder.Companion.context
import com.fbkj.licencephoto.utils.toPlainTextBody
import com.fbkj.licencephoto.utils.toast
import com.umeng.analytics.MobclickAgent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okio.Okio
import java.io.File

/**
- @author:  LZC
- @time:  2021/7/1
- @desc:
 */

class OrderCommitViewModel : BaseViewModel() {

    val netBitmap = MutableLiveData<Bitmap>()
    val flushPrice = MutableLiveData<PriceModel>()
    val elePrice = MutableLiveData<List<PriceModel>>()

    var wxReCallBackData = MutableLiveData<WxOrderReCall>()
    var zfbReCallBackData = MutableLiveData<AliOrderReCall>()

    val allPaidOrderCallBackData = MutableLiveData<AllPayResult>()
    val wxOrderCallBackSuccessData = MutableLiveData<WxOrderCallBack>()
    val aliOrderCallBackSuccessData = MutableLiveData<AliOrderParams>()

    fun netPic2Bitmap(url: String) {
        val file = File(context.externalCacheDir, "${System.currentTimeMillis()}")
        viewModelScope.launch(Dispatchers.IO) {
            val result = RetrofitHelper.api.downloadBitmap(url)
            result.byteStream()
            Okio.buffer(Okio.sink(file))?.use { it.writeAll(result.source()) }
            netBitmap.postValue(BitmapFactory.decodeFile(file.path))
        }
    }

    fun userOrderCommit(
        multiBgs: Int, type: Int, isChangeClothes: Int, printNumbers:Int, photoSize: String,
        content: String, specification: String, pixel: String, background: String,
        addressId: Int, bitmap: Bitmap, remBgBitmap: Bitmap, payType: Int, couponId: Int) {
        val file = File(
            context.externalCacheDir,
            "withBg-${System.currentTimeMillis()}"
        )

        val file2 = File(
            context.externalCacheDir,
            "removeBg-${System.currentTimeMillis()}"
        )

//        LastClickRecord.getInstance().finalBitmapWithBg = bitmap
//        LastClickRecord.getInstance().finalBitmapWithoutBg = remBgBitmap

        val ops = file.outputStream()
        val ops2 = file2.outputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, ops)
        remBgBitmap.compress(Bitmap.CompressFormat.PNG, 100, ops2)


        ops.flush()
        ops2.flush()

        ops.close()
        ops2.close()

        val withBgImage = MultipartBody.Part.createFormData(
            "photo",
            file.name,
            RequestBody.create(MediaType.parse("image/*"), file)
        )

        val removeBgImage = MultipartBody.Part.createFormData(
            "photo_rembg",
            file2.name,
            RequestBody.create(MediaType.parse("image/*"), file2)
        )



        if (payType==0){
            viewModelScope.launch {
                val result = apiCall {
                    RetrofitHelper.api.fetchOrderForWxPay(
                        multiBgs,
                        type,
                        isChangeClothes,
                        photoSize.toPlainTextBody(),
                        content.toPlainTextBody(),
                        specification.toPlainTextBody(),
                        pixel.toPlainTextBody(),
                        background.toPlainTextBody(),
                        addressId,
                        printNumbers,
                        couponId,
                        withBgImage,
                        removeBgImage
                    )
                }
                if (result.isSuccess) {
                    wxOrderCallBackSuccessData.postValue(result.getOrNull())
                    LastClickRecord.getInstance().apply {
                        isOrder = false
                        flushNum = printNumbers
                        isBeauty = isChangeClothes
                        this.finalBitmapWithBg = bitmap
                        this.finalBitmapWithoutBg = remBgBitmap
                        order = result.getOrNull()!!.order
                        tradNo = result.getOrNull()!!.trade_no
                        onDiscountOrder = couponId==1 //1用0不用(优惠卷)
                    }
                } else{
                    context.toast("获取微信订单失败，请重试")
                }
            }
        }else{
            viewModelScope.launch {
                val result = apiCall {
                    RetrofitHelper.api.fetchOrderForAliPay(
                        multiBgs,
                        type,
                        isChangeClothes,
                        photoSize.toPlainTextBody(),
                        content.toPlainTextBody(),
                        specification.toPlainTextBody(),
                        pixel.toPlainTextBody(),
                        background.toPlainTextBody(),
                        addressId,
                        printNumbers,
                        couponId,
                        withBgImage,
                        removeBgImage
                    )
                }
                if (result.isSuccess) {
                    aliOrderCallBackSuccessData.postValue(result.getOrNull())
                    LastClickRecord.getInstance().apply {
                        isOrder = false
                        flushNum = printNumbers
                        isBeauty = isChangeClothes
                        this.finalBitmapWithBg = bitmap
                        this.finalBitmapWithoutBg = remBgBitmap
                        order = result.getOrNull()!!.order
                        tradNo = result.getOrNull()!!.trade_no
                        onDiscountOrder = couponId==1 //1用0不用(优惠卷)
                    }
                }  else{
                    context.toast("获取支付宝订单失败，请重试")
                }
            }
        }
        if (couponId==1){
            MobclickAgent.onEvent(context, "on_discount_pay")
        }
        MobclickAgent.onEvent(context, "order_created")
    }

    fun orderWxQuery(wxTradNo: String) {
        viewModelScope.launch {
            val result = apiCall { RetrofitHelper.api.wxOrderQuery(wxTradNo) }
            if (result.isSuccess) {
                allPaidOrderCallBackData.postValue(result.getOrNull())
                Log.i("success_val", RetrofitHelper.gson.toJson(result))
            } else {
                Log.i("error_val", result.getOrNull().toString())
            }
        }
    }

    fun orderZfbQuery(zfbTradNo: String) {
        viewModelScope.launch {
            val result = apiCall { RetrofitHelper.api.aliOrderQuery(zfbTradNo) }
            if (result.isSuccess) {
                allPaidOrderCallBackData.postValue(result.getOrNull())
                Log.i("success_zfb_val", RetrofitHelper.gson.toJson(result))
            } else {
                Log.i("error_zfb_val", RetrofitHelper.gson.toJson(result))
            }
        }
    }

    //获取电子版价格
    fun getElectronicPrice(){
        viewModelScope.launch {
            val result = apiCall { RetrofitHelper.api.getElePrice(1) }
            if (result.isSuccess) {
                elePrice.postValue(result.getOrNull())
            }
        }
    }
    //获取冲印版价格
    fun getFlushPrice(){
        viewModelScope.launch {
            val result = apiCall { RetrofitHelper.api.getFlushPrice(2) }
            if (result.isSuccess) {
                flushPrice.postValue(result.getOrNull())
            }
        }
    }

    //微信重新支付
    fun wxRePay(orderId: Int) {
        viewModelScope.launch {
            val result = apiCall { RetrofitHelper.api.wxRePay(orderId) }
            if (result.isSuccess) {
                if (result.getOrNull() != null) {
                    wxReCallBackData.postValue(result.getOrNull())
                } else {

                    Log.i("error_id", "订单id错误")
                }

            } else {
                context.toast("订单超过20分钟，请重新下单")
            }
        }
    }
    //支付宝重新支付
    fun zfbRePay(orderId: Int) {
        viewModelScope.launch {
            val result = apiCall { RetrofitHelper.api.aliRePay(orderId) }
            if (result.isSuccess) {
                if (result.getOrNull() != null) {
                    zfbReCallBackData.postValue(result.getOrNull())
//                        Log.i("asnfnloaf",RetrofitHelper.gson.toJson(result.getOrNull()))
                } else {
                    Log.i("error_id", "订单id错误")
                }

            } else {
                context.toast("订单超过20分钟，请重新下单")
            }
        }
    }
}