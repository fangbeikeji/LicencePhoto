package com.fbkj.licencephoto.orderfragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.fbkj.licencephoto.R
import com.fbkj.licencephoto.config.Event
import com.fbkj.licencephoto.databinding.FragmentFlushOutBinding
import com.fbkj.licencephoto.local.LastClickRecord
import com.fbkj.licencephoto.model.EventModel
import com.fbkj.licencephoto.model.NewOrder
import com.fbkj.licencephoto.model.NormalData
import com.fbkj.licencephoto.ui.mine.OrderDetailActivity
import com.fbkj.licencephoto.utils.*
import com.lxj.xpopup.XPopup
import com.tencent.mm.opensdk.modelpay.PayReq
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import kotlinx.android.synthetic.main.my_order_flush_item.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class FlushOrderFragment : Fragment() {

    private var _binding: FragmentFlushOutBinding? = null
    private val binding get() = _binding!!
    private lateinit var orderViewModel: FlushOrderViewModel
    private var flushAdapter = FlushAdapter(arrayListOf())
    private var isDelete = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFlushOutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        orderViewModel = ViewModelProvider(this).get(FlushOrderViewModel::class.java)
        initView()
    }

    private fun initView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(
            requireContext(), LinearLayoutManager.VERTICAL, false
        )

        binding.recyclerView.adapter = flushAdapter

        orderViewModel.flushOrderList.observe(viewLifecycleOwner) {
            if (isDelete) {
                //无单显示空
//                binding.recyclerView.visibility =
//                    if (it.isEmpty()) View.GONE else View.VISIBLE
//                binding.emptyView.visibility =
//                    if (it.isEmpty()) View.VISIBLE else View.GONE
                flushAdapter.setNewData(it)
                flushAdapter.setEnableLoadMore(true)
                flushAdapter.loadMoreEnd(false)
            } else {
                flushAdapter.addData(it)
            }

        }

        binding.goPic.setOnClickListener {
            LastClickRecord.getInstance().recordSizeData(
                NormalData(
                    "295*413 px",
                    101,
                    "一寸",
                    "25*35 mm"
                )
            )
            RequirePermission.getInstance().openCameraOptions(requireActivity(), lifecycleScope)
        }

        flushAdapter.setOnItemClickListener { _, _, position ->
            OrderDetailActivity.start(requireContext(), flushAdapter.data[position])
        }

        flushAdapter.setOnItemChildClickListener { _, view, position ->
            when (view.id) {
                flush_go_pay.id -> {
                    LastClickRecord.getInstance().apply{
                        isOrder = true
                        onDiscountOrder = true
                        isOrderData = flushAdapter.data[position]
                    }
                    if (flushAdapter.data[position].payPlatform == 1) {//微信重新支付
                        orderViewModel.wxRePay(flushAdapter.data[position].id)
                    } else {//支付宝
                        orderViewModel.zfbRePay(flushAdapter.data[position].id)
                    }
                }
                tv_delete.id -> {
                    //删除当前订单
                    XPopup.Builder(requireContext()).asConfirm(
                        "删除", getString(R.string.delete_order)
                    ) {
                        orderViewModel.deleteOrder(flushAdapter.data[position].id)
                        flushAdapter.remove(position)
                        isDelete = true
                        flushAdapter.notifyDataSetChanged()
                    }.show()
                }
            }
        }

        //获取订单后信息后挑起微信支付
        orderViewModel.wxReCallBackData.observe(viewLifecycleOwner) {
            val api = WXAPIFactory.createWXAPI(requireContext(), it.params.appid)
            if (!api.isWXAppInstalled) {
                Toast.makeText(requireContext(), "您尚未安装微信客户端", Toast.LENGTH_SHORT).show()
                return@observe
            }
            GlobalScope.launch {
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

        orderViewModel.zfbReCallBackData.observe(viewLifecycleOwner) {
            lifecycleScope.launch {
                ZfbCallBack.getInstance().pay(requireActivity(), it.params!!)
                LastClickRecord.getInstance().tradNo = it.trade_no
            }
        }

        flushAdapter.setOnLoadMoreListener {
            isDelete = false
            orderViewModel.fetchOrder(false)
        }
        flushAdapter.setEnableLoadMore(true)

        orderViewModel.fetchOrder(false)
    }

    companion object {
        fun newInstance() = FlushOrderFragment()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun eventData(message: EventModel) {
        when (message.message) {
            Event.ON_PAY_FINISH -> {
                //刷新一遍订单数据
                orderViewModel.fetchOrder(true)
                isDelete = true
            }
        }
    }
}

class FlushAdapter(orderData: List<NewOrder>) :
    BaseQuickAdapter<NewOrder, BaseViewHolder>
        (R.layout.my_order_flush_item, orderData) {
    override fun convert(helper: BaseViewHolder?, item: NewOrder?) {

        Glide.with(mContext).load(item!!.photo)
            .into(helper?.itemView!!.findViewById(R.id.iv_order_flush_image))

        helper.setBackgroundRes(
            R.id.iv_order_flush_image,
            when (item.photoBackground) {
                "蓝底" -> R.drawable.blue_background
                "红底" -> R.drawable.red_background
                "白底" -> R.drawable.white_background
                "蓝渐白底" -> R.drawable.blue_transparent_background
                "红渐白底" -> R.drawable.red_transparent_background
                else -> R.drawable.grey_transparent_background
            }
        )

        helper.apply {
            setText(R.id.tv_size_flush, item.photoSize)
            setText(R.id.order_code_flush, item.outTradeNo)
            setText(
                R.id.create_time_flush,
                TimeHelper.getFriendlyTimeSpanByNow(TimeHelper.parseISODateTime(item.createAt))
            )
            setText(R.id.address_flush, item.address?.address)
            addOnClickListener(R.id.flush_go_pay)
            addOnClickListener(R.id.tv_delete)
        }

        //-1是还未回调，0是未支付，1是支付成功，2是关闭
        when (item.status) {
            -1 -> helper.setVisible(R.id.ll_not_paid, true)
            0 -> helper.setVisible(R.id.ll_not_paid, true)
            1 -> helper.setVisible(R.id.ll_not_paid, false)
        }
    }
}