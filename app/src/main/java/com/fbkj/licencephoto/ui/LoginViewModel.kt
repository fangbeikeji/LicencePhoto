package com.fbkj.licencephoto.ui

import androidx.lifecycle.viewModelScope
import com.fbkj.licencephoto.base.BaseViewModel
import com.fbkj.licencephoto.network.RetrofitHelper.api
import com.fbkj.licencephoto.utils.ContextHolder
import com.fbkj.licencephoto.utils.toast
import com.umeng.analytics.MobclickAgent
import kotlinx.coroutines.launch

/**
- @author:  LZC
- @time:  2021/6/25
- @desc:
 */

class LoginViewModel: BaseViewModel() {

    fun getSmsCode(sign: String, phone: String) {
        viewModelScope.launch {
            val result = apiCall { api.fetchSms(sign, phone) }
            result.getOrNull()?.let {
                ContextHolder.context.toast(it)
            } ?: let {
                result.exceptionOrNull()?.let {
                    ContextHolder.context.toast("${it.message}")
                }
            }
        }
    }

    fun smsLogin(phone: String, code: String) {
        viewModelScope.launch {
            val result = apiCall { api.smsLogin(phone, code) }
            result.getOrNull()?.let {
                UserLoginManager.getInstance().loginSuccess(it)
                MobclickAgent.onEvent(ContextHolder.context,"sms_login")
            } ?: let {
                result.exceptionOrNull()?.let {
                    ContextHolder.context.toast("${it.message}")
                }
            }
        }
    }


}