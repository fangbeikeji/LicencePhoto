package com.fbkj.licencephoto.orderfragment

import android.os.Bundle
import android.view.*
import android.widget.PopupMenu
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
import com.fbkj.licencephoto.config.Event.ON_ADDRESS_CHANGED
import com.fbkj.licencephoto.databinding.FragmentElectronVersionBinding
import com.fbkj.licencephoto.dialogs.AddressDialog
import com.fbkj.licencephoto.local.LastClickRecord
import com.fbkj.licencephoto.model.*
import com.fbkj.licencephoto.ui.DownLoadPageActivity
import com.fbkj.licencephoto.ui.logic.FlushPageActivity
import com.fbkj.licencephoto.ui.mine.OrderDetailActivity
import com.fbkj.licencephoto.ui.complain.ComplainActivity
import com.fbkj.licencephoto.utils.*
import com.lxj.xpopup.XPopup
import com.tencent.mm.opensdk.modelpay.PayReq
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import kotlinx.android.synthetic.main.my_ele_order_item.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

class ElectronOrderFragment : Fragment() {

    private var _binding: FragmentElectronVersionBinding? = null
    private val binding get() = _binding!!
    private var electronAdapter = ElectronAdapter(arrayListOf())
    private lateinit var addressDialog: AddressDialog
    private lateinit var viewModelEle: EleOrderViewModel
    private var isDelete = false

    companion object {
        fun newInstance() = ElectronOrderFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentElectronVersionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModelEle = ViewModelProvider(this).get(EleOrderViewModel::class.java)
        initView()
    }

