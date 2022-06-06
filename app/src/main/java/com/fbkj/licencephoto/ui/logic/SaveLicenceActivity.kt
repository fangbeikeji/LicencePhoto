package com.fbkj.licencephoto.ui.logic

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Paint
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.fbkj.licencephoto.R
import com.fbkj.licencephoto.config.Constants
import com.fbkj.licencephoto.config.Event
import com.fbkj.licencephoto.databinding.ActivitySaveLicenceBinding
import com.fbkj.licencephoto.dialogs.DiscountPop
import com.fbkj.licencephoto.local.LastClickRecord
import com.fbkj.licencephoto.model.EventModel
import com.fbkj.licencephoto.ui.WebViewActivity
import com.fbkj.licencephoto.utils.*
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BasePopupView
import com.tencent.mm.opensdk.modelpay.PayReq
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import com.tencent.mmkv.MMKV
import com.umeng.analytics.MobclickAgent
import kotlinx.android.synthetic.main.discount_pop.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class SaveLicenceActivity : AppCompatActivity() {

    private var _binding: ActivitySaveLicenceBinding? = null
    private val binding get() = _binding!!
    private var checkTag = 0
    private var beautyCost = 0
    private var singleBg = 0
    private var multiBg = 0
    private var currentCost = 0
    private var payType = -1

    //0是单背景，1是多背景,开始默认单
    private var isMulti = 0
    private var isShownDiscount = false
    private val kv = MMKV.defaultMMKV()
    private var imageArray = arrayListOf<Bitmap>()
    private var adapter = MultiBgAdapter(arrayListOf())
    private var finialBitmap = BitmapStore.getInstance().beautyBitmap
    private var isRemoveBgBitmap = BitmapStore.getInstance().removeBgBitmap
    private lateinit var discountPopView: BasePopupView
    private lateinit var viewModel: OrderCommitViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivitySaveLicenceBinding.inflate(layoutInflater)
        setContentView(binding.root)
        EventBus.getDefault().register(this)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.recyclerView.visibility = View.GONE

        binding.toolbar.setNavigationOnClickListener {
            //第二次进来，给个优惠卷
            if (isShownDiscount) {
                onBackPressed()
            } else {
                if (LastClickRecord.getInstance().saveLicenceTimes >= if (
                        kv.decodeBool("firstDiscount",true)
                ) 2 else 3) {
                    initDiscountPop()
                    discountPopView.show()
                } else {
                    onBackPressed()
                }
            }

        }
        binding.toolbarTxt.text = "保存证件照"
        LastClickRecord.getInstance().apply {
            orderType = 0
            saveLicenceTimes++
        }

        viewModel = ViewModelProvider(this)[OrderCommitViewModel::class.java]

        viewModel.elePrice.observe(this) {
            for (i in it.indices) {
                when (it[i].type) {
                    3 -> {
                        singleBg = it[i].price
                        binding.singleBackground.text =
                            "￥${AmountUtil.getInstance().changeF2Y(it[i].price.toLong())}（单背景）"
                    }
                    4 -> {
                        multiBg = it[i].price
                        binding.multiBackground.text =
                            "￥${AmountUtil.getInstance().changeF2Y(it[i].price.toLong())}（多背景）"
                    }
                    5 -> {
                        beautyCost = it[i].price
                        binding.beautyReload.text =
                            "￥${AmountUtil.getInstance().changeF2Y(it[i].price.toLong())}（美颜换装）"
                    }
                }
            }

            currentCost = singleBg + beautyCost
            binding.tvCost.text =
                "￥${AmountUtil.getInstance().changeF2Y((currentCost).toLong())}"
        }
        //获取订单价格
        viewModel.getElectronicPrice()

        //一开始显示换装美颜的图，已经加上背景了的
        binding.eleOrderImage.setImageBitmap(BitmapStore.getInstance().beautyBitmap)

        binding.eleOrderImage.background =
            when (LastClickRecord.getInstance().background) {
                "蓝底" -> resources.getDrawable(R.drawable.blue_background)
                "红底" -> resources.getDrawable(R.drawable.red_background)
                "白底" -> resources.getDrawable(R.drawable.white_background)
                "蓝渐白底" -> resources.getDrawable(R.drawable.blue_transparent_background)
                "红渐白底" -> resources.getDrawable(R.drawable.red_transparent_background)
                else -> resources.getDrawable(R.drawable.grey_transparent_background)
            }

        binding.sizeName.text = LastClickRecord.getInstance().recordSizeData!!.sizeName
        binding.sizePixel.text = LastClickRecord.getInstance().recordSizeData!!.sizePixel
        binding.sizeUnit.text = LastClickRecord.getInstance().recordSizeData!!.sizeUnit
        binding.background.text = LastClickRecord.getInstance().background

        //默认单背景和美颜换装
        binding.singleBackground.isChecked = true
        binding.beautyReload.isChecked = true
        multiBgRecyclerView(BitmapStore.getInstance().removeBgBitmap)

        binding.flowRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                //选择单背景
                R.id.single_background -> {
                    currentCost = if (binding.beautyReload.isChecked) {
                        singleBg + beautyCost
                    } else {
                        singleBg
                    }
                    checkTag = 0
                    isMulti = 0
                    binding.background.text = LastClickRecord.getInstance().background
                    binding.recyclerView.visibility = View.GONE
                }
                //选择多背景
                R.id.multi_background -> {
                    currentCost = if (binding.beautyReload.isChecked) {
                        multiBg + beautyCost
                    } else {
                        multiBg
                    }
                    checkTag = 1
                    isMulti = 1
                    binding.background.text = getString(R.string.multi_background)
                    binding.recyclerView.visibility = View.VISIBLE
                }
            }

            binding.tvCost.text = "￥${AmountUtil.getInstance().changeF2Y((currentCost).toLong())}"
            if (binding.discountChosen.isChecked) {
                binding.afterCount.visibility = View.VISIBLE
                binding.afterCount.text =
                    "￥${AmountUtil.getInstance().changeF2Y((currentCost - 300).toLong())}"
                binding.tvCost.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                binding.tvCost.paintFlags = 0
                binding.afterCount.visibility = View.GONE
            }

        }

        binding.discountChosen.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.afterCount.visibility = View.VISIBLE
                binding.afterCount.text =
                    "￥${AmountUtil.getInstance().changeF2Y((currentCost - 300).toLong())}"
                binding.discountChosen.setBackgroundResource(R.mipmap.ic_address_on)
                binding.tvCost.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                binding.discountChosen.setBackgroundResource(R.mipmap.ic_address_off)
                binding.tvCost.paintFlags = 0
                binding.afterCount.visibility = View.GONE
            }
        }

        //选择有美颜和换装的
        binding.beautyReload.setOnCheckedChangeListener { _, isChecked ->
            currentCost = if (isChecked) {
                finialBitmap = BitmapStore.getInstance().beautyBitmap
                isMultiWithBeauty(true)
                isRemoveBgBitmap = BitmapStore.getInstance().removeBgBitmap
                binding.eleOrderImage.setImageBitmap( BitmapStore.getInstance().beautyBitmap)
                if (checkTag == 0) {
                    singleBg + beautyCost
                } else {
                    multiBg + beautyCost
                }
            } else {//选择原图扣出来的
                finialBitmap = BitmapStore.getInstance().originBitmap
                isMultiWithBeauty(false)
                isRemoveBgBitmap = BitmapStore.getInstance().originBitmap
                binding.eleOrderImage.setImageBitmap(BitmapStore.getInstance().originBitmap)
                if (checkTag == 0) {
                    singleBg
                } else {
                    multiBg
                }
            }

            binding.tvCost.text = "￥${AmountUtil.getInstance().changeF2Y((currentCost).toLong())}"
            if (binding.discountChosen.isChecked) {
                binding.afterCount.visibility = View.VISIBLE
                binding.afterCount.text =
                    "￥${AmountUtil.getInstance().changeF2Y((currentCost - 300).toLong())}"
                binding.tvCost.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                binding.tvCost.paintFlags = 0
                binding.afterCount.visibility = View.GONE
            }
        }

        /**-----------------------------------支付回调-----------------------------------------*/

        //获取订单后信息后挑起微信支付
        viewModel.wxOrderCallBackSuccessData.observe(this) {
            binding.rlProgressBar.visibility = View.GONE
            val api = WXAPIFactory.createWXAPI(this@SaveLicenceActivity, it.params.appid)
            if (!api.isWXAppInstalled) {
                toast("您尚未安装微信客户端，请安装微信客户端再重新支付")
                return@observe
            }
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    val request = PayReq()
                    request.appId = it.params.appid
                    request.partnerId = it.params.partnerid
                    request.prepayId = it.params.prepayid
                    request.packageValue = it.params.`package`
                    request.nonceStr = it.params.noncestr
                    request.timeStamp = it.params.timestamp
                    request.sign = it.params.paySign
                    api.sendReq(request)
                }
            }
        }

        viewModel.aliOrderCallBackSuccessData.observe(this) {
            LastClickRecord.getInstance().tradNo = it.trade_no
            lifecycleScope.launch {
                ZfbCallBack.getInstance().pay(this@SaveLicenceActivity, it.params!!)
            }
        }

        /**-----------------------------------请求订单---------------------------------------*/

        binding.wxPay.setOnClickListener {
            payType = 0
            viewModel.userOrderCommit(
                isMulti,
                1,//电子照
                if (binding.beautyReload.isChecked) 1 else 0,//是否美颜换装
                0,//冲印数
                LastClickRecord.getInstance().recordSizeData!!.sizeName,
                "电子版订单",
                LastClickRecord.getInstance().recordSizeData!!.sizeUnit,
                LastClickRecord.getInstance().recordSizeData!!.sizePixel,
                LastClickRecord.getInstance().background,
                0,
                finialBitmap,
                isRemoveBgBitmap,
                payType,
                if (binding.discountChosen.isChecked) 1 else 0//优惠卷
            )
            binding.rlProgressBar.visibility = View.VISIBLE
            MobclickAgent.onEvent(this@SaveLicenceActivity, "wx_pay")
        }

        binding.zfbPay.setOnClickListener {
            payType = 1
            viewModel.userOrderCommit(
                isMulti,
                1,//电子照
                if (binding.beautyReload.isChecked) 1 else 0,//是否美颜换装
                0,//冲印数
                LastClickRecord.getInstance().recordSizeData!!.sizeName,
                "电子版订单",
                LastClickRecord.getInstance().recordSizeData!!.sizeUnit,
                LastClickRecord.getInstance().recordSizeData!!.sizePixel,
                LastClickRecord.getInstance().background,
                0,
                finialBitmap,
                isRemoveBgBitmap,
                payType,
                if (binding.discountChosen.isChecked) 1 else 0,//优惠卷
            )
            binding.rlProgressBar.visibility = View.VISIBLE
            MobclickAgent.onEvent(this@SaveLicenceActivity, "zfb_pay")
        }

        /**------------------------------------------------------------------------------*/

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

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun eventData(message: EventModel) {
        when (message.message) {
            Event.ON_PAY_FINISH -> {
                this@SaveLicenceActivity.finish()
            }
        }
    }

    /**
     * 多背景展示一列模板
     * */
    private fun isMultiWithBeauty(with: Boolean) {
        imageArray.clear()
        if (with) {
            for (i in 0..5) {
                imageArray.add(BitmapStore.getInstance().removeBgBitmap)
            }
        } else {
            for (i in 0..5) {
                imageArray.add(BitmapStore.getInstance().originBitmap)
            }
        }
        adapter.setNewData(imageArray)
    }

    private fun multiBgRecyclerView(bitmap: Bitmap) {
        for (i in 0..5) {
            imageArray.add(bitmap)
        }
        adapter = MultiBgAdapter(imageArray)
        binding.recyclerView.layoutManager = LinearLayoutManager(
            this, LinearLayoutManager.HORIZONTAL, false
        )
        binding.recyclerView.adapter = adapter
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this@SaveLicenceActivity)
    }

    private fun initDiscountPop() {
        isShownDiscount = true
        binding.rlDiscount.visibility = View.VISIBLE
        MobclickAgent.onEvent(this@SaveLicenceActivity,"shown_discount")
        discountPopView = XPopup.Builder(this)
            .asCustom(
                DiscountPop(this,
                    object : DiscountPop.Callback {
                        override fun onClick(v: View?) {
                            when (v!!.id) {
                                R.id.discount_pay -> {
                                    binding.discountChosen.isChecked = true
                                }
                            }
                        }
                    })
            )
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (KeyEvent.KEYCODE_BACK == keyCode) {
            if (isShownDiscount) {
                onBackPressed()
                return true
            } else {
                //第二次进来，给个优惠卷
                if (LastClickRecord.getInstance().saveLicenceTimes >= if (
                        kv.decodeBool("firstDiscount",true)
                ) 2 else 3) {
                    kv.encode("firstDiscount",false)
                    initDiscountPop()
                    discountPopView.show()
                    return false
                }
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    companion object {
        fun start(context: Context) {
            context.startActivity(
                Intent(context, SaveLicenceActivity::class.java)
            )
        }
    }
}

class MultiBgAdapter(sizeData: List<Bitmap>) : BaseQuickAdapter<Bitmap, BaseViewHolder>
    (R.layout.grid_items, sizeData) {

    override fun convert(helper: BaseViewHolder?, item: Bitmap?) {
        helper?.setImageBitmap(R.id.iv_images, item!!)
        helper?.setBackgroundRes(
            R.id.iv_images,
            when (helper.adapterPosition) {
                0 -> R.drawable.blue_background
                1 -> R.drawable.red_background
                2 -> R.drawable.white_background
                3 -> R.drawable.blue_transparent_background
                4 -> R.drawable.red_transparent_background
                else -> R.drawable.grey_transparent_background
            }
        )
    }
}