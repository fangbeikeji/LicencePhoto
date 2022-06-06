package com.fbkj.licencephoto.utils

import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader

/**
- @author:  LZC
- @time:  2021/6/8
- @desc:
 */
class JsonFetch {
    fun getJson(context: Context, filename: String):String{
        val stringBuilder = StringBuilder()
        try {
            BufferedReader(InputStreamReader(
                context.assets.open(filename),
                "UTF-8")).forEachLine {
                if (it.isNotEmpty()){
                    stringBuilder.append(it).toString()
                }
            }
        }catch (e: Exception){
            e.printStackTrace()
        }
        return stringBuilder.toString()
    }

}