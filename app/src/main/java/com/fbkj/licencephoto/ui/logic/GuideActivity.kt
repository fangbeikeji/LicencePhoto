package com.fbkj.licencephoto.ui.logic

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.fbkj.licencephoto.R
import com.fbkj.licencephoto.databinding.GuideActivityBinding
import com.fbkj.licencephoto.model.CustomViewsInfo
import com.tencent.mmkv.MMKV
import java.util.*

class GuideActivity : AppCompatActivity() {

    private var _binding: GuideActivityBinding? = null
    private val binding get() = _binding!!
    private var data: MutableList<CustomViewsInfo> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = GuideActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.ivClose.setOnClickListener { finish() }
        data.add(0, CustomViewsInfo(R.mipmap.show_step1))
        data.add(1, CustomViewsInfo(R.mipmap.show_step2))
        data.add(2, CustomViewsInfo(R.mipmap.show_step3))
        data.add(3, CustomViewsInfo(R.mipmap.show_step4))
        binding.xbannerGuide.setBannerData(data)

        if (MMKV.defaultMMKV().decodeBool("isFirstOpen",true)) {
            binding.ivClose.visibility = View.GONE
            MMKV.defaultMMKV().encode("isFirstOpen",false)
        }

        binding.xbannerGuide.loadImage { _, model, view, _ ->
            view.setBackgroundResource((model as CustomViewsInfo).info)
        }

        binding.toNext.setOnClickListener {
            binding.xbannerGuide.viewPager.arrowScroll(View.FOCUS_RIGHT)
        }

        binding.xbannerGuide.setOnPageChangeListener(
            object : ViewPager.OnPageChangeListener {
                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
                ) {
                }

                override fun onPageSelected(position: Int) {
                    when (position) {
                        0 -> {
                            binding.tvStep.text = "????????????"
                            binding.tvDescription.text = "????????????"
                            binding.tvWarn.text = getString(R.string.forbid_step1)
                        }
                        1 -> {
                            binding.tvStep.text = "????????????"
                            binding.tvDescription.text = "??????????????????????????????????????????"
                            binding.tvWarn.text = ""
                        }
                        2 -> {
                            binding.tvStep.text = "????????????"
                            binding.tvDescription.text = "??????????????????????????????"
                            binding.tvWarn.text = getString(R.string.forbid_step3)
                        }
                        3 -> {
                            binding.tvStep.text = "????????????"
                            binding.tvDescription.text = "??????????????????????????????????????????????????????"
                            binding.tvWarn.text = ""
                            binding.toNext.text = "?????????"
                            binding.toNext.setOnClickListener {
                                finish()
                            }
                        }
                    }
                }

                override fun onPageScrollStateChanged(state: Int) {

                }
            })
    }

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, GuideActivity::class.java))
        }
    }
}