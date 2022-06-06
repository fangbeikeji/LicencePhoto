package com.fbkj.licencephoto.dialogs

import android.content.Context
import com.fbkj.licencephoto.R
import com.fbkj.licencephoto.utils.ScreenUtils
import com.lxj.xpopup.core.CenterPopupView

/**
- @author:  LZC
- @time:  2021/7/9
- @desc:
 */

class MakingPop
    (context: Context) : CenterPopupView(context) {
    // 返回自定义弹窗的布局
    override fun getImplLayoutId(): Int {
        return R.layout.making_pop
    }
    override fun getMaxWidth(): Int {
        return ScreenUtils.getScreenWidth(context)
    }
}