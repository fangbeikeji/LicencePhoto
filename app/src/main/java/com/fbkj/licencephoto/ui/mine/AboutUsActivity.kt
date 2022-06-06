package com.fbkj.licencephoto.ui.mine

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.fbkj.licencephoto.BuildConfig
import com.fbkj.licencephoto.R
import com.fbkj.licencephoto.config.Constants.USER_AGREEMENT
import com.fbkj.licencephoto.config.Constants.USER_PRIVACY
import com.fbkj.licencephoto.databinding.AboutUsActivityBinding
import com.fbkj.licencephoto.ui.WebViewActivity
import com.fbkj.licencephoto.utils.toast

class AboutUsActivity : AppCompatActivity() {

    private var _binding: AboutUsActivityBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding=AboutUsActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
        binding.toolbarTxt.text="关于我们"

        binding.swAgreement.setOnClickListener {
            WebViewActivity.startUrl(
                this, USER_AGREEMENT, this.getString(
                    R.string.service_agreement
                )
            )
        }

        binding.swPrivacy.setOnClickListener {
            WebViewActivity.startUrl(this, USER_PRIVACY, this.getString(R.string.privacy_policy))
        }

        binding.swCurrentV.setOnClickListener {
            toast("当前版本：${BuildConfig.VERSION_NAME}") }
    }

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, AboutUsActivity::class.java))
        }
    }
}