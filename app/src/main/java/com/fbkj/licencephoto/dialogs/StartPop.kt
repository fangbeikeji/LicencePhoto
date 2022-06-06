package com.fbkj.licencephoto.dialogs

import android.content.Context
import android.view.View
import android.widget.TextView
import com.fbkj.licencephoto.R
import com.fbkj.licencephoto.utils.ScreenUtils
import com.lxj.xpopup.core.CenterPopupView

/**
- @author:  LZC
- @time:  2021/6/23
- @desc:
 */

internal class StartPop  //注意：自定义弹窗本质是一个自定义View，但是只需重写一个参数的构造，其他的不要重写，所有的自定义弹窗都是这样。
    (context: Context,c:Callback) : CenterPopupView(context) {
    private val call = c
    // 返回自定义弹窗的布局
    override fun getImplLayoutId(): Int {
        return R.layout.start_popup
    }

    // 执行初始化操作，比如：findView，设置点击，或者任何你弹窗内的业务逻辑
    override fun onCreate() {
        super.onCreate()
        findViewById<TextView>(R.id.tv_user_agreement).setOnClickListener {
            call.onClick(it)
        }
        findViewById<TextView>(R.id.tv_privacy_policy).setOnClickListener {
            call.onClick(it)
        }
        findViewById<TextView>(R.id.tv_not_agreed).setOnClickListener {
            call.onClick(it)
            dismiss()
        }
        findViewById<TextView>(R.id.tv_agreed).setOnClickListener {
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