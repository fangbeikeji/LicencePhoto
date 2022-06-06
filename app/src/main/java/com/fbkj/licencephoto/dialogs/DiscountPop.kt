package com.fbkj.licencephoto.dialogs

import android.content.Context
import android.view.View
import android.widget.TextView
import com.fbkj.licencephoto.R
import com.fbkj.licencephoto.utils.ScreenUtils
import com.lxj.xpopup.core.CenterPopupView

/**
- @author:  LZC
- @time:  2021/7/20
- @desc:
 */
class DiscountPop (context: Context,c: Callback) : CenterPopupView(context) {
    private val call = c
    // 返回自定义弹窗的布局
    override fun getImplLayoutId(): Int {
        return R.layout.discount_pop
    }

    override fun onCreate() {
        super.onCreate()
        findViewById<TextView>(R.id.discount_pay).setOnClickListener {
            call.onClick(it)
            dismiss()
        }


    }
    override fun getMaxWidth(): Int {
        return ScreenUtils.getScreenWidth(context)
    }
    interface Callback {
        fun onClick(v: View?)
    }
}