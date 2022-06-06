package com.fbkj.licencephoto.ui.address

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.ViewModelProvider
import com.fbkj.licencephoto.base.BaseActivity
import com.fbkj.licencephoto.config.Event.ON_ADDRESS_ADD
import com.fbkj.licencephoto.databinding.AddAddressActivityBinding
import com.fbkj.licencephoto.model.EventModel
import com.fbkj.licencephoto.utils.toast
import com.lljjcoder.Interface.OnCityItemClickListener
import com.lljjcoder.bean.CityBean
import com.lljjcoder.bean.DistrictBean
import com.lljjcoder.bean.ProvinceBean
import com.lljjcoder.citywheel.CityConfig
import com.lljjcoder.style.citypickerview.CityPickerView
import org.greenrobot.eventbus.EventBus
import java.util.regex.Pattern

class AddAddressActivity : BaseActivity() {

    private var _binding: AddAddressActivityBinding? = null
    private val binding get() = _binding!!
    private var cityPicker = CityPickerView()
    private lateinit var viewModel: AddAddressModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = AddAddressActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this)[AddAddressModel::class.java]
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
        binding.toolbarTxt.text = "添加地址"
        cityPicker.init(this)
        if (intent.getBooleanExtra("modify", false)){
            binding.llChooseAddress.visibility = View.GONE
            binding.postalCode.setText(intent.getStringExtra("postalCode"))
            binding.addressee.setText(intent.getStringExtra("addressee"))
            binding.phone.setText(intent.getStringExtra("phone"))
            binding.addressDetails.setText(intent.getStringExtra("address"))
        }

        binding.llChooseAddress.setOnClickListener {
            selectAddress() //弹出地址选择框
        }

        binding.tvFinish.setOnClickListener {
            if (intent.getBooleanExtra("modify", false)) {
                judgeAndUpdate(
                    intent.getStringExtra("addressId").toString(),
                    binding.postalCode.text.toString().trim(),
                    binding.addressee.text.toString().trim(),
                    binding.phone.text.toString().trim(),
                    binding.chooseAddress.text.toString() + binding.addressDetails.text.toString(),
                    true
                )
            }
            judgeAndAdd(
                binding.postalCode.text.toString().trim(),
                binding.addressee.text.toString().trim(),
                binding.phone.text.toString().trim(),
                binding.chooseAddress.text.toString() + binding.addressDetails.text.toString(),
                true
            )
        }
        viewModel.addCondition.observe(this){
            if (it){
                finish()
                toast("添加成功")
                EventBus.getDefault().post(EventModel(1,ON_ADDRESS_ADD))
            } else{
                toast("添加失败,请检查信息")
            }
        }
    }

    private fun judgeAndAdd(
        postalCode: String,
        contactName: String,
        phone: String,
        address: String,
        isDefault: Boolean
    ) {
        val machePhone = Pattern.compile("^(13|14|15|16|17|18|19)\\d{9}$").matcher(phone)
        val machePostal = Pattern.compile("^[1-9][0-9]{5}$").matcher(postalCode)
//        val machePhone = phoneReg.matcher(phone)
        if (phone.isEmpty() || !machePhone.matches()) {
            toast("请输入完整正确手机号")
            return
        }
        if (postalCode.isEmpty() || !machePostal.matches()) {
            toast("请输入完整正确邮编")
            return
        }

        viewModel.addAddress(postalCode, contactName, phone, address, isDefault)
    }

    private fun judgeAndUpdate(
        id: String,
        postalCode: String,
        contactName: String,
        phone: String,
        address: String,
        isDefault: Boolean
    ) {
        val machePhone = Pattern.compile("^(13|14|15|16|17|18|19)\\d{9}$").matcher(phone)
        val machePostal = Pattern.compile("^[1-9][0-9]{5}$").matcher(postalCode)
//        val machePhone = phoneReg.matcher(phone)
        if (phone.isEmpty() || !machePhone.matches()) {
            toast("请输入完整正确手机号")
            return
        }
        if (postalCode.isEmpty() || !machePostal.matches()) {
            toast("请输入完整正确邮编")
            return
        }

        viewModel.updateAddress(id, postalCode, contactName, phone, address, isDefault)

    }

    private fun selectAddress() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        if (imm.isActive){//如果激活了软键盘
            imm.hideSoftInputFromWindow(currentFocus!!.windowToken,InputMethodManager.HIDE_NOT_ALWAYS)
        }

        cityPicker.apply {
            setConfig(CityConfig.Builder().build())
            setOnCityItemClickListener(object : OnCityItemClickListener() {

                override fun onSelected(
                    province: ProvinceBean?,
                    city: CityBean?,
                    district: DistrictBean?
                ) {
                    super.onSelected(province, city, district)
                    binding.chooseAddress.setText(
                        province!!.name.toString().trim() +
                                city!!.name.toString().trim() +
                                district!!.name.toString().trim()
                    )
                }

                override fun onCancel() {
                    super.onCancel()
                    toast("back")
                }
            })
        }.showCityPicker()
    }

    companion object {
        fun start(
            context: Context, modify: Boolean,
            postalCode: String?, contactName: String?, phone: String?, address: String?,addressId:String?
        ) {
            context.startActivity(Intent(context, AddAddressActivity::class.java).apply {
                putExtra("modify", modify)
                if (modify) {
                    putExtra("addressId", addressId)
                    putExtra("postalCode", postalCode)
                    putExtra("contactName", contactName)
                    putExtra("phone", phone)
                    putExtra("address", address)
                }
            })
        }
    }
}