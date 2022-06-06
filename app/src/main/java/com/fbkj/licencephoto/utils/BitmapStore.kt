package com.fbkj.licencephoto.utils

import android.annotation.SuppressLint
import android.graphics.Bitmap

/**
- @author:  LZC
- @time:  2021/7/2
- @desc:
 */
class BitmapStore {

    lateinit var originBitmap :Bitmap//没处理背景的图片，原始抠完图后没有美颜换装
    lateinit var beautyBitmap :Bitmap//处理背景的图片，抠完图美颜加换装
    lateinit var removeBgBitmap :Bitmap//没处理背景的图片，抠完图美颜加换装
    lateinit var originBackUpBitmap :Bitmap//预留的另一个保存图片的bitmap
    lateinit var currentOrderPic :String//在订单里边点击去冲印或者未支付的话去支付从后台下载的抠图url地址

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var INSTANCE: BitmapStore? = null

        fun getInstance() = INSTANCE ?: synchronized(BitmapStore::class.java) {
                INSTANCE ?: BitmapStore()
                    .also { INSTANCE = it }
            }
    }
}