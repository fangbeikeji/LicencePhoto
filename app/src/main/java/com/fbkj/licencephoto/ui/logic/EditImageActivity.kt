package com.fbkj.licencephoto.ui.logic

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.BitmapFactory.decodeFile
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.fbkj.licencephoto.BuildConfig
import com.fbkj.licencephoto.R
import com.fbkj.licencephoto.config.Event.ON_BACKGROUND_CHANGED
import com.fbkj.licencephoto.config.Event.ON_BUFFING_CHANGED
import com.fbkj.licencephoto.config.Event.ON_ITEM_CHECK_CHANGED
import com.fbkj.licencephoto.config.Event.ON_PAY_FINISH
import com.fbkj.licencephoto.config.Event.ON_RELOAD_CHANGED
import com.fbkj.licencephoto.config.Event.ON_WHITE_CHANGED
import com.fbkj.licencephoto.databinding.ActivityNormalizeBinding
import com.fbkj.licencephoto.dialogs.MakingPop
import com.fbkj.licencephoto.local.LastClickRecord
import com.fbkj.licencephoto.model.EventModel
import com.fbkj.licencephoto.modifyfragments.BackgroundFragment
import com.fbkj.licencephoto.modifyfragments.ReloadFragment
import com.fbkj.licencephoto.modifyfragments.SizeFragment
import com.fbkj.licencephoto.utils.*
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BasePopupView
import jp.co.cyberagent.android.gpuimage.GPUImage
import jp.co.cyberagent.android.gpuimage.GPUImage.ResponseListener
import jp.co.cyberagent.android.gpuimage.GPUImageRenderer
import jp.co.cyberagent.android.gpuimage.PixelBuffer
import jp.co.cyberagent.android.gpuimage.filter.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class EditImageActivity : AppCompatActivity() {

    private var _binding: ActivityNormalizeBinding? = null
    private val binding get() = _binding!!
    private var white = 0f
    private var buffing = 0f
    private var whiteBitmap :Bitmap?=null
    private var buffedBitmap :Bitmap? = null
    private var viewModel: EditImageViewModel? = null

    private lateinit var makingPop: BasePopupView
    //??????????????????????????????????????????????????????????????????ui???
    private var mHandler = object : Handler(Looper.myLooper()!!) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if (msg.what == 101){
                viewModel!!.removeBackground(
                    this@EditImageActivity,
                    intent.data!!,
                    intent.getBooleanExtra("byCamera", false)
                )
            }
        }
    }

    private val reloadListMale = arrayListOf<Int>().apply {
        add(R.mipmap.ic_male1)
        add(R.mipmap.ic_male2)
        add(R.mipmap.ic_male3)
    }

    private val reloadListFemale = arrayListOf<Int>().apply {
        add(R.mipmap.ic_female1)
        add(R.mipmap.ic_female2)
        add(R.mipmap.ic_female3)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!BuildConfig.DEBUG){
            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)//?????????
        }

        _binding = ActivityNormalizeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mHandler.sendEmptyMessageDelayed(101, 400)
        initMakingPop()
        makingPop.show()
        viewModel = ViewModelProvider(this@EditImageActivity).get(EditImageViewModel::class.java)
        setSupportActionBar(binding.toolbar)
        EventBus.getDefault().register(this)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
        binding.toolbarTxt.text = LastClickRecord.getInstance().recordSizeData!!.sizeName
        LastClickRecord.getInstance().background = "??????"//???????????????????????????

        binding.viewPager2.adapter = ModifyPagerAdapter(this)
        binding.viewPager2.isUserInputEnabled = false
        binding.viewPager2.offscreenPageLimit = 4
        TabLayoutMediator(binding.tabLayout, binding.viewPager2) { tab, position ->
            when (position) {
                0 -> tab.text = "??????"
                1 -> tab.text = "??????"
                2 -> tab.text = "??????"
                3 -> tab.text = "??????"
            }
        }.attach()

        binding.tabLayout.addOnTabSelectedListener(object :TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> {
                        binding.viewPager2.visibility = View.VISIBLE
                        binding.buffingLayout.visibility = View.GONE
                    }
                    1 -> {
                        binding.viewPager2.visibility = View.GONE
                        binding.buffingLayout.visibility = View.VISIBLE
                    }
                    2 -> {
                        binding.viewPager2.visibility = View.VISIBLE
                        binding.buffingLayout.visibility = View.GONE
                    }
                    3 -> {
                        binding.viewPager2.visibility = View.VISIBLE
                        binding.buffingLayout.visibility = View.GONE
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })

        /**
         * ---------------------????????????????????????-------------------* */

        //?????????????????????????????????
        viewModel!!.rembgFile.observe(this) {
            makingPop.dismiss()
            val options = BitmapFactory.Options().apply {
                inSampleSize = 2
            }

            val removedBgBitmap = decodeFile(it.path, options)
            BitmapStore.getInstance().originBitmap = removedBgBitmap
            BitmapStore.getInstance().originBackUpBitmap = removedBgBitmap.copy(Bitmap.Config.ARGB_8888,true)

            binding.viewBorder.background = resources.getDrawable(R.drawable.blue_background)

            binding.remBgBitmap.setImageBitmap(removedBgBitmap)
            binding.nextStep.visibility = View.VISIBLE
        }

        //??????????????????ML??????
        viewModel!!.rembgBitmap.observe(this) {
            makingPop.dismiss()

            BitmapStore.getInstance().originBitmap = it
            BitmapStore.getInstance().originBackUpBitmap = it.copy(Bitmap.Config.ARGB_8888,true)

            binding.viewBorder.background = resources.getDrawable(R.drawable.blue_background)
            binding.remBgBitmap.setImageBitmap(it)
            binding.nextStep.visibility = View.VISIBLE
        }

        /**---------------------------------------------------------------*/

        binding.sbBuffe.setOnSeekBarChangeListener(object :SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                binding.remBgBitmap.setImageBitmap(GPUImage(this@EditImageActivity).apply {
                    setImage(whiteBitmap)
                    setFilter(
                        GPUImageBilateralBlurFilter(16f - p1 * 0.1f)
                    )
                }.bitmapWithFilterApplied)
                buffing = 16f - p1 * 0.1f
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
                whiteBitmap = GPUImage(this@EditImageActivity).apply {
                    setImage(BitmapStore.getInstance().originBackUpBitmap)
                    setFilter(GPUImageBrightnessFilter(white)) }.bitmapWithFilterApplied
            }
            override fun onStopTrackingTouch(p0: SeekBar?) {
            }
        })

        binding.sbWhith.setOnSeekBarChangeListener(object :SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                binding.remBgBitmap.setImageBitmap(GPUImage(this@EditImageActivity).apply {
                    setImage(buffedBitmap)
                    setFilter(GPUImageBilateralBlurFilter(buffing))
                    setFilter(
                        GPUImageBrightnessFilter( p1 * 0.001f)
                    ) }.bitmapWithFilterApplied)
                white = p1 * 0.001f
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
                buffedBitmap = GPUImage(this@EditImageActivity).apply {
                    setImage(BitmapStore.getInstance().originBackUpBitmap)
                    setFilter(GPUImageBilateralBlurFilter(buffing)) }.bitmapWithFilterApplied
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
            }
        })

        binding.nextStep.setOnClickListener {
            val sp = LastClickRecord.getInstance()
                .recordSizeData!!.sizePixel.split("*", " ")

            BitmapStore.getInstance().beautyBitmap = FileHandler.crop(//???????????????????????????????????????
                binding.frameCrop,
                sp[0].toInt(),
                sp[1].toInt()
            )

            BitmapStore.getInstance().removeBgBitmap = FileHandler.crop(//???????????????????????????????????????
                binding.beautyCanvas,
                sp[0].toInt(),
                sp[1].toInt()
            )

            binding.remBgBitmap.setImageBitmap(BitmapStore.getInstance().originBackUpBitmap)//???????????????

            BitmapStore.getInstance().originBitmap = FileHandler.crop(//????????????????????????????????????
                binding.remBgBitmap,
                sp[0].toInt(),
                sp[1].toInt()
            )

            PreviewEditActivity.start(
                this,
                binding.toolbarTxt.text.toString(),
                LastClickRecord.getInstance().recordSizeData!!.sizePixel
            )
        }
    }

    private fun initMakingPop() {
        makingPop = XPopup.Builder(this@EditImageActivity)
            .dismissOnBackPressed(false) // ??????????????????????????????????????????true
            .dismissOnTouchOutside(false) // ??????????????????????????????????????????true
            .autoFocusEditText(true)  // ????????????????????????????????????????????????true
            .asCustom(MakingPop(this))
    }

    companion object {
        fun start(context: Context, data: Uri, byCamera: Boolean) {
            context.startActivity(Intent(context, EditImageActivity::class.java).apply {
                setData(data)
                putExtra("byCamera", byCamera)
            })
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun eventData(message: EventModel) {
        /**
         * ?????????????????????????????????????????????originBitmap?????????????????????????????????????????????originBitmap???
         * ??????????????????bitmap??????,???????????????????????????
         * */
        when (message.message) {
            ON_BACKGROUND_CHANGED -> {//?????????
                changeBackground(message.c)
            }

            ON_ITEM_CHECK_CHANGED -> {//??????
                binding.toolbarTxt.text = message.name
            }

            ON_RELOAD_CHANGED -> {//??????
                when {
                    message.c == -1 -> {
                        binding.ivReload.setImageResource(0)
                    }
                    message.name == "0" -> {
                        binding.ivReload.setImageResource(reloadListMale[message.c])
                    }
                    message.name == "1" -> {
                        binding.ivReload.setImageResource(reloadListFemale[message.c])
                    }
                }
            }

            ON_PAY_FINISH -> {//???????????????????????????
                this@EditImageActivity.finish()
            }
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun changeBackground(tag: Int) {
        binding.viewBorder.background =
            when (tag) {
                0 -> resources.getDrawable(R.drawable.blue_background)
                1 -> resources.getDrawable(R.drawable.red_background)
                2 -> resources.getDrawable(R.drawable.white_background)
                3 -> resources.getDrawable(R.drawable.blue_transparent_background)
                4 -> resources.getDrawable(R.drawable.red_transparent_background)
                else -> resources.getDrawable(R.drawable.grey_transparent_background)
            }

        LastClickRecord.getInstance().background = when (tag) {
            0 -> "??????"
            1 -> "??????"
            2 -> "??????"
            3 -> "????????????"
            4 -> "????????????"
            else -> "????????????"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }


    private class ModifyPagerAdapter(fragmentActivity: FragmentActivity) :
        FragmentStateAdapter(fragmentActivity) {

        override fun getItemCount() = 4

        override fun createFragment(position: Int): Fragment {

            return when (position) {
                0 -> SizeFragment.newInstance()
                1 -> Fragment()
                2 -> ReloadFragment.newInstance()
                3 -> BackgroundFragment.newInstance()
                else -> throw IllegalStateException("No fragment can be instantiated for position $position")
            }
        }
    }

}

