package com.fbkj.licencephoto.ui.complain

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.fbkj.licencephoto.base.BaseViewModel
import com.fbkj.licencephoto.network.RetrofitHelper
import kotlinx.coroutines.launch

/**
- @author:  LZC
- @time:  2021/9/15
- @desc:
 */

class ComplainViewModel: BaseViewModel() {
    var onResult=MutableLiveData<Boolean>()
    fun sendComplain(orderId: Int,type: Int,username: String,contact: String,description: String){
        viewModelScope.launch {
            val result = apiCall {
                RetrofitHelper.api.paybackOrFeedback(type,orderId,username,contact,description)
            }
            if(result.isSuccess){
                onResult.postValue(true)
            }else{
                onResult.postValue(false)
            }
        }
    }
}