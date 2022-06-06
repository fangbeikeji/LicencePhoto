package com.fbkj.licencephoto.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.Target
import com.fbkj.licencephoto.R
import com.fbkj.licencephoto.config.Constants
import com.fbkj.licencephoto.databinding.DownLoadPageActivityBinding
import com.fbkj.licencephoto.utils.PhotoUtils
import com.fbkj.licencephoto.utils.toast
import com.hjq.permissions.XXPermissions
import com.umeng.analytics.MobclickAgent

class DownLoadPageActivity : AppCompatActivity() {

    private var _binding: DownLoadPageActivityBinding? = null
    private val binding get() = _binding!!
    private var fileUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = DownLoadPageActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
        binding.toolbarTxt.text = "下载页"

        fileUrl = if (intent.getBooleanExtra("quickIn", false)) {
            Constants.HOST_HEAD + intent.getStringExtra("url")
        } else {
            intent.getStringExtra("url")
        }

        Glide.with(this)
            .load(fileUrl)
            .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
            .into(binding.remBgBitmap)

        if (!intent.getBooleanExtra("isMultipart", true)) {
            binding.flowRadioGroup.visibility = View.GONE
            binding.remBgBitmap.background =
                when (intent.getStringExtra("background")) {
                    "蓝底" -> resources.getDrawable(R.drawable.blue_background)
                    "红底" -> resources.getDrawable(R.drawable.red_background)
                    "白底" -> resources.getDrawable(R.drawable.white_background)
                    "蓝渐白底" -> resources.getDrawable(R.drawable.blue_transparent_background)
                    "红渐白底" -> resources.getDrawable(R.drawable.red_transparent_background)
                    else -> resources.getDrawable(R.drawable.grey_transparent_background)
                }
        }

        binding.flowRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rb_blue -> {
                    binding.remBgBitmap.background =
                        resources.getDrawable(R.drawable.blue_background)
                }
                R.id.rb_red -> {
                    binding.remBgBitmap.background =
                        resources.getDrawable(R.drawable.red_background)
                }
                R.id.rb_white -> {
                    binding.remBgBitmap.background =
                        resources.getDrawable(R.drawable.white_background)
                }
                R.id.rb_blue_transparent -> {
                    binding.remBgBitmap.background =
                        resources.getDrawable(R.drawable.blue_transparent_background)
                }
                R.id.rb_red_transparent -> {
                    binding.remBgBitmap.background =
                        resources.getDrawable(R.drawable.red_transparent_background)
                }
                R.id.rb_grey_transparent -> {
                    binding.remBgBitmap.background =
                        resources.getDrawable(R.drawable.grey_transparent_background)
                }
            }
        }

        binding.tvDownload.setOnClickListener {
            binding.remBgBitmap.isDrawingCacheEnabled = true
            XXPermissions.with(this@DownLoadPageActivity).permission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ).request { _, all ->
                if (all) {
//                    PhotoUtils.saveBitmap2Gallery2(this@DownLoadPageActivity,
//                        binding.remBgBitmap.drawingCache)

                    PhotoUtils.saveBitmap2Gallery(this@DownLoadPageActivity,
                        binding.remBgBitmap.drawingCache)
//                    FileHandler.saveImageToGallery(
//                        this, binding.remBgBitmap.drawingCache
//                    )
                    MobclickAgent.onEvent(this, "download_pic")
                    toast("已保存到本地相册")
                    binding.remBgBitmap.isDrawingCacheEnabled = false
                } else {
                    this.toast("请打开读写权限再保存进相册")
                    binding.remBgBitmap.isDrawingCacheEnabled = false
                }
            }


        }
    }

    companion object {
        fun start(
            context: Context,
            url: String,
            isMultipart: Boolean,
            background: String,
            quickIn: Boolean
        ) {
            context.startActivity(Intent(context, DownLoadPageActivity::class.java).apply {
                putExtra("url", url)
                putExtra("isMultipart", isMultipart)
                putExtra("background", background)
                putExtra("quickIn", quickIn)
            })
        }
    }
}