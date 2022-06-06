package com.fbkj.licencephoto

import android.app.Application
import android.content.Context
import com.fbkj.licencephoto.config.Constants.UM_APP_KEY
import com.fbkj.licencephoto.utils.ContextHolder
import com.hjq.permissions.XXPermissions
import com.meituan.android.walle.ChannelInfo
import com.meituan.android.walle.WalleChannelReader
import com.tencent.mmkv.MMKV
import com.umeng.analytics.MobclickAgent
import com.umeng.commonsdk.UMConfigure

/**
- @author:  LZC
- @time:  2021/6/7
- @desc:
 */

class App : Application() {
    var channelInfo: ChannelInfo? = null
    var channel: String? = null

    companion object {
        lateinit var instance: Application
            private set
    }

    override fun onCreate() {
        super.onCreate()
        MMKV.initialize(this)
        //debugable分区存储适配需要添加
        XXPermissions.setScopedStorage(true)
        instance = this
        ContextHolder.context = applicationContext
        channelInfo = WalleChannelReader.getChannelInfo(applicationContext)

        channel = if (channelInfo != null) {
            channelInfo!!.channel
        } else {
            "unset"
        }

        if (!BuildConfig.DEBUG) {
            if (MMKV.defaultMMKV().decodeBool("agreePrivacy", false)) {
                initUM(this)
            } else {
                UMConfigure.preInit(this, UM_APP_KEY, channel)//预加载友盟，必须
            }
        }
    }

    fun initUM(context: Context) {
        // 在此处调用基础组件包提供的初始化函数 相应信息可在应用管理 -> 应用信息 中找到 http://message.umeng.com/list/apps
        // 参数一：当前上下文context；
        // 参数二：应用申请的Appkey；
        // 参数三：渠道名称；
        // 参数四：设备类型，必须参数，传参数为UMConfigure.DEVICE_TYPE_PHONE则表示手机；传参数为UMConfigure.DEVICE_TYPE_BOX则表示盒子；默认为手机；
        // 参数五：Push推送业务的secret 填充Umeng Message Secret对应信息


        if (BuildConfig.DEBUG) {
            MobclickAgent.setCatchUncaughtExceptions(false)
        } else {
            UMConfigure.init(context, UM_APP_KEY, channel, UMConfigure.DEVICE_TYPE_PHONE, "")
            MobclickAgent.setCatchUncaughtExceptions(true)
            //埋点
            UMConfigure.setProcessEvent(true)
            // 选用AUTO页面采集模式
            MobclickAgent.setPageCollectionMode(MobclickAgent.PageMode.AUTO)
        }
        UMConfigure.setLogEnabled(BuildConfig.DEBUG)

    }

}