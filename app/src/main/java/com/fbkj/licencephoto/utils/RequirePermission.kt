package com.fbkj.licencephoto.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleCoroutineScope
import com.fbkj.licencephoto.config.Event
import com.fbkj.licencephoto.local.SavingUserMsg
import com.fbkj.licencephoto.local.SignInHandler
import com.fbkj.licencephoto.model.SnapLoginModel
import com.fbkj.licencephoto.network.NetworkCall
import com.fbkj.licencephoto.network.RetrofitHelper
import com.fbkj.licencephoto.ui.CameraViewActivity
import com.google.gson.reflect.TypeToken
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.XXPermissions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus

/**
- @author:  LZC
- @time:  2021/7/14
- @desc:
 */
open class RequirePermission : NetworkCall {

    private var cList = arrayListOf<String>()
    private var pList = arrayListOf<String>()

    init {
        //android11适配,SD卡内存获取
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            cList.add(Manifest.permission.CAMERA)
            cList.add(Manifest.permission.MANAGE_EXTERNAL_STORAGE)
            pList.add(Manifest.permission.MANAGE_EXTERNAL_STORAGE)
        } else {
            cList.add(Manifest.permission.CAMERA)
            cList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            cList.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            pList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            pList.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }
    /**
     * 检查
     * 1.是否有相机硬件 2.用户同意的权限 ->创建个人信息
     */
    fun openCameraOptions(activity: FragmentActivity, lifecycleScope: LifecycleCoroutineScope) {
        if (activity.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)){
            XXPermissions.with(activity).permission(cList).request(object:OnPermissionCallback{
                override fun onGranted(permissions: MutableList<String>?, all: Boolean) {
                    if (all){
                        if (!SignInHandler.getInstance().isSnapSigned()) {
                            if (SavingUserMsg.getInstance().readUserMsgFromSD().isEmpty()) {
                                lifecycleScope.launch(Dispatchers.IO) { snapLogin() }
                            } else {
                                val result = RetrofitHelper.gson.fromJson<SnapLoginModel>(
                                    SavingUserMsg.getInstance().readUserMsgFromSD(),
                                    object : TypeToken<SnapLoginModel>() {}.type
                                )
                                SignInHandler.getInstance().signIn(result)
                                EventBus.getDefault().post(Event.ON_USER_SIGN_STATE_CHANGED)
                            }
                        } else {
                            //登录的话，判断sd卡是否被清除了数据，如果被清楚就再新建一个licencepic.txt存信息
                            SavingUserMsg.getInstance().saveUserMsg2SD(
                                RetrofitHelper.gson.toJson(SignInHandler.getInstance().snapUser)
                            )
                            CameraViewActivity.start(activity)
                        }
                    }

                }

                override fun onDenied(permissions: MutableList<String>?, never: Boolean) {
                    super.onDenied(permissions, never)
                    activity.toast("未打开授权选项")
                }
            })
        }else{
            activity.toast("未找到相机")
        }
    }

    /**
     * 点击我的订单那些查看个人订单信息的话是需要权限来访问SD卡的
     * 情况 1.新增用户 2.如果用户一开始没有开启所有权限 3.用户清除了所有缓存数据
     * 用户卸载了程序或者清了缓存又把 SD 卡信息清了的话就没办法了
     * */
    fun onPermissionFetch(activity: FragmentActivity, lifecycleScope: LifecycleCoroutineScope){
        XXPermissions.with(activity).permission(pList).request(object:OnPermissionCallback{
            override fun onGranted(permissions: MutableList<String>?, all: Boolean) {
                if (all){
                    if (!SignInHandler.getInstance().isSnapSigned()) {
                        if (SavingUserMsg.getInstance().readUserMsgFromSD().isEmpty()) {//返回""
                            lifecycleScope.launch(Dispatchers.IO) { snapLogin() }//创建新用户
                        } else {
                            val result = RetrofitHelper.gson.fromJson<SnapLoginModel>(
                                SavingUserMsg.getInstance().readUserMsgFromSD(),
                                object : TypeToken<SnapLoginModel>() {}.type
                            )
                            SignInHandler.getInstance().signIn(result)
                            EventBus.getDefault().post(Event.ON_USER_SIGN_STATE_CHANGED)
                        }
                    }else {
                        //登录的话，判断sd卡是否被清除了数据，如果被清楚就再新建一个licencepic.txt存信息
                        SavingUserMsg.getInstance().saveUserMsg2SD(
                            RetrofitHelper.gson.toJson(SignInHandler.getInstance().snapUser)
                        )
                    }
                }
            }

            override fun onDenied(permissions: MutableList<String>?, never: Boolean) {
                super.onDenied(permissions, never)
                activity.toast("授权后获取个人信息")
            }
        })
    }

    suspend fun snapLogin() {
        val result = apiCall { RetrofitHelper.api.snapLogin() }
        if (result.isSuccess) {
            SignInHandler.getInstance().signIn(result.getOrNull()!!)
            EventBus.getDefault().post(Event.ON_USER_SIGN_STATE_CHANGED)
            SavingUserMsg.getInstance().saveUserMsg2SD(
                RetrofitHelper.gson.toJson(result.getOrNull())
            )
        }
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var INSTANCE: RequirePermission? = null

        fun getInstance() =
            INSTANCE ?: synchronized(RequirePermission::class.java) {
                INSTANCE ?: RequirePermission()
                    .also { INSTANCE = it }
            }
    }
}