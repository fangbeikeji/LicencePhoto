package com.fbkj.licencephoto.network

import android.util.Log
import com.fbkj.licencephoto.R
import com.fbkj.licencephoto.model.NetResponse
import com.fbkj.licencephoto.utils.ContextHolder
import com.fbkj.licencephoto.utils.toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody

/**
- @author:  LZC
- @time:  2021/6/2
- @desc:
 */

@FunctionalInterface
interface NetworkCall {

    suspend fun <T : Any> apiCall(call: suspend () -> NetResponse<T>): Result<T?> {
        val response = try {
                call.invoke()
            } catch (t: Throwable) {
                t
            }

        if (response is Exception) {
            Log.e("fetch", response.toString())
            return Result.failure(response)
        }

        response as NetResponse<*>
        if (response.status != 200) {
            Log.e("NetworkCall", "${response.status} ${response.msg}")
            if (response.status == 401) {
                withContext(Dispatchers.Main) {
                    ContextHolder.context.toast(
                        response.msg ?: ContextHolder.context.getString(R.string.please_login))
                }
            }
            return Result.failure(ApiCodeException(response.status,response.msg))
        }
        @Suppress("UNCHECKED_CAST")
        return Result.success(response.data as T?)
    }

    suspend fun request(job: suspend () -> ResponseBody) {
        if (!ConnectCheck.isConnected()) {
            val msg = ContextHolder.context.getString(R.string.please_check_network)
            withContext(Dispatchers.Main) {
                ContextHolder.context.toast(msg)
            }
            return
        }

        try {
            job.invoke()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}