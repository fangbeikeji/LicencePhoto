package com.fbkj.licencephoto.ui

import android.app.Activity
import android.graphics.Color
import android.util.Log
import com.fbkj.licencephoto.R
import com.fbkj.licencephoto.config.Constants
import com.fbkj.licencephoto.config.Event
import com.fbkj.licencephoto.model.EventModel
import com.fbkj.licencephoto.model.UserModel
import com.fbkj.licencephoto.network.NetworkCall
import com.fbkj.licencephoto.network.RetrofitHelper.api
import com.fbkj.licencephoto.utils.ContextHolder
import com.fbkj.licencephoto.utils.toast
import com.github.florent37.application.provider.ActivityDestroyedListener
import com.github.florent37.application.provider.ActivityProvider
import com.mobile.auth.gatewayauth.LoginAuthActivity
import com.umeng.umverify.UMResultCode
import com.umeng.umverify.UMVerifyHelper
import com.umeng.umverify.listener.UMPreLoginResultListener
import com.umeng.umverify.listener.UMTokenResultListener
import com.umeng.umverify.model.UMTokenRet
import com.umeng.umverify.view.UMAuthUIConfig
import kotlinx.coroutines.*
import org.greenrobot.eventbus.EventBus

/**
- @author:  LZC
- @time:  2021/5/13
- @desc:
 */

class UserLoginManager private constructor(): NetworkCall {

    private lateinit var umVerifyHelper: UMVerifyHelper
    private var isLoginPressed = false // 是否是用户按下登录按钮
    private var isVerifyAvailable = true // 终端网络环境是否支持一键登录或者号码认证

    init {
        initUM()
        ActivityProvider.addDestroyedListener(object : ActivityDestroyedListener {
            override fun onActivityDestroyed(activity: Activity) {
                // 当一键登录页面销毁，移除加入的自定义控件
                if (activity::class.java == LoginAuthActivity::class.java) {
                    umVerifyHelper.removeAuthRegisterXmlConfig()
                }
            }
        })
    }

    /**
     * 初始化友盟只能验证，文档：https://developer.umeng.com/docs/143070/detail/144780
     */
    private fun initUM() {
        // 1.初始化获取token实例
        val checkListener = object : UMTokenResultListener {
            override fun onTokenSuccess(s: String) {
                val tokenRet: UMTokenRet
                try {
                    tokenRet = UMTokenRet.fromJson(s)
                    if (UMResultCode.CODE_START_AUTHPAGE_SUCCESS == tokenRet.code) {
                        Log.i("LoginMsg", "唤起授权页成功：$s")
                    }
                    if (UMResultCode.CODE_GET_TOKEN_SUCCESS == tokenRet.code) {
                        Log.i("LoginMsg", "获取token成功：$s")
                        GlobalScope.launch { loginUm(tokenRet.token) }
                    }
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
//                    e.message?.let { this.onTokenFailed(it) }
                }

            }

            override fun onTokenFailed(s: String) {
                Log.e("LoginMsg", "获取token失败：$s")
                //如果环境检查失败 使用其他登录方式
                val tokenRet = UMTokenRet.fromJson(s)
                if (tokenRet.code == UMResultCode.CODE_ERROR_USER_CANCEL){
                    return
                }
                isVerifyAvailable = false
                if (isLoginPressed) {
                    ActivityProvider.currentActivity?.let {
                        it.runOnUiThread {
                            it.toast("需要打开蜂窝网络进行登录验证")
                        }
                    }
                    LoginActivity.start(ActivityProvider.currentActivity!!)
//                    umVerifyHelper.removeAuthRegisterViewConfig()
                    isLoginPressed=false
                }
            }
        }

        // 2.初始化SDK实例
        umVerifyHelper = UMVerifyHelper.getInstance(ContextHolder.context, checkListener)
        // 3.设置SDK秘钥
        umVerifyHelper.setAuthSDKInfo(Constants.UM_SDK_SECRET)
        // 4.检测终端网络环境是否支持一键登录或者号码认证
        umVerifyHelper.checkEnvAvailable(UMVerifyHelper.SERVICE_TYPE_LOGIN)
        // 设置UI,setAuthUIConfig
        setUMAuthUIConfig()
    }

    /**
     * 设置 UI
     */
    private fun setUMAuthUIConfig() {
        umVerifyHelper.setAuthUIConfig(
            UMAuthUIConfig.Builder()
                .setSwitchAccHidden(true)
                .setNavColor(Color.BLACK)
                .setStatusBarColor(Color.BLACK)
                .setLogoImgPath(R.mipmap.ic_launcher_round2.toString())
                .setAppPrivacyTwo(
                    ContextHolder.context.getString(R.string.service_agreement),
                    Constants.USER_AGREEMENT
                )
                .setAppPrivacyThree(
                    ContextHolder.context.getString(R.string.privacy_policy),
                    Constants.USER_PRIVACY
                )
                .create()
        )

        umVerifyHelper.setUIClickListener { s, context, _ ->
            when (s) {
                UMResultCode.CODE_ERROR_USER_CANCEL -> context.toast(UMResultCode.MSG_ERROR_USER_CANCEL)
                UMResultCode.CODE_ERROR_USER_SWITCH -> {
                    umVerifyHelper.removeAuthRegisterViewConfig()
                }
            }
        }
    }

    /**
     * 在不是一进app就需要登录的场景 建议调用此接口 加速拉起一键登录页面
     * 等到用户点击登录的时候 授权页可以秒拉
     * 预取号的成功与否不影响一键登录功能，所以不需要等待预取号的返回。
     * @param overdueTime
     */
    fun accelerateLoginPage(overdueTime: Int) {
        umVerifyHelper.accelerateLoginPage(overdueTime, object : UMPreLoginResultListener {
            override fun onTokenSuccess(s: String) {
                Log.i("LoginMsg", "预取号成功: $s")
            }

            override fun onTokenFailed(s: String, s1: String) {
                Log.i("LoginMsg", "预取号失败：, $s1")
            }
        })
    }

    fun login() {
        isLoginPressed = true
        umVerifyHelper.getLoginToken(ContextHolder.context, 20000)
//        MobclickAgent.onEvent(ContextHolder.context,"open_login_page")
    }

    private suspend fun loginUm(p0: String) {
        val result = apiCall { api.quickLogin(p0) }
        if (result.isSuccess) {
            quitUMLoginPage()
            loginSuccess(result.getOrNull())
        } else {
            withContext(Dispatchers.Main) {
                ContextHolder.context.toast("登录失败，${result.exceptionOrNull()?.message}")
            }
        }
    }

    fun loginSuccess(userModel: UserModel?) {
        //保存数据到本地
        ActivityProvider.currentActivity?.let {
            it.runOnUiThread {
                it.toast(R.string.login_suc_ok)
            }
        }

        Log.i("LoginMsg", userModel!!.nickname+userModel.id)
//        UserSignInHandler.getInstance().signIn(userModel)

        EventBus.getDefault().post(EventModel(1, Event.ON_USER_SIGN_STATE_CHANGED))

        quitUMLoginPage()
    }

    private fun quitUMLoginPage() {
        umVerifyHelper.quitLoginPage()
    }

    companion object {

        @Volatile
        private var INSTANCE: UserLoginManager? = null
        fun getInstance() = INSTANCE ?: synchronized(UserLoginManager::class.java) {
            INSTANCE ?: UserLoginManager().also { INSTANCE = it }
        }
    }
}