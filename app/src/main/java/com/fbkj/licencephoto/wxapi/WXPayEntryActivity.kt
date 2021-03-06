package com.fbkj.licencephoto.wxapi

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.fbkj.licencephoto.R
import com.fbkj.licencephoto.config.Constants
import com.fbkj.licencephoto.config.Event.ON_PAY_FINISH
import com.fbkj.licencephoto.config.Event.ON_ZFB_REORDER
import com.fbkj.licencephoto.databinding.ActivityPaySuceessBinding
import com.fbkj.licencephoto.dialogs.DiscountPop
import com.fbkj.licencephoto.local.LastClickRecord
import com.fbkj.licencephoto.model.EventModel
import com.fbkj.licencephoto.ui.DownLoadPageActivity
import com.fbkj.licencephoto.ui.mine.MyOrderActivity
import com.fbkj.licencephoto.ui.logic.OrderCommitViewModel
import com.fbkj.licencephoto.utils.*
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BasePopupView
import com.tencent.mm.opensdk.modelbase.BaseReq
import com.tencent.mm.opensdk.modelbase.BaseResp
import com.tencent.mm.opensdk.modelpay.PayReq
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import com.umeng.analytics.MobclickAgent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class WXPayEntryActivity : AppCompatActivity(), IWXAPIEventHandler {

    private var _binding: ActivityPaySuceessBinding? = null
    private val binding get() = _binding!!
    private var iwxapi: IWXAPI? = null
    private var onPaySuccess: Boolean = false
    private lateinit var discountPopView: BasePopupView
    private lateinit var viewModel: OrderCommitViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // ????????????App???????????????????????????
        _binding = ActivityPaySuceessBinding.inflate(layoutInflater)
        setContentView(binding.root)
        EventBus.getDefault().register(this@WXPayEntryActivity)
        viewModel = ViewModelProvider(this)[OrderCommitViewModel::class.java]
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbar.setNavigationOnClickListener {
            backPress()
        }
        binding.toolbarTxt.text = "????????????"

        LastClickRecord.getInstance().saveLicenceTimes = 0
        iwxapi = WXAPIFactory.createWXAPI(this, Constants.WX_APP_ID, true)
        iwxapi?.registerApp(Constants.WX_APP_ID)
        iwxapi?.handleIntent(intent, this)

        //??????????????????????????????????????????
        viewModel.wxReCallBackData.observe(this) {
            val api = WXAPIFactory.createWXAPI(this, it.params.appid)
            if (!api.isWXAppInstalled) {
                Toast.makeText(this, "??????????????????????????????", Toast.LENGTH_SHORT).show()
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

        //?????????????????????????????????????????????
        viewModel.zfbReCallBackData.observe(this) {
            lifecycleScope.launch {
                ZfbCallBack.getInstance().pay(this@WXPayEntryActivity, it.params!!)
                LastClickRecord.getInstance().tradNo = it.trade_no
            }
        }

        //??????????????????????????????????????????
        viewModel.wxOrderCallBackSuccessData.observe(this) {
            //????????????????????????
            binding.discountChosen.isChecked = false
            val api = WXAPIFactory.createWXAPI(this@WXPayEntryActivity, it.params.appid)
            if (!api.isWXAppInstalled) {
                toast("????????????????????????????????????????????????????????????????????????")
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
            //????????????????????????
            binding.discountChosen.isChecked = false
            binding.rlDiscount.visibility = View.GONE
            LastClickRecord.getInstance().tradNo = it.trade_no
            lifecycleScope.launch {
                ZfbCallBack.getInstance().pay(this@WXPayEntryActivity, it.params!!)
            }
        }

        //???????????????
        if (intent.getBooleanExtra("isZfb", false)) {
            //????????????
            if (intent.getBooleanExtra("isZfbSuccess", true)) {
                onPaySuccess = true
                viewModel.orderZfbQuery(LastClickRecord.getInstance().tradNo)
                binding.ivPaySuccess.setImageResource(R.mipmap.ic_pay_success)
                binding.tvRePay.visibility = View.GONE
                MobclickAgent.onEvent(this, "zfb_pay_successed")
            } else {
                //????????????
                binding.ivPaySuccess.setImageResource(R.mipmap.ic_pay_fail)
                binding.tvPayMsg.text = "????????????"

                //???????????????????????????????????????????????????,?????????????????????????????????
                if (LastClickRecord.getInstance().onDiscountOrder) {
                    binding.rlDiscount.visibility = View.GONE
                    binding.discountChosen.isChecked = false
                } else {
                    binding.rlDiscount.visibility = View.VISIBLE
                    initDiscountPop()
                }

                if (LastClickRecord.getInstance().isOrder) {
                    //??????????????????????????????????????????
                    binding.rlDiscount.visibility = View.GONE

                    LastClickRecord.getInstance().isOrderData.apply {
                        binding.orderCode.text = this.outTradeNo
                        binding.createTime.text = TimeHelper.parseISOTimeString(this.createAt)
                        binding.amount.text =
                            AmountUtil.getInstance().changeF2Y(this.amount.toLong())
                    }

                    binding.tvRePay.setOnClickListener {
                        viewModel.zfbRePay(LastClickRecord.getInstance().isOrderData.id)
                    }
                } else {
                    //????????????????????????
                    binding.tvRePay.setOnClickListener {
                        if (binding.discountChosen.isChecked) {
                            discountPay()
                        } else {
                            viewModel.zfbRePay(LastClickRecord.getInstance().order.id)
                        }
                    }

                    LastClickRecord.getInstance().order.apply {
                        binding.orderCode.text = this.outTradeNo
                        binding.createTime.text = TimeHelper.parseISOTimeString(this.createAt)
                        binding.amount.text =
                            AmountUtil.getInstance().changeF2Y(this.amount.toLong())
                    }
                }
                MobclickAgent.onEvent(this, "order_pay_cancle")
            }
        }

        viewModel.allPaidOrderCallBackData.observe(this) {
            binding.orderCode.text = LastClickRecord.getInstance().tradNo
            binding.createTime.text = TimeHelper.parseISOTimeString(it.success_time)
            binding.amount.text = AmountUtil.getInstance().changeF2Y(it.amount.toLong())
        }

        binding.discountChosen.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.discountChosen.setBackgroundResource(R.mipmap.ic_address_on)
            } else {
                binding.discountChosen.setBackgroundResource(R.mipmap.ic_address_off)
            }
        }

        binding.copyCode.setOnClickListener {
            (this.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager)
                .setPrimaryClip(ClipData.newPlainText(null, binding.orderCode.text))
            toast("?????????")
            LastClickRecord.getInstance().tradNo
        }

        binding.tvBack.setOnClickListener {
            backPress()
        }
    }

    private fun backPress(){
        EventBus.getDefault().post(EventModel(0, ON_PAY_FINISH))//?????????
        if (LastClickRecord.getInstance().isOrder) {
            this@WXPayEntryActivity.finish()
        } else {
            if (onPaySuccess){
                DownLoadPageActivity.start(
                    this,
                    LastClickRecord.getInstance().order.photoRembg,
                    LastClickRecord.getInstance().order.multibgs,
                    LastClickRecord.getInstance().order.photoBackground,
                    true
                )
            }else{
                MyOrderActivity.start(this, LastClickRecord.getInstance().orderType)
            }
            this@WXPayEntryActivity.finish()
        }
        removeBitmapCache()
    }

    private fun removeBitmapCache(){
        LastClickRecord.getInstance().onDiscountOrder = false
    }

    private fun discountPay() {
        //?????????????????????
        LastClickRecord.getInstance().apply {
            //???????????????????????????????????????
            onDiscountOrder = true
            //???????????????
            viewModel.userOrderCommit(
                if (order.multibgs) 1 else 0,
                order.type,//1?????????,2??????
                isBeauty,//??????????????????
                if (order.type == 1) 0 else flushNum,//?????????
                order.photoSize,
                "???????????????(???????????????)",
                order.photoSpecification,
                order.photoPixel,
                background,
                if (order.type == 1) 0 else order.addressId,//2?????????????????????
                finalBitmapWithBg,
                finalBitmapWithoutBg,
                if (order.payPlatform == 1) 0 else 1,//?????????0?????????1????????????
                if (binding.discountChosen.isChecked) 1 else 0 //??????
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        iwxapi?.handleIntent(intent, this)
    }

    override fun onReq(baseReq: BaseReq) {

    }

    /**
     * ??????????????????
     */
    override fun onResp(baseResp: BaseResp) {
        when (baseResp.errCode) {
            0 -> {
                onPaySuccess = true
                binding.tvPayMsg.text = "????????????"
                binding.tvRePay.visibility = View.GONE
                MobclickAgent.onEvent(this, "wx_pay_successed")
                binding.ivPaySuccess.setImageResource(R.mipmap.ic_pay_success)
                viewModel.orderWxQuery(LastClickRecord.getInstance().tradNo)
            }
            -1 -> {
                //??????????????????????????????????????????????????????
                binding.tvPayMsg.text = "????????????"
                LastClickRecord.getInstance().order.apply {
                    binding.orderCode.text = this.outTradeNo
                    binding.createTime.text = TimeHelper.parseISOTimeString(this.createAt)
                    binding.amount.text = AmountUtil.getInstance().changeF2Y(this.amount.toLong())
                }
                binding.rlMsgs.visibility = View.GONE
                binding.tvRePay.visibility = View.GONE
                binding.rlDiscount.visibility = View.GONE
                MobclickAgent.onEvent(this, "order_pay_cancle")
                binding.ivPaySuccess.setImageResource(R.mipmap.ic_pay_fail)
                toast("????????????")
            }
            -2 -> {
                binding.ivPaySuccess.setImageResource(R.mipmap.ic_pay_fail)
                binding.tvPayMsg.text = "????????????"
                //???????????????????????????????????????????????????
                if (LastClickRecord.getInstance().onDiscountOrder) {
                    binding.rlDiscount.visibility = View.GONE
                } else {
                    binding.rlDiscount.visibility = View.VISIBLE
                    initDiscountPop()
                }

                if (LastClickRecord.getInstance().isOrder) {
                    //??????????????????????????????????????????
                    binding.rlDiscount.visibility = View.GONE
                    LastClickRecord.getInstance().isOrderData.apply {
                        binding.orderCode.text = this.outTradeNo
                        binding.createTime.text = TimeHelper.parseISOTimeString(this.createAt)
                        binding.amount.text =
                            AmountUtil.getInstance().changeF2Y(this.amount.toLong())
                    }

                    //???????????????????????????
                    binding.tvRePay.setOnClickListener {
                        viewModel.wxRePay(LastClickRecord.getInstance().isOrderData.id)
                    }

                } else {
                    //??????????????????
                    LastClickRecord.getInstance().order.apply {
                        binding.orderCode.text = this.outTradeNo
                        binding.createTime.text = TimeHelper.parseISOTimeString(this.createAt)
                        binding.amount.text =
                            AmountUtil.getInstance().changeF2Y(this.amount.toLong())
                    }

                    //????????????
                    binding.tvRePay.setOnClickListener {
                        if (binding.discountChosen.isChecked) {
                            discountPay()
                        } else {
                            viewModel.wxRePay(LastClickRecord.getInstance().order.id)
                        }
                    }
                }
                toast("?????????????????????")
                MobclickAgent.onEvent(this, "order_pay_cancle")
            }
        }
    }

    private fun initDiscountPop() {
        MobclickAgent.onEvent(this@WXPayEntryActivity,"shown_discount")
        discountPopView = XPopup.Builder(this)
            .asCustom(
                DiscountPop(this,
                    object : DiscountPop.Callback {
                        override fun onClick(v: View?) {
                            when (v!!.id) {
                                R.id.discount_pay -> {
                                    binding.discountChosen.isChecked = true
                                    discountPay()
                                }
                            }
                        }
                    })
            ).show()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun eventData(message: String) {
        if (message == ON_ZFB_REORDER){
            this@WXPayEntryActivity.finish()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        backPress()
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this@WXPayEntryActivity)
    }

    companion object {
        fun start(context: Context?, isZfb: Boolean, isZfbSuccess: Boolean) {
            context?.startActivity(
                Intent(context, WXPayEntryActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    putExtra("isZfbSuccess", isZfbSuccess)
                    putExtra("isZfb", isZfb)
                })
        }
    }
}