    private fun initView() {
        addressDialog = AddressDialog(requireContext())

        binding.recyclerView.layoutManager = LinearLayoutManager(
            requireContext(), LinearLayoutManager.VERTICAL, false
        )
        binding.recyclerView.adapter = electronAdapter

        viewModelEle.orderList.observe(viewLifecycleOwner) {
            if (isDelete) {
                //无单显示空
//                binding.recyclerView.visibility =
//                    if (it.isEmpty()) View.GONE else View.VISIBLE
//                binding.emptyView.visibility =
//                    if (it.isEmpty()) View.VISIBLE else View.GONE
                electronAdapter.setNewData(it)
                electronAdapter.setEnableLoadMore(true)
                electronAdapter.loadMoreEnd(false)
            } else {
                electronAdapter.addData(it)
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

        electronAdapter.setOnItemClickListener { _, _, position ->
            OrderDetailActivity.start(requireContext(), electronAdapter.data[position])
        }

        electronAdapter.setOnItemChildClickListener { _, view, position ->
            when (view.id) {
                //下载
                ele_download.id -> {
                    if (electronAdapter.data[position].multibgs) {
                        DownLoadPageActivity.start(
                            requireContext(),
                            electronAdapter.data[position].photoRembg,
                            true,
                            electronAdapter.data[position].photoBackground,
                            false
                        )
                    } else {
                        DownLoadPageActivity.start(
                            requireContext(),
                            electronAdapter.data[position].photo,
                            false,
                            electronAdapter.data[position].photoBackground,
                            false
                        )
                    }
                }
                //冲印
                ele_flush_out.id -> {
                    LastClickRecord.getInstance().isOrder = true
                    LastClickRecord.getInstance().recordOrderData =
                        electronAdapter.data[position]
                    BitmapStore.getInstance().currentOrderPic =
                        electronAdapter.data[position].photo
                    FlushPageActivity.start(
                        requireContext(),
                        true, electronAdapter.data[position].outTradeNo
                    )
//                    MobclickAgent.onEvent(requireContext(),"flush_licence")
                }
                //去支付
                ele_go_pay.id -> {
                    LastClickRecord.getInstance().apply{
                        isOrder = true
                        onDiscountOrder = true
                        isOrderData = electronAdapter.data[position]
                    }
                    if (electronAdapter.data[position].payPlatform == 1) {//微信重新支付
                        viewModelEle.wxRePay(electronAdapter.data[position].id)
                    } else {//支付宝
                        viewModelEle.zfbRePay(electronAdapter.data[position].id)
                    }
                }

                contact_us.id ->{
                    ComplainActivity.start(//
                        requireContext(),2,electronAdapter.data[position])
                }

                //更多选择
                more_options.id -> {
                    showPopup(more_options).setOnMenuItemClickListener(object:MenuItem.OnMenuItemClickListener,
                        PopupMenu.OnMenuItemClickListener {
                        override fun onMenuItemClick(p0: MenuItem?): Boolean {
                            when(p0!!.itemId){
                                R.id.menu_delete->{
                                    //删除当前订单
                                    XPopup.Builder(requireContext()).asConfirm(
                                        "删除", getString(R.string.delete_order)
                                    ) {
                                        viewModelEle.deleteOrder(electronAdapter.data[position].id)
                                        viewModelEle.elePage = 0
                                        electronAdapter.remove(position)
                                        //viewModel.fetchOrder(true)
                                        electronAdapter.notifyDataSetChanged()
                                        isDelete = true
                                    }.show()
                                }
                                R.id.menu_payback->{
                                    ComplainActivity.start(
                                        requireContext(),1,electronAdapter.data[position])
                                }
                            }
                            return true
                        }
                    })
                }
            }
        }

        electronAdapter.setOnLoadMoreListener {
            isDelete = false
            viewModelEle.fetchOrder(false)
        }

        electronAdapter.setEnableLoadMore(true)

        //获取订单后信息后挑起微信支付
        viewModelEle.wxReCallBackData.observe(viewLifecycleOwner) {
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

        viewModelEle.zfbReCallBackData.observe(viewLifecycleOwner) {
            lifecycleScope.launch {
                ZfbCallBack.getInstance().pay(requireActivity(), it.params!!)
                LastClickRecord.getInstance().tradNo = it.trade_no
            }
        }
        viewModelEle.fetchOrder(false)
    }

    private fun showPopup(v: View) :PopupMenu{
        val popup = PopupMenu(requireContext(), v)
        val inflater: MenuInflater = popup.menuInflater
        inflater.inflate(R.menu.more_option_menu, popup.menu)
        popup.show()
        return popup
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun eventData(message: EventModel) {
        when (message.message) {
            ON_ADDRESS_CHANGED -> {
                LastClickRecord.getInstance().recordAddressData.apply {
                    addressDialog.name = this!!.contactName
                    addressDialog.phone = this.phone
                    addressDialog.address = this.address
                }
            }
            Event.ON_PAY_FINISH -> {
                //刷新一遍订单数据
                isDelete = true
                viewModelEle.fetchOrder(true)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }
}

class ElectronAdapter(orderData: List<NewOrder>) :
    BaseQuickAdapter<NewOrder, BaseViewHolder>
        (R.layout.my_ele_order_item, orderData) {
    override fun convert(helper: BaseViewHolder?, item: NewOrder?) {

        Glide.with(mContext).load(item?.photo)
            .into(helper?.getView(R.id.iv_ele_order_image)!!)
        helper.setBackgroundRes(
            R.id.iv_ele_order_image,
            when (item!!.photoBackground) {
                "蓝底" -> R.drawable.blue_background
                "红底" -> R.drawable.red_background
                "白底" -> R.drawable.white_background
                "蓝渐白底" -> R.drawable.blue_transparent_background
                "红渐白底" -> R.drawable.red_transparent_background
                else -> R.drawable.grey_transparent_background
            }
        )

        helper.apply {
            setText(R.id.ele_size, item.photoSize)
            setText(R.id.ele_order_code, item.outTradeNo)
            setText(
                R.id.ele_create_time,
                TimeHelper.getFriendlyTimeSpanByNow(TimeHelper.parseISODateTime(item.createAt))
            )
            if (item.multibgs) {
                setText(R.id.ele_background, "多底色")
            } else {
                setText(R.id.ele_background, item.photoBackground)
            }
            addOnClickListener(R.id.ele_download)
            addOnClickListener(R.id.ele_flush_out)
            addOnClickListener(R.id.more_options)
            addOnClickListener(R.id.ele_go_pay)
            addOnClickListener(R.id.contact_us)
        }

//        helper.setVisible(R.id.ll_paid, true)
//        helper.setVisible(R.id.more_options, true)//投诉退款
        //-1是还未回调，0是未支付，1是支付成功，2是关闭
        when (item.status) {
            -1 -> {
                helper.setVisible(R.id.ll_paid, false)
                helper.setVisible(R.id.ll_not_paid, true)
            }
            0 -> {
                helper.setVisible(R.id.ll_paid, false)
                helper.setVisible(R.id.ll_not_paid, true)
            }
            1 -> {
                helper.setVisible(R.id.ll_paid, true)
                helper.setVisible(R.id.more_options, true)//投诉退款
                helper.setVisible(R.id.ll_not_paid, false)
            }
        }
    }
}