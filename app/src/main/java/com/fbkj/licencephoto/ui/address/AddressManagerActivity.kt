package com.fbkj.licencephoto.ui.address

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.fbkj.licencephoto.R
import com.fbkj.licencephoto.config.Event
import com.fbkj.licencephoto.databinding.AddressManagerActivityBinding
import com.fbkj.licencephoto.local.LastClickRecord
import com.fbkj.licencephoto.model.AddressModel
import com.fbkj.licencephoto.model.EventModel
import com.fbkj.licencephoto.utils.toast
import com.lljjcoder.style.citypickerview.CityPickerView
import com.lxj.xpopup.XPopup
import kotlinx.android.synthetic.main.layout_address_item.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class AddressManagerActivity : AppCompatActivity() {

    private var _binding: AddressManagerActivityBinding? = null
    private val binding get() = _binding!!
    private var cityPicker = CityPickerView()
    private lateinit var addressAdapter: AddressAdapter
    private lateinit var viewModel: AddressManagerModel
    private lateinit var addressBundle:AddressModel
    private var isChoose = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = AddressManagerActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        EventBus.getDefault().register(this)
        viewModel = ViewModelProvider(this)[AddressManagerModel::class.java]

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
        binding.toolbarTxt.text = "选择地址"

        cityPicker.init(this)
        binding.recyclerView.layoutManager = LinearLayoutManager(
            this, LinearLayoutManager.VERTICAL, false
        )
        binding.addAddress.setOnClickListener {
            AddAddressActivity.start(
                this, false, "",
                "", "", "", ""
            )
        }

        binding.chooseConfirm.setOnClickListener {
            if (!isChoose){
                toast("请选择地址")
                return@setOnClickListener
            }
            //记录最近选择地址
            EventBus.getDefault().post(EventModel(0, Event.ON_ADDRESS_CHANGED))
            finish()
        }

        viewModel.getAddress()

        viewModel.addressList.observe(this){
            addressAdapter = AddressAdapter(it)
            addressAdapter.onItemClickListener =
                BaseQuickAdapter.OnItemClickListener { adapter, _, position ->
                    //修改地址
                    AddAddressActivity.start(
                        this,
                        true,
                        addressAdapter.data[position].postalCode,
                        addressAdapter.data[position].contactName,
                        addressAdapter.data[position].phone,
                        addressAdapter.data[position].address,
                        addressAdapter.data[position].id.toString()
                    )
                    adapter.notifyDataSetChanged()
                }
            addressAdapter.setOnItemChildClickListener { _, view, position ->
                if (tv_delete.id == view.id) {
                    //删除当前地址
                    XPopup.Builder(this).asConfirm(
                        "删除", getString(R.string.delete_address)
                    ) {
                        viewModel.deleteAddress(addressAdapter.data[position].id)
                        addressAdapter.remove(position)
                        addressAdapter.notifyDataSetChanged()
                    }.show()

                }
                if (address_chosen.id == view.id) {
                    isChoose = true
                    LastClickRecord.getInstance().recordAddressData(addressAdapter.data[position])
                    addressBundle = addressAdapter.data[position]
                    addressAdapter.chosen = position
                    addressAdapter.notifyDataSetChanged()

                }
            }
            binding.recyclerView.adapter = addressAdapter
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun eventData(message: EventModel) {
        when (message.message) {
            Event.ON_ADDRESS_ADD -> {
                viewModel.page = 0
                viewModel.getAddress()
                addressAdapter.notifyDataSetChanged()
            }
        }
    }

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, AddressManagerActivity::class.java))
        }
    }
}

class AddressAdapter(sizeData: List<AddressModel>) : BaseQuickAdapter<AddressModel, BaseViewHolder>
    (R.layout.layout_address_item, sizeData) {
    var chosen = -1

    @SuppressLint("ResourceAsColor")
    override fun convert(helper: BaseViewHolder?, item: AddressModel?) {

        helper?.apply {
            setText(R.id.tv_name, item!!.contactName)
            setText(R.id.tv_phone, item.phone)
            setText(R.id.tv_address, item.address)
            addOnClickListener(R.id.tv_delete)
            addOnClickListener(R.id.address_chosen)
        }

        helper?.setBackgroundRes(R.id.address_chosen, R.mipmap.ic_address_off)
        if (helper?.adapterPosition == chosen) {
            helper.setBackgroundRes(R.id.address_chosen, R.mipmap.ic_address_on)
        }
    }
}