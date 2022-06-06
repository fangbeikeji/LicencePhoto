package com.fbkj.licencephoto.ui.address

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.fbkj.licencephoto.base.BaseViewModel
import com.fbkj.licencephoto.network.RetrofitHelper
import kotlinx.coroutines.launch

/**
- @author:  LZC
- @time:  2021/6/24
- @desc:
 */
class AddAddressModel : BaseViewModel() {
    val addCondition = MutableLiveData<Boolean>()

    fun addAddress(
        postalCode: String,
        contactName: String,
        phone: String,
        address: String,
        isDefault: Boolean
    ) {
        viewModelScope.launch {
            val result = apiCall {
                RetrofitHelper.api.addAddress(
                    postalCode,
                    contactName,
                    phone, address,
                    isDefault
                )
            }
            if (result.isSuccess) {
                addCondition.postValue(true)
//                Log.i("success_add", result.getOrNull().toString())
            } else {
                addCondition.postValue(false)
                Log.i("fail", result.getOrNull().toString())
            }
        }
    }

    fun updateAddress(
        id: String,
        postalCode: String,
        contactName: String,
        phone: String,
        address: String,
        isDefault: Boolean
    ) {
        viewModelScope.launch {
            val result = apiCall {
                RetrofitHelper.api.updateAddress(
                    id,
                    postalCode,
                    contactName,
                    phone,
                    address,
                    isDefault
                )
            }
            if (result.isSuccess) {
                Log.i("success", result.getOrNull().toString())
            } else {
                Log.i("fail", result.getOrNull().toString())
            }
        }
    }
}