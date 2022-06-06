package com.fbkj.licencephoto.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.fbkj.licencephoto.base.BaseViewModel
import com.fbkj.licencephoto.config.Event
import com.fbkj.licencephoto.local.SignInHandler
import com.fbkj.licencephoto.model.EventModel
import com.fbkj.licencephoto.model.UpdateModel
import com.fbkj.licencephoto.network.RetrofitHelper
import com.fbkj.licencephoto.network.RetrofitHelper.api
import com.fbkj.licencephoto.local.SavingUserMsg
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus

/**
- @author:  LZC
- @time:  2021/6/29
- @desc:
 */

class MainViewModel : BaseViewModel() {

    private val _updateBean = MutableLiveData<UpdateModel>()

    val updateModel: LiveData<UpdateModel> = _updateBean

    fun checkUpdate(pkg: String, code: Int) {
        viewModelScope.launch {
            val result = apiCall { api.checkUpdate(pkg, code) }
            if (result.isSuccess) {
                if (result.getOrNull() != null) {
                    //需要更新
                    _updateBean.postValue(result.getOrNull())
                }
            }
        }
    }

    fun snapLogin() {
        viewModelScope.launch(Dispatchers.IO) {
            val result = apiCall { api.snapLogin() }
            if (result.isSuccess) {
                SignInHandler.getInstance().signIn(result.getOrNull()!!)
                EventBus.getDefault().post(EventModel(0, Event.ON_USER_SIGN_STATE_CHANGED))

                SavingUserMsg.getInstance().saveUserMsg2SD(
                    RetrofitHelper.gson.toJson(result.getOrNull())
                )
            }
        }
    }
}