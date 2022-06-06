package com.fbkj.licencephoto.orderfragment

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.fbkj.licencephoto.base.BaseViewModel
import com.fbkj.licencephoto.local.LastClickRecord
import com.fbkj.licencephoto.model.*
import com.fbkj.licencephoto.network.RetrofitHelper
import com.fbkj.licencephoto.network.RetrofitHelper.api
import com.fbkj.licencephoto.utils.ContextHolder.Companion.context
import com.fbkj.licencephoto.utils.toast
import kotlinx.coroutines.launch

class EleOrderViewModel : BaseViewModel() {

    private val TAG = "ElectronVersion"

    var orderList = MutableLiveData<List<NewOrder>>()
    var wxReCallBackData = MutableLiveData<WxOrderReCall>()
    var zfbReCallBackData = MutableLiveData<AliOrderReCall>()

    var allPage = 0
    var elePage = 0

    fun deleteOrder(orderId: Int) {
        viewModelScope.launch {
            val result = apiCall { api.deleteOrder(orderId) }
            if (result.isSuccess) {
                Log.i(TAG, RetrofitHelper.gson.toJson(result.getOrNull()))
            } else {
                Log.i(TAG, "false")
            }
        }
    }

    fun fetchOrder(isDelete: Boolean) {
        viewModelScope.launch {
            if (isDelete) {
                //删除数据后,刷新一遍
                elePage = 1
            } else {
                //不是更新，加载更多
                elePage++
            }
            val result = apiCall { api.getOrderList(null,1, 10, elePage) }
            if (result.isSuccess) {
                val data = result.getOrNull()
                if (data != null) {
                    orderList.postValue(data)
                }
            }
            if (result.isFailure) {
                elePage--
                return@launch
            }
        }
    }

    fun fetchAllFinishedOrder(isDelete: Boolean) {
        viewModelScope.launch {
            if (isDelete) {
                //删除数据后,刷新一遍
                allPage = 1
            } else {
                //不是更新，加载更多
                allPage++
            }

            val result = apiCall { api.getOrderList(1,null, 10, allPage) }
            if (result.isSuccess) {
                val data = result.getOrNull()
                if (data != null) {
                    orderList.postValue(data)
                }
            }
            if (result.isFailure) {
                allPage--
                return@launch
            }
        }
    }

    fun wxRePay(orderId: Int) {
        viewModelScope.launch {
            val result = apiCall { api.wxRePay(orderId) }
            if (result.isSuccess) {
                if (result.getOrNull() != null) {
                    wxReCallBackData.postValue(result.getOrNull())
                    LastClickRecord.getInstance().tradNo = result.getOrNull()!!.trade_no

//                        Log.i("asnfnloaf",RetrofitHelper.gson.toJson(result.getOrNull()))
                } else {

                    Log.i("error_id", "订单id错误")
                }

            } else {
                Log.i("error_id", RetrofitHelper.gson.toJson(result.getOrNull()))
                context.toast("订单超过20分钟，请重新下单")
            }
        }
    }

    fun zfbRePay(orderId: Int) {
        viewModelScope.launch {
            val result = apiCall { api.aliRePay(orderId) }
            if (result.isSuccess) {
                if (result.getOrNull() != null) {
                    zfbReCallBackData.postValue(result.getOrNull())
                    LastClickRecord.getInstance().tradNo = result.getOrNull()!!.trade_no
                } else {
                    Log.i("error_id", "订单id错误")
                }
            } else {
//                Log.i("error_id", RetrofitHelper.gson.toJson(result.getOrNull()))
                context.toast("订单超过20分钟，请重新下单")
            }
        }
    }


}


