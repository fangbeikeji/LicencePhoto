package com.fbkj.licencephoto.ui.mine

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.fbkj.licencephoto.R
import com.fbkj.licencephoto.config.Event
import com.fbkj.licencephoto.databinding.OrderDetailActivityBinding
import com.fbkj.licencephoto.local.LastClickRecord
import com.fbkj.licencephoto.model.EventModel
import com.fbkj.licencephoto.model.NewOrder
import com.fbkj.licencephoto.orderfragment.EleOrderViewModel
import com.fbkj.licencephoto.utils.AmountUtil
import com.fbkj.licencephoto.utils.TimeHelper
import com.fbkj.licencephoto.utils.ZfbCallBack
import com.fbkj.licencephoto.utils.toast
import com.tencent.mm.opensdk.modelpay.PayReq
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class OrderDetailActivity : AppCompatActivity() {

    private var _binding: OrderDetailActivityBinding? = null
    private val binding get() = _binding!!
    private lateinit var orderDetails: NewOrder
    private lateinit var viewModelEle: EleOrderViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = OrderDetailActivityBinding.inflate(layoutInflater)
        EventBus.getDefault().register(this@OrderDetailActivity)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
        binding.toolbarTxt.text = "订单详情"
        orderDetails = intent.getParcelableExtra("orderDetails")!!
        viewModelEle = ViewModelProvider(this)[EleOrderViewModel::class.java]
        initViews()

    }

    private fun initViews() {
        Glide.with(this).load(orderDetails.photo).into(binding.ivImage)

        binding.ivImage.setBackgroundResource(
            when (orderDetails.photoBackground) {
                "蓝底" -> R.drawable.blue_background
                "红底" -> R.drawable.red_background
                "白底" -> R.drawable.white_background
                "蓝渐白底" -> R.drawable.blue_transparent_background
                "红渐白底" -> R.drawable.red_transparent_background
                else -> R.drawable.grey_transparent_background
            }
        )

        binding.size.text = orderDetails.photoSize
        binding.pixel.text = orderDetails.photoPixel
        binding.createTime.text = TimeHelper.parseISOTimeString(orderDetails.createAt)
        binding.inch.text = orderDetails.photoSpecification
        binding.background.text = if (orderDetails.multibgs) "多底色（6种）" else orderDetails.photoBackground
        binding.amount.text = AmountUtil.getInstance().changeF2Y(orderDetails.amount.toLong())

        binding.status.apply {
            when (orderDetails.status) {
                -1 -> {
                    text = "未支付"
                    setTextColor(resources.getColor(R.color.red))
                }
                0 -> {
                    text = "未支付"
                    setTextColor(resources.getColor(R.color.red))
                }
                1 -> {
                    text = "已支付"
                    setTextColor(resources.getColor(R.color.green))
                }
                else -> {
                    text = "已过期"
                    setTextColor(resources.getColor(R.color.transparent_80))
                }
            }
        }

        binding.orderNo.text = orderDetails.outTradeNo
        if (orderDetails.address != null && orderDetails.type == 2) {
            binding.addressee.text = orderDetails.address?.contactName
            binding.address.text = orderDetails.address?.address
            binding.expressCompany.text = orderDetails.expressCompany
        } else {
            binding.llOrderTag2.visibility = View.GONE
            binding.llOrderTag3.visibility = View.GONE
            binding.llOrderTag4.visibility = View.GONE
            binding.llOrderTag5.visibility = View.GONE
        }

        binding.copyOrderNo.setOnClickListener {
            (this.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager)
                .setPrimaryClip(ClipData.newPlainText(null, binding.orderNo.text))
            toast("已复制订单号")
        }

        binding.copyExpressNo.setOnClickListener {
            (this.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager)
                .setPrimaryClip(ClipData.newPlainText(null, binding.expressNo.text))
            toast("已复制快递号")
        }

        binding.tvBack.setOnClickListener { finish() }

        binding.tvRePay.setOnClickListener {
            LastClickRecord.getInstance().apply {
                isOrder = true
                onDiscountOrder = true
                isOrderData = orderDetails
            }
            if (orderDetails.payPlatform == 1) {//微信重新支付
                viewModelEle.wxRePay(orderDetails.id)
            } else {//支付宝重新支付
                viewModelEle.zfbRePay(orderDetails.id)
            }
        }

        //获取订单后信息后挑起微信支付
        viewModelEle.wxReCallBackData.observe(this@OrderDetailActivity) {
            val api = WXAPIFactory.createWXAPI(this@OrderDetailActivity, it.params.appid)
            if (!api.isWXAppInstalled) {
                toast("您尚未安装微信客户端")
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

        //获取订单后信息后挑起支付宝支付
        viewModelEle.zfbReCallBackData.observe(this@OrderDetailActivity) {
            lifecycleScope.launch {
                ZfbCallBack.getInstance().pay(this@OrderDetailActivity, it.params!!)
                LastClickRecord.getInstance().tradNo = it.trade_no
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this@OrderDetailActivity)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun eventData(message: EventModel) {
        when (message.message) {
            Event.ON_PAY_FINISH -> {
               finish()
            }
        }
    }

    companion object {
        fun start(context: Context, orderDetails: NewOrder) {
            context.startActivity(Intent(context, OrderDetailActivity::class.java).apply {
                putExtra("orderDetails", orderDetails)
            })
        }
    }
}