package com.fbkj.licencephoto.local

import android.os.Environment
import android.util.Log
import java.io.*

/**
- @author:  LZC
- @time:  2021/7/12
- @desc:
 */
open class SavingUserMsg {
    val path = Environment.getExternalStorageDirectory()!!.absolutePath + "/licencepic.txt"

    fun saveUserMsg2SD(userMsg: String) {
        try {
            if (!File(path).exists()) {
                File(path).createNewFile()
            } else {
                return
            }
            FileWriter(path).buffered().use { it.write(userMsg) }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun readUserMsgFromSD(): String =
        try {
            FileReader(path).buffered().use { it.readText() }
        } catch (e: Exception) {
            Log.i("sdcard_token", e.toString())
            e.printStackTrace()
            ""
        }

    companion object {
        @Volatile
        private var INSTANCE: SavingUserMsg? = null
        fun getInstance() = INSTANCE ?: synchronized(SavingUserMsg::class.java) {
            INSTANCE ?: SavingUserMsg().also { INSTANCE = it }
        }
    }

}