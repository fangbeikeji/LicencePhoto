package com.fbkj.licencephoto.ui.mine

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.fbkj.licencephoto.BuildConfig
import com.fbkj.licencephoto.R
import com.fbkj.licencephoto.config.Constants
import com.fbkj.licencephoto.databinding.RecommendActivityBinding
import com.fbkj.licencephoto.ui.WebViewActivity
import com.fbkj.licencephoto.utils.toast

class RecommendActivity : AppCompatActivity() {

    private var _binding: RecommendActivityBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: RecommendViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = RecommendActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this)[RecommendViewModel::class.java]
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
        binding.toolbarTxt.text = "投诉建议"
        binding.tvVersion.text =
            String.format(resources.getString(R.string.version_code),BuildConfig.VERSION_NAME)
        binding.tvCommit.setOnClickListener { commit() }
        binding.tvAgreement.setOnClickListener {
            WebViewActivity.startUrl(
                this, Constants.USER_AGREEMENT, getString(R.string.service_agreement)
            )
        }
        binding.tvPrivacy.setOnClickListener {
            WebViewActivity.startUrl(
                this, Constants.USER_PRIVACY, getString(R.string.privacy_policy)
            )
        }
    }

    private fun commit() {
        val contact = binding.etPhoneEmail.text.toString()
        val content = binding.etContent.text.toString()
        if (contact.isEmpty() || content.isEmpty()) {
            toast("请补充完整信息")
            return
        }
        //POST
        viewModel.commit(this, contact, content)

    }

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, RecommendActivity::class.java))
        }
    }
}