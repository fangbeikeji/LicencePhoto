package com.fbkj.licencephoto.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.fbkj.licencephoto.R
import com.fbkj.licencephoto.databinding.MyLicenceActivityBinding
import com.fbkj.licencephoto.local.LastClickRecord
import com.fbkj.licencephoto.model.*
import com.fbkj.licencephoto.orderfragment.EleOrderViewModel
import com.fbkj.licencephoto.ui.complain.ComplainActivity
import com.fbkj.licencephoto.ui.logic.FlushPageActivity
import com.fbkj.licencephoto.ui.mine.OrderDetailActivity
import com.fbkj.licencephoto.utils.*
import com.lxj.xpopup.XPopup
import kotlinx.android.synthetic.main.my_ele_order_item.*
import kotlinx.android.synthetic.main.my_licence_item.*
import kotlinx.android.synthetic.main.my_licence_item.more_options

class MyLicenceActivity : AppCompatActivity() {

    private var _binding: MyLicenceActivityBinding? = null
    private val binding get() = _binding!!
    private var myLicenceAdapter = MyLicenceAdapter(arrayListOf())
    private lateinit var viewModelEle: EleOrderViewModel
    private var isDelete = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = MyLicenceActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        viewModelEle = ViewModelProvider(this)[EleOrderViewModel::class.java]
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
        binding.toolbarTxt.text = "我的证件照"

        binding.recyclerView.layoutManager = LinearLayoutManager(
            this, LinearLayoutManager.VERTICAL, false
        )

        binding.recyclerView.adapter = myLicenceAdapter

        viewModelEle.orderList.observe(this) {
            if (isDelete) {
                //无单显示空
//                binding.recyclerView.visibility =
//                    if (it.isEmpty()) View.GONE else View.VISIBLE
//                binding.emptyView.visibility =
//                    if (it.isEmpty()) View.VISIBLE else View.GONE
                myLicenceAdapter.setNewData(it)
                myLicenceAdapter.setEnableLoadMore(true)
                myLicenceAdapter.loadMoreEnd(false)
            } else {
                myLicenceAdapter.addData(it)
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
            RequirePermission.getInstance().openCameraOptions(
                this@MyLicenceActivity, lifecycleScope
            )
        }

        myLicenceAdapter.setOnItemClickListener { _, _, position ->
            OrderDetailActivity.start(this@MyLicenceActivity, myLicenceAdapter.data[position])
        }

        myLicenceAdapter.setOnItemChildClickListener { _, view, position ->
            when (view.id) {
                my_download.id -> {
                    if (myLicenceAdapter.data[position].multibgs) {
                        DownLoadPageActivity.start(
                            this@MyLicenceActivity,
                            myLicenceAdapter.data[position].photoRembg,
                            true,
                            myLicenceAdapter.data[position].photoBackground,
                            false
                        )

                    } else {
                        DownLoadPageActivity.start(
                            this@MyLicenceActivity,
                            myLicenceAdapter.data[position].photo,
                            false,
                            myLicenceAdapter.data[position].photoBackground,
                            false
                        )
//                        viewModel.download(requireContext(), electronAdapter.data[position].photo)
                        toast("已保存至手机相册")
                    }
                }
                //更多选择
                more_options.id -> {
                    showPopup(more_options).setOnMenuItemClickListener(object: MenuItem.OnMenuItemClickListener,
                        PopupMenu.OnMenuItemClickListener {
                        override fun onMenuItemClick(p0: MenuItem?): Boolean {
                            when(p0!!.itemId){
                                R.id.menu_delete->{
                                    //删除当前订单
                                    XPopup.Builder(this@MyLicenceActivity).asConfirm(
                                        "删除", getString(R.string.delete_order)
                                    ) {
                                        viewModelEle.deleteOrder(myLicenceAdapter.data[position].id)
                                        myLicenceAdapter.remove(position)
                                        isDelete = true
                                        myLicenceAdapter.notifyDataSetChanged()
                                    }.show()
                                }
                                R.id.menu_payback->{
                                    ComplainActivity.start(
                                        this@MyLicenceActivity,1,myLicenceAdapter.data[position])
                                }
                            }
                            return true
                        }
                    })
                }

                //冲印
                my_flush_out.id -> {
                    LastClickRecord.getInstance().isOrder = true
                    LastClickRecord.getInstance().recordOrderData =
                        myLicenceAdapter.data[position]
                    BitmapStore.getInstance().currentOrderPic =
                        myLicenceAdapter.data[position].photo
                    FlushPageActivity.start(
                        this,
                        true, myLicenceAdapter.data[position].outTradeNo
                    )
                }
            }
        }

        myLicenceAdapter.setOnLoadMoreListener {
            isDelete = false
            viewModelEle.fetchAllFinishedOrder(false)
        }

        myLicenceAdapter.setEnableLoadMore(true)

        viewModelEle.fetchAllFinishedOrder(false)
    }

    private fun showPopup(v: View) :PopupMenu{
        val popup = PopupMenu(this@MyLicenceActivity, v)
        val inflater: MenuInflater = popup.menuInflater
        inflater.inflate(R.menu.more_option_menu, popup.menu)
        popup.show()
        return popup
    }

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, MyLicenceActivity::class.java))
        }
    }
}

class MyLicenceAdapter(orderData: List<NewOrder>) :
    BaseQuickAdapter<NewOrder, BaseViewHolder>
        (R.layout.my_licence_item, orderData) {
    override fun convert(helper: BaseViewHolder?, item: NewOrder?) {
        //都是支付成功的

        Glide.with(mContext).load(item!!.photo)
            .into(helper?.itemView!!.findViewById(R.id.my_order_image))
        helper.setBackgroundRes(
            R.id.my_order_image,
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
            setText(R.id.my_size, item.photoSize)
            setText(R.id.my_order_code, item.outTradeNo)
            setText(R.id.my_create_time, TimeHelper.parseISOTimeString(item.createAt))
            if (item.multibgs) {
                setText(R.id.my_background, "多背景色")
            } else {
                setText(R.id.my_background, "单背景色")
            }
            addOnClickListener(R.id.my_download)
            addOnClickListener(R.id.my_flush_out)
            addOnClickListener(R.id.more_options)
            if (item.type == 2) {
                setVisible(R.id.ll_paid, false)
                setText(R.id.my_background_address, "收货地址：")
                setText(R.id.my_background, item.address?.address)
            }
        }

    }
}