package com.fbkj.licencephoto.ui.address

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.fbkj.licencephoto.base.BaseViewModel
import com.fbkj.licencephoto.model.AddressModel
import com.fbkj.licencephoto.network.RetrofitHelper
import com.fbkj.licencephoto.network.RetrofitHelper.api
import kotlinx.coroutines.launch

/**
- @author:  LZC
- @time:  2021/6/24
- @desc:
 */

class AddressManagerModel : BaseViewModel() {

    var page = 0
    var addressList = MutableLiveData<List<AddressModel>>()

    fun getAddress() {
        page++
        viewModelScope.launch {
            val result = apiCall { api.addressList(10, page) }
            if (result.isSuccess) {
                Log.i("success_al", RetrofitHelper.gson.toJson(result))
                addressList.postValue(result.getOrNull())
            } else {
                page--
                Log.i("fail", result.getOrNull().toString())
            }
        }
    }

    fun deleteAddress(id:Int) {
        viewModelScope.launch {
            val result = apiCall { api.deleteAddress(id) }
            if (result.isSuccess) {
                Log.i("success", result.getOrNull().toString())
            } else {
                Log.i("fail", result.getOrNull().toString())
            }
        }
    }

}