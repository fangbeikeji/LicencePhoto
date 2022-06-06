package com.fbkj.licencephoto.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import com.fbkj.licencephoto.App
import com.fbkj.licencephoto.BuildConfig
import com.fbkj.licencephoto.R
import com.fbkj.licencephoto.base.BaseActivity
import com.fbkj.licencephoto.config.Constants
import com.fbkj.licencephoto.config.Event.ON_LAUNCH_SUCCESS
import com.fbkj.licencephoto.databinding.SplashActivityBinding
import com.fbkj.licencephoto.dialogs.StartPop
import com.lxj.xpopup.XPopup
import com.tencent.mmkv.MMKV
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity() {
    private var _binding: SplashActivityBinding? = null
    private val binding get() = _binding!!
    private val kv = MMKV.defaultMMKV()
    private val context = this@SplashActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = SplashActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        EventBus.getDefault().register(this@SplashActivity)
        val animation = AlphaAnimation(0.5f, 1.0f)
        animation.duration = 1000
        binding.splashView.animation = animation
        binding.tvVersion.text =
            String.format(getString(R.string.version_code), BuildConfig.VERSION_NAME)

        animation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}

            override fun onAnimationEnd(animation: Animation?) {
                if (!kv.decodeBool("agreedMode", false)) {
                    XPopup.Builder(this@SplashActivity)
                        .asCustom(
                            StartPop(this@SplashActivity,
                                object : StartPop.Callback {
                                    override fun onClick(v: View?) {
                                        when (v!!.id) {
                                            R.id.tv_user_agreement -> {
                                                WebViewActivity.startUrl(
                                                    this@SplashActivity,
                                                    Constants.USER_AGREEMENT,
                                                    context.getString(R.string.service_agreement)
                                                )
                                            }
                                            R.id.tv_privacy_policy -> {
                                                WebViewActivity.startUrl(
                                                    this@SplashActivity,
                                                    Constants.USER_PRIVACY,
                                                    context.getString(R.string.privacy_policy)
                                                )
                                            }
                                            R.id.tv_not_agreed -> {
                                                finish()
                                            }
                                            R.id.tv_agreed -> {
                                                //初始化开始
                                                App().initUM(context)
                                                MMKV.defaultMMKV().encode("agreePrivacy", true)
                                                MainActivity.start(this@SplashActivity)
                                                kv.encode("agreedMode", true)
                                            }
                                        }
                                    }
                                })
                        ).show()
                } else {
                    MainActivity.start(this@SplashActivity)
                }
            }

            override fun onAnimationRepeat(animation: Animation?) {

            }
        })

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun eventData(message: String) {
        if (message == ON_LAUNCH_SUCCESS){
            this@SplashActivity.finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this@SplashActivity)
    }
}