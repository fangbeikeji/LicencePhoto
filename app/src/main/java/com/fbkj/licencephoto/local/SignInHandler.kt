package com.fbkj.licencephoto.local

import android.annotation.SuppressLint
import com.fbkj.licencephoto.model.SnapLoginModel
import com.fbkj.licencephoto.network.RetrofitHelper
import com.fbkj.licencephoto.utils.ContextHolder
import com.tencent.mmkv.MMKV
import com.umeng.analytics.MobclickAgent

interface SignIn {
    fun signIn(user: SnapLoginModel)
    fun isSnapSigned(): Boolean
}

class SignInHandler : SignIn {

    var snapUser: SnapLoginModel? = null
        set(value) {
            field = value
            val v = if (value == null) null else {
                RetrofitHelper.gson.toJson(value)
            }
            //mmkv
            MMKV.defaultMMKV().encode(PREF_SNAP_USER, v)
        }

    init {
        snapUser = RetrofitHelper.gson.fromJson(
            MMKV.defaultMMKV().decodeString(PREF_SNAP_USER), SnapLoginModel::class.java
        )
    }

    override fun signIn(user: SnapLoginModel) {
        this.snapUser = user
        MobclickAgent.onEvent(ContextHolder.context, "login_success")
    }

    override fun isSnapSigned(): Boolean {
        return snapUser != null
    }


    companion object {
        private const val PREF_SNAP_USER = "snap_pref_user"

        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var INSTANCE: SignInHandler? = null

        fun getInstance() =
            INSTANCE ?: synchronized(SignInHandler::class.java) {
                INSTANCE ?: SignInHandler()
                    .also { INSTANCE = it }
            }
    }
}