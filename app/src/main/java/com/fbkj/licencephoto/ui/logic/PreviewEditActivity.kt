package com.fbkj.licencephoto.ui.logic

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.fbkj.licencephoto.R
import com.fbkj.licencephoto.config.Event.ON_PAY_FINISH
import com.fbkj.licencephoto.databinding.PreviewEditActivityBinding
import com.fbkj.licencephoto.local.LastClickRecord
import com.fbkj.licencephoto.model.EventModel
import com.fbkj.licencephoto.utils.BitmapStore
import com.umeng.analytics.MobclickAgent
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

@Suppress("DEPRECATION")
class PreviewEditActivity : AppCompatActivity() {

    private var _binding: PreviewEditActivityBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: GridAdapter
    private lateinit var background: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        _binding = PreviewEditActivityBinding.inflate(layoutInflater)
        EventBus.getDefault().register(this)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
        binding.toolbarTxt.text = "${intent.getStringExtra("size")}（预览效果）"
        background = LastClickRecord.getInstance().background
        val map = HashMap<String, String>()
        map["size_name"] = LastClickRecord.getInstance().recordSizeData!!.sizeName
        map["size_pixel"] = LastClickRecord.getInstance().recordSizeData!!.sizePixel
        MobclickAgent.onEvent(this, "make_size", map)
        binding.remBgBitmap.setImageBitmap(BitmapStore.getInstance().removeBgBitmap)

        binding.ivHide.setImageBitmap(BitmapStore.getInstance().removeBgBitmap)

//        binding.ivHide.background =
//            when (LastClickRecord.getInstance().background) {
//                "蓝底" -> resources.getDrawable(R.drawable.blue_background)
//                "红底" -> resources.getDrawable(R.drawable.red_background)
//                "白底" -> resources.getDrawable(R.drawable.white_background)
//                "蓝渐白底" -> resources.getDrawable(R.drawable.blue_transparent_background)
//                "红渐白底" -> resources.getDrawable(R.drawable.red_transparent_background)
//                else -> resources.getDrawable(R.drawable.grey_transparent_background)
//            }

        binding.remBgBitmap.background =
            when (LastClickRecord.getInstance().background) {
                "蓝底" -> resources.getDrawable(R.drawable.blue_background)
                "红底" -> resources.getDrawable(R.drawable.red_background)
                "白底" -> resources.getDrawable(R.drawable.white_background)
                "蓝渐白底" -> resources.getDrawable(R.drawable.blue_transparent_background)
                "红渐白底" -> resources.getDrawable(R.drawable.red_transparent_background)
                else -> resources.getDrawable(R.drawable.grey_transparent_background)
            }

        /**
         * 保存证件照
         * */
        binding.saveLicence.setOnClickListener {
            LastClickRecord.getInstance().isOrder = false
            SaveLicenceActivity.start(this@PreviewEditActivity)
            MobclickAgent.onEvent(this@PreviewEditActivity, "save_licence")
        }

        /**
         * 冲印
         * */
        binding.tvFlushNow.setOnClickListener {
            LastClickRecord.getInstance().isOrder = false
            FlushPageActivity.start(this@PreviewEditActivity, false, "")
            MobclickAgent.onEvent(this@PreviewEditActivity, "flush_licence")
        }

        binding.flowRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rb_blue -> {
                    binding.remBgBitmap.background =
                        resources.getDrawable(R.drawable.blue_background)
                    binding.ivHide.background =
                        resources.getDrawable(R.drawable.blue_background)
                    LastClickRecord.getInstance().background = "蓝底"
                }
                R.id.rb_red -> {
                    binding.remBgBitmap.background =
                        resources.getDrawable(R.drawable.red_background)
                    binding.ivHide.background =
                        resources.getDrawable(R.drawable.red_background)
                    LastClickRecord.getInstance().background = "红底"
                }
                R.id.rb_white -> {
                    binding.remBgBitmap.background =
                        resources.getDrawable(R.drawable.white_background)
                    binding.ivHide.background =
                        resources.getDrawable(R.drawable.white_background)
                    LastClickRecord.getInstance().background = "白底"
                }
                R.id.rb_blue_transparent -> {
                    binding.remBgBitmap.background =
                        resources.getDrawable(R.drawable.blue_transparent_background)
                    binding.ivHide.background =
                        resources.getDrawable(R.drawable.blue_transparent_background)
                    LastClickRecord.getInstance().background = "蓝渐白底"
                }
                R.id.rb_red_transparent -> {
                    binding.remBgBitmap.background =
                        resources.getDrawable(R.drawable.red_transparent_background)
                    binding.ivHide.background =
                        resources.getDrawable(R.drawable.red_transparent_background)
                    LastClickRecord.getInstance().background = "红渐白底"
                }
                R.id.rb_grey_transparent -> {
                    binding.remBgBitmap.background =
                        resources.getDrawable(R.drawable.grey_transparent_background)
                    binding.ivHide.background =
                        resources.getDrawable(R.drawable.grey_transparent_background)
                    LastClickRecord.getInstance().background = "黑渐白底"
                }
            }
            binding.ivHide.isDrawingCacheEnabled = true

            BitmapStore.getInstance().beautyBitmap = binding.ivHide.drawingCache

            adapter.notifyDataSetChanged()
        }

        //冲印方格
        gridView(8, BitmapStore.getInstance().removeBgBitmap)

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun eventData(message: EventModel) {
        when (message.message) {
            ON_PAY_FINISH -> {
                this@PreviewEditActivity.finish()
            }
        }
    }

    private fun gridView(flushNum: Int, bitmap: Bitmap) {
        val imageArray = arrayListOf<Bitmap>()
        for (i in 0 until flushNum) {
            imageArray.add(bitmap)
        }
        adapter = GridAdapter(imageArray)
        binding.recyclerView.layoutManager = GridLayoutManager(
            this, flushNum / 2
        )
        binding.recyclerView.adapter = adapter
    }

    companion object {
        fun start(
            context: Context,
            size: String,
            pixel: String
        ) {
            context.startActivity(
                Intent(context, PreviewEditActivity::class.java).apply {
                    putExtra("size", size)
                    putExtra("pixel", pixel)
                })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this@PreviewEditActivity)
    }

}

class GridAdapter(sizeData: List<Bitmap>) : BaseQuickAdapter<Bitmap, BaseViewHolder>
    (R.layout.grid_items, sizeData) {

    override fun convert(helper: BaseViewHolder?, item: Bitmap?) {
        helper?.setImageBitmap(R.id.iv_images, item!!)
        helper?.setBackgroundRes(
            R.id.iv_images,
            when (LastClickRecord.getInstance().background) {
                "蓝底" -> R.drawable.blue_background
                "红底" -> R.drawable.red_background
                "白底" -> R.drawable.white_background
                "蓝渐白底" -> R.drawable.blue_transparent_background
                "红渐白底" -> R.drawable.red_transparent_background
                else -> R.drawable.grey_transparent_background
            }
        )
    }
}