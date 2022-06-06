package com.fbkj.licencephoto.ui.complain

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.fbkj.licencephoto.R
import com.fbkj.licencephoto.databinding.ActivityComplainBinding
import com.fbkj.licencephoto.model.NewOrder
import com.fbkj.licencephoto.utils.AmountUtil
import com.fbkj.licencephoto.utils.TimeHelper
import com.fbkj.licencephoto.utils.toast

class ComplainActivity : AppCompatActivity() {
    private var _binding: ActivityComplainBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: ComplainViewModel
    private lateinit var order: NewOrder
    private var type: Int = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityComplainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
        type = intent.getIntExtra("type", 2)

        if (type == 2){
            binding.toolbarTxt.text = "反馈建议"
            binding.etContent.hint = getString(R.string.input_content)
            binding.tvTag1.visibility = View.GONE
            binding.etAmount.visibility = View.GONE
        }else{
            binding.toolbarTxt.text = "退款申请"
        }

        order = intent.getParcelableExtra("order")!!
        viewModel = ViewModelProvider(this)[ComplainViewModel::class.java]

        binding.etOrderId.setText(order.outTradeNo)
        binding.etAmount.setText(AmountUtil.getInstance().changeF2Y(this.order.amount.toLong()))
        binding.etCreateTime.setText(TimeHelper.parseISOTimeString(order.createAt))
        binding.tvBack.setOnClickListener { onBackPressed() }
        binding.tvCommit.setOnClickListener { sendComplain() }
        viewModel.onResult.observe(this@ComplainActivity){
            if (it){
                this.finish()
                toast("提交成功，感谢您的反馈")
            } else{
                toast("抱歉，出了点小问题，请稍后提交")
            }
        }
    }

    private fun sendComplain() {
        val contactName = binding.etContactName.text.toString()
        val contactWay = binding.etContactWay.text.toString()
        val content = binding.etContent.text.toString()
        if (contactName.isEmpty() || content.isEmpty() || contactWay.isEmpty()) {
            toast("请补充完整信息")
            return
        }
        viewModel.sendComplain(order.id,type,contactName,contactWay,content)
    }

    companion object {
        fun start(
            context: Context, type: Int, order: NewOrder
        ) {
            context.startActivity(Intent(context, ComplainActivity::class.java).apply {
                putExtra("type", type)
                putExtra("order", order)
            })
        }
    }
}