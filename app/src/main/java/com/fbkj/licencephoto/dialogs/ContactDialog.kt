package com.fbkj.licencephoto.dialogs

import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.View
import com.fbkj.licencephoto.config.Constants.CONTACT_QQ
import com.fbkj.licencephoto.databinding.DialogContactUsBinding

/**
- @author:  LZC
- @time:  2021/6/23
- @desc:
 */
class ContactDialog(context : Context,c : Callback) : Dialog(context) {

    private val call = c
    private var _binding: DialogContactUsBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding= DialogContactUsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.tvWay.text="联系客服：${CONTACT_QQ}"
        binding.tvConfirm.setOnClickListener{
            (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager)
                .setPrimaryClip(ClipData.newPlainText(null, CONTACT_QQ))
            call.onClick(it)
            dismiss()
        }
    }

    interface Callback {
        fun onClick(v: View?)
    }
}