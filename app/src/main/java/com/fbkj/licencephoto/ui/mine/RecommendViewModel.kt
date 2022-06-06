package com.fbkj.licencephoto.ui.mine

import android.content.Context
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.fbkj.licencephoto.base.BaseViewModel
import com.fbkj.licencephoto.network.RetrofitHelper.api
import com.fbkj.licencephoto.utils.toast
import kotlinx.coroutines.launch

/**
- @author:  LZC
- @time:  2021/6/25
- @desc:
 */
class RecommendViewModel : BaseViewModel() {
    fun commit(context: Context, contact: String, content: String) {
        viewModelScope.launch {
            val result = apiCall { api.recommend(context.packageName, contact, content) }
            if (result.isSuccess){
                context.toast("提交成功,感谢您的建议与支持！")
                Log.i("recommend_Success",result.getOrNull().toString())
            }else{
                context.toast("提交失败")
                Log.i("recommend_Failed",result.getOrNull().toString())
            }
        }
    }
}