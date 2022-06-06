package com.fbkj.licencephoto.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Message
import android.text.TextUtils
import com.alipay.sdk.app.PayTask
import com.fbkj.licencephoto.config.Event
import com.fbkj.licencephoto.local.LastClickRecord
import com.fbkj.licencephoto.model.PayResult
import com.fbkj.licencephoto.wxapi.WXPayEntryActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus

/**
- @author:  LZC
- @time:  2021/7/12
- @desc:
 */

open class ZfbCallBack(context: Context) {
    private val handler: Handler = @SuppressLint("HandlerLeak")

    object : Handler() {
        override fun handleMessage(msg: Message) {
            val payResult = PayResult(msg.obj as Map<String?, String?>)

            /**
             * 对于支付结果，请商户依赖服务端的异步通知结果。
             * 同步通知结果，仅作为支付结束的通知。
             * 这边同样进入微信自定义支付页面
             */
            //支付宝需要处理取消支付后重新回调支付页面
            EventBus.getDefault().post(Event.ON_ZFB_REORDER)

            val resultInfo: String = payResult.result // 同步返回需要验证的信息

            val resultStatus: String = payResult.resultStatus
            // 判断resultStatus 为9000则代表支付成功
            if (TextUtils.equals(resultStatus, "9000")) {
                //在订单信息里边支付的
                LastClickRecord.getInstance().isOrder = true
                // 该笔订单是否真实支付成功，需要依赖服务端的异步通知。
                WXPayEntryActivity.start(context, isZfb = true, isZfbSuccess = true)
                context.toast("支付成功")
            } else {
                // 该笔订单真实的支付结果，需要依赖服务端的异步通知。
                WXPayEntryActivity.start(context, isZfb = true, isZfbSuccess = false)
                context.toast("支付失败")
            }
        }
    }

    suspend fun pay(activity: Activity,params: String){
        withContext(Dispatchers.IO) {
            val alipay = PayTask(activity)
            //传入true表示用户在商户app内部点击付款，是否需要一个 loading 做为在钱包唤起之前的过渡，
            // 这个值设置为 true，将会在调用 pay 接口的时候直接唤起一个 loading，
            // 直到唤起H5支付页面或者唤起外部的钱包付款页面 loading 才消失
            val result = alipay.payV2(params, true)
            val msg = Message()
            msg.what = 1
            msg.obj = result
            handler.sendMessage(msg)
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: ZfbCallBack? = null
        fun getInstance() = INSTANCE ?: synchronized(ZfbCallBack::class.java) {
            INSTANCE ?: ZfbCallBack(ContextHolder.context).also { INSTANCE = it }
        }
    }
}