package com.fbkj.licencephoto.ui.logic

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.fbkj.licencephoto.R
import com.fbkj.licencephoto.config.Constants
import com.fbkj.licencephoto.config.Event.ON_ADDRESS_CHANGED
import com.fbkj.licencephoto.config.Event.ON_PAY_FINISH
import com.fbkj.licencephoto.databinding.FlushPage2ActivityBinding
import com.fbkj.licencephoto.dialogs.DiscountPop
import com.fbkj.licencephoto.local.LastClickRecord
import com.fbkj.licencephoto.model.EventModel
import com.fbkj.licencephoto.ui.WebViewActivity
import com.fbkj.licencephoto.ui.address.AddressManagerActivity
import com.fbkj.licencephoto.utils.*
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BasePopupView
import com.tencent.mm.opensdk.modelpay.PayReq
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import com.tencent.mmkv.MMKV
import com.umeng.analytics.MobclickAgent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class FlushPageActivity : AppCompatActivity() {

    private var _binding: FlushPage2ActivityBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: OrderCommitViewModel
    private var count = 1
    private var flushSinglePrice = 0//单价
    private var flushCount = 0//折扣
    private var isShownDiscount = false
    private val kv = MMKV.defaultMMKV()
    private lateinit var finalBitmap: Bitmap
    private lateinit var discountPopView: BasePopupView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = FlushPage2ActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        EventBus.getDefault().register(this)
        setSupportActionBar(binding.toolbar)
        viewModel = ViewModelProvider(this)[OrderCommitViewModel::class.java]
        viewModel.flushPrice.observe(this) {
            flushSinglePrice = it.price
            flushCount = it.discount
            binding.tvCost.text =
                AmountUtil.getInstance().changeF2Y((count * flushSinglePrice).toLong())
        }
        viewModel.getFlushPrice()
        MobclickAgent.onEvent(this@FlushPageActivity, "flush_licence")
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbar.setNavigationOnClickListener {
            //第二次进来，给个优惠卷
            if (isShownDiscount) {
                onBackPressed()
            } else {
                if (LastClickRecord.getInstance().saveLicenceTimes >= if (
                        kv.decodeBool("firstDiscount",true)) 2 else 3) {
                    initDiscountPop()
                    discountPopView.show()
                } else {
                    onBackPressed()
                }
            }
        }
        binding.toolbarTxt.text = "冲印"

        initView()
    }

    private fun initView() {
        LastClickRecord.getInstance().apply {
            orderType = 1
            saveLicenceTimes++
            if (hasAddress()) {
                binding.tvAddressee.text = recordAddressData!!.contactName
                binding.tvPhone.text = recordAddressData!!.phone
                binding.tvAddress.text = recordAddressData!!.address
                binding.tvTag6.visibility = View.GONE
            }
        }

        binding.flushAddress.setOnClickListener { AddressManagerActivity.start(this) }

        binding.discountChosen.setOnCheckedChangeListener { _, isChecked ->
            binding.tvCost.text =
                if (isChecked) {
                    binding.discountChosen.setBackgroundResource(R.mipmap.ic_address_on)
                    "￥${
                        AmountUtil.getInstance()
                            .changeF2Y((count * flushSinglePrice - 300).toLong())
                    }"
                } else {
                    binding.discountChosen.setBackgroundResource(R.mipmap.ic_address_off)
                    "￥${AmountUtil.getInstance().changeF2Y((count * flushSinglePrice).toLong())}"
                }
        }

        if (intent.getBooleanExtra("isOrder", false)) {
            Glide.with(this)
                .load(BitmapStore.getInstance().currentOrderPic)
                .into(binding.ivImage)
            viewModel.netBitmap.observe(this) {
                finalBitmap = it
            }
            LastClickRecord.getInstance().apply {
                binding.size.text = recordOrderData?.photoSize
                binding.pixel.text = recordOrderData?.photoPixel
                binding.inch.text = recordOrderData?.photoSpecification
                binding.background.text = recordOrderData?.photoBackground
            }

            viewModel.netPic2Bitmap(BitmapStore.getInstance().currentOrderPic)
        } else {
            finalBitmap = BitmapStore.getInstance().beautyBitmap
            binding.ivImage.setImageBitmap(finalBitmap)
            LastClickRecord.getInstance().apply {
                binding.size.text = recordSizeData!!.sizeName
                binding.pixel.text = recordSizeData!!.sizePixel
                binding.inch.text = recordSizeData!!.sizeUnit
                binding.background.text = background
            }
        }

        binding.ivImage.background = when (LastClickRecord.getInstance().background) {
            "蓝底" -> resources.getDrawable(R.drawable.blue_background)
            "红底" -> resources.getDrawable(R.drawable.red_background)
            "白底" -> resources.getDrawable(R.drawable.white_background)
            "蓝渐白底" -> resources.getDrawable(R.drawable.blue_transparent_background)
            "红渐白底" -> resources.getDrawable(R.drawable.red_transparent_background)
            else -> resources.getDrawable(R.drawable.grey_transparent_background)
        }

        binding.ivReduce.setOnClickListener {
            if (count <= 1) {
                toast("冲印最少张为1版")
                return@setOnClickListener
            }
            count--
            binding.count.text = "$count"
            binding.tvCost.text =
                if (binding.discountChosen.isChecked) {
                    AmountUtil.getInstance().changeF2Y((count * flushSinglePrice - 300).toLong())
                } else {
                    AmountUtil.getInstance().changeF2Y((count * flushSinglePrice).toLong())
                }
        }
        binding.ivPlus.setOnClickListener {
            count++
            binding.count.text = "$count"
            binding.tvCost.text =
                if (binding.discountChosen.isChecked) {
                    AmountUtil.getInstance().changeF2Y((count * flushSinglePrice - 300).toLong())
                } else {
                    AmountUtil.getInstance().changeF2Y((count * flushSinglePrice).toLong())
                }
        }

        binding.wxPay.setOnClickListener {
            if (binding.tvAddressee.text.isEmpty() || binding.tvAddress.text.isEmpty()) {
                toast("请添加收件地址")
                return@setOnClickListener
            }
            orderPay(0)
            MobclickAgent.onEvent(this@FlushPageActivity, "wx_pay")
        }

        binding.zfbPay.setOnClickListener {
            if (binding.tvAddressee.text.isEmpty() || binding.tvAddress.text.isEmpty()) {
                toast("请添加收件地址")
                return@setOnClickListener
            }
            orderPay(1)
            MobclickAgent.onEvent(this@FlushPageActivity, "zfb_pay")
        }

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

        viewModel.wxOrderCallBackSuccessData.observe(this) {
            binding.rlProgressBar.visibility = View.GONE
            val api = WXAPIFactory.createWXAPI(this@FlushPageActivity, it.params.appid)

            if (!api.isWXAppInstalled) {
                Toast.makeText(this@FlushPageActivity, "您尚未安装微信客户端", Toast.LENGTH_SHORT).show()
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
            binding.rlProgressBar.visibility = View.GONE
            LastClickRecord.getInstance().tradNo = it.trade_no
            lifecycleScope.launch {
                ZfbCallBack.getInstance().pay(this@FlushPageActivity, it.params!!)
            }
        }
    }

    private fun orderPay(payType: Int) {
        binding.rlProgressBar.visibility = View.VISIBLE
        //是订单点进来冲印的
        if (intent.getBooleanExtra("isOrder", false)) {
            viewModel.userOrderCommit(
                0,//冲印默认单背景
                2,//type2冲印
                1,//美颜换装
                count,//冲印数
                LastClickRecord.getInstance().recordOrderData!!.photoSize,
                "冲印版订单",
                LastClickRecord.getInstance().recordOrderData!!.photoSpecification,
                LastClickRecord.getInstance().recordOrderData!!.photoPixel,
                LastClickRecord.getInstance().recordOrderData!!.photoBackground,
                LastClickRecord.getInstance().recordAddressData!!.id,
                finalBitmap,
                finalBitmap,
                payType,//0为微信支付
                if (binding.discountChosen.isChecked) 1 else 0
            )
        } else {
            LastClickRecord.getInstance().recordAddressData!!.id
            viewModel.userOrderCommit(
                0, //冲印默认单背景
                2, //type2冲印
                1, //美颜换装
                count,//冲印数
                LastClickRecord.getInstance().recordSizeData!!.sizeName,
                "冲印版订单",
                LastClickRecord.getInstance().recordSizeData!!.sizeUnit,
                LastClickRecord.getInstance().recordSizeData!!.sizePixel,
                LastClickRecord.getInstance().background,
                LastClickRecord.getInstance().recordAddressData!!.id,
                BitmapStore.getInstance().beautyBitmap,
                BitmapStore.getInstance().removeBgBitmap,
                payType,//1为支付宝支付
                if (binding.discountChosen.isChecked) 1 else 0
            )
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun eventData(message: EventModel) {
        when (message.message) {
            ON_ADDRESS_CHANGED -> {
                binding.tvTag6.visibility = View.GONE
                binding.tvAddressee.text =
                    LastClickRecord.getInstance().recordAddressData!!.contactName
                binding.tvPhone.text = LastClickRecord.getInstance().recordAddressData!!.phone
                binding.tvAddress.text = LastClickRecord.getInstance().recordAddressData!!.address
            }
            ON_PAY_FINISH -> {
                this@FlushPageActivity.finish()
            }
        }
    }

    private fun initDiscountPop() {
        isShownDiscount = true
        binding.rlDiscount.visibility = View.VISIBLE
        MobclickAgent.onEvent(this@FlushPageActivity,"shown_discount")
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
                if (LastClickRecord.getInstance().saveLicenceTimes >= if (
                        kv.decodeBool(
                            "firstDiscount",
                            true))
                                2 else 3) {//第二次进来，给个优惠卷
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
        fun start(context: Context, isOrder: Boolean, tradNo: String) {
            context.startActivity(Intent(context, FlushPageActivity::class.java).apply {
                putExtra("isOrder", isOrder)
                putExtra("tradNo", tradNo)
            })
        }
    }
}