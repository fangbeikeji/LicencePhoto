package com.fbkj.licencephoto.orderfragment

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.fbkj.licencephoto.base.BaseViewModel
import com.fbkj.licencephoto.config.Event
import com.fbkj.licencephoto.local.LastClickRecord
import com.fbkj.licencephoto.local.SignInHandler
import com.fbkj.licencephoto.model.*
import com.fbkj.licencephoto.network.RetrofitHelper
import com.fbkj.licencephoto.utils.ContextHolder.Companion.context
import com.fbkj.licencephoto.local.SavingUserMsg
import com.fbkj.licencephoto.utils.toast
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus

/**
- @author:  LZC
- @time:  2021/7/7
- @desc:
 */
class FlushOrderViewModel : BaseViewModel() {
    var flushOrderList = MutableLiveData<List<NewOrder>>()

    var wxReCallBackData = MutableLiveData<WxOrderReCall>()
    var zfbReCallBackData = MutableLiveData<AliOrderReCall>()

    var flushPage = 0
    fun fetchOrder(isDelete: Boolean) {
        if (isDelete) {
            //删除数据后,刷新一遍
            flushPage = 1
        } else {
            //不是更新，加载更多
            flushPage++
        }
        viewModelScope.launch {
            val result = apiCall { RetrofitHelper.api.getOrderList(null,2, 10, flushPage) }
            if (result.isSuccess) {
                val data = result.getOrNull()
                if (data != null) {
                    flushOrderList.postValue(data)
                }
            }
            if (result.isFailure) {
                flushPage--
                return@launch
            }
        }
    }

    fun deleteOrder(orderId: Int) {
        viewModelScope.launch {
            val result = apiCall { RetrofitHelper.api.deleteOrder(orderId) }
            if (result.isSuccess) {
                Log.i("on_delete", RetrofitHelper.gson.toJson(result.getOrNull()))
            } else {
                Log.i("on_delete", "false")
            }
        }
    }

    fun wxRePay(orderId: Int) {
        viewModelScope.launch {
            val result = apiCall { RetrofitHelper.api.wxRePay(orderId) }
            if (result.isSuccess) {
                if (result.getOrNull() != null) {
                    LastClickRecord.getInstance().tradNo = result.getOrNull()!!.trade_no
                    wxReCallBackData.postValue(result.getOrNull())
//                        Log.i("asnfnloaf",RetrofitHelper.gson.toJson(result.getOrNull()))
                } else {
                    Log.i("error_id", "订单id错误")
                }

            } else {
                context.toast("订单超过20分钟，请重新下单")
            }
        }
    }

    fun zfbRePay(orderId: Int) {
        viewModelScope.launch {
            val result = apiCall { RetrofitHelper.api.aliRePay(orderId) }
            if (result.isSuccess) {
                if (result.getOrNull() != null) {
                    LastClickRecord.getInstance().tradNo = result.getOrNull()!!.trade_no
                    zfbReCallBackData.postValue(result.getOrNull())
                } else {
                    Log.i("error_id", "订单id错误")
                }
            } else {
                context.toast("订单超过20分钟，请重新下单")
            }
        }
    }


}