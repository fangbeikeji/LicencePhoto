package com.fbkj.licencephoto.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import com.fbkj.licencephoto.databinding.DialogAddressLayoutBinding
import com.fbkj.licencephoto.ui.address.AddressManagerActivity

/**
- @author:  LZC
- @time:  2021/6/21
- @desc:
 */

class AddressDialog(context: Context) : Dialog(context) {

    private var _binding: DialogAddressLayoutBinding? = null
    val binding get() = _binding!!

    //接口定义：
    private var onConfirmListener: ((Int) -> Unit)? = null
    private var st = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = DialogAddressLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvChooseAddress.setOnClickListener {
            AddressManagerActivity.start(context)
        }

        // 使用
        binding.tvConfirm.setOnClickListener {
            onConfirmListener?.invoke(st)
        }
    }

    fun setOnConfirmListener(onConfirmListener: ((Int) -> Unit)) {
        this.onConfirmListener = onConfirmListener
    }

    var name = "姓名"
        set(value) {
            field = value
            this@AddressDialog.binding.tvName.text = field
        }

    var phone = "+86"
        set(value) {
            field = value
            this@AddressDialog.binding.tvPhone.text = field
        }

    var address = "地址"
        set(value) {
            field = value
            this@AddressDialog.binding.tvAddress.text = field
        }

}
