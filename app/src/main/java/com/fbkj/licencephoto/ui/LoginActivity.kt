package com.fbkj.licencephoto.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.fbkj.licencephoto.R
import com.fbkj.licencephoto.base.BaseActivity
import com.fbkj.licencephoto.config.Constants.USER_AGREEMENT
import com.fbkj.licencephoto.config.Constants.USER_PRIVACY
import com.fbkj.licencephoto.config.Event
import com.fbkj.licencephoto.databinding.ActivitySmsloginBinding
import com.fbkj.licencephoto.model.EventModel
import com.fbkj.licencephoto.utils.Md5Utils
import com.fbkj.licencephoto.utils.toast
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.regex.Pattern

class LoginActivity : BaseActivity() {

    private var isChecked = false
    private lateinit var binding: ActivitySmsloginBinding
    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySmsloginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this)[LoginViewModel::class.java]
        EventBus.getDefault().register(this)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
        binding.toolbarTxt.text = "验证码登录"
        binding.requestCode.text = "发送验证码"

        binding.ivCheck.setOnClickListener {
            if (isChecked) {
                isChecked = false
                binding.ivCheck.setImageResource(R.drawable.ic_not_confirm)
            } else {
                isChecked = true
                binding.ivCheck.setImageResource(R.drawable.ic_confirm)
            }
        }

        binding.requestCode.setOnClickListener {
            if (!check()) return@setOnClickListener
            fetchSMS()
        }

        binding.smsLogin.setOnClickListener {
            viewModel.smsLogin(
                binding.editPhone.text.toString().trim(),
                binding.etSms.text.toString().trim()
            )
        }

        binding.tvFw.setOnClickListener {
            WebViewActivity.startUrl(this, USER_AGREEMENT, getString(R.string.service_agreement))
        }

        binding.tvPy.setOnClickListener {
            WebViewActivity.startUrl(this, USER_PRIVACY, getString(R.string.privacy_policy))
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun eventData(message: EventModel) {
        when (message.message) {
            Event.ON_USER_SIGN_STATE_CHANGED -> {
                this@LoginActivity.finish()
            }
        }
    }

    private fun check(): Boolean {
        if (!isChecked) {
            toast(R.string.login_check_tip)
            return false
        }
        return true
    }

    private fun fetchSMS() {
        val phone = binding.editPhone.text.toString().trim()
        val reg = Pattern.compile("^(13|14|15|16|17|18|19)\\d{9}$")
        val machePhone = reg.matcher(phone)
        if (phone.isEmpty() || phone.length != 11 || !machePhone.matches()) {
            this.toast("请输入完整正确手机号")
            return
        } else {
            binding.requestCode.isClickable = false
            binding.requestCode.setTextColor(resources.getColor(R.color.color_6f))
            binding.smsTag.visibility = View.VISIBLE
            binding.smsTag.text = String.format(getString(R.string.has_send_to), phone)
            val sign = Md5Utils.md5(String.format("%s.%s.fb", phone, phone.substring(0..9)))
            viewModel.getSmsCode(sign, phone)
            initCountDown()
            this.toast("正在发送验证码..")
        }
    }

    private fun initCountDown() {
        object : CountDownTimer(60 * 1000, 300) {
            override fun onFinish() {
                binding.requestCode.text = "重新发送"
                binding.requestCode.setTextColor(resources.getColor(R.color.black))
                binding.requestCode.isClickable = true
            }

            override fun onTick(millisUntilFinished: Long) {
                val second = millisUntilFinished / 1000 % 60
                binding.requestCode.text = "(${second}s)"
            }
        }.start()
    }

    companion object {
        @JvmStatic
        fun start(context: Context) {
            context.startActivity(Intent(context, LoginActivity::class.java))
        }
    }
}