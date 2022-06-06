package com.fbkj.licencephoto.utils

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import java.io.File
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL

/**
- @author:  LZC
- @time:  2021/9/28
- @desc:
 */
object PhotoUtils {

    fun saveBitmap2Gallery(context: Context, bitmap: Bitmap): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            //返回出一个URI
            val insert = context.contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                /*
                这里如果不写的话 默认是保存在 /sdCard/DCIM/Pictures
                 */
                ContentValues()//这里可以啥也不设置 保存图片默认就好了
            ) ?: return false //为空的话 直接失败返回了

            //这个打开了输出流  直接保存图片就好了
            context.contentResolver.openOutputStream(insert).use {
                it ?: return false
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
            }

            // 通知相册更新
            context.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE), insert.toString())
            return true
        } else {
            MediaStore.Images.Media.insertImage(context.contentResolver, bitmap,
                "IMAGE"+System.currentTimeMillis().toString(), "MyLicencePics")
            // 通知相册更新
            context.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE))
            return true
        }
    }

    fun saveFile2Gallery(context: Context, url: String): Boolean {
        if (true) {
            //返回出一个URI
            val insert = context.contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                /*
                这里可以默认不写 默认保存在
                 */
                ContentValues()
            ) ?: return false //为空的话 直接失败返回了
            //这个打开了输出流  直接保存图片就好了
            context.contentResolver.openOutputStream(insert).use { os ->
                os ?: return false
                var x = download(url, os)
                return x
            }
            return false
        } else {
            val externalFilesDir =
                context.getExternalFilesDir(Environment.DIRECTORY_DCIM) ?: return false
            var name = "${System.currentTimeMillis()}.jpg"
            val file = File(externalFilesDir, name)
            //下载文件到应用目录
            download(url, file.outputStream())
            MediaStore.Images.Media.insertImage(
                context.contentResolver,
                file.absolutePath,
                name,
                "desc"
            )
            //刷新相册
            context.sendBroadcast(
                Intent(
                    Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                    Uri.fromFile(File(file.path))
                )
            )
            return true
        }
    }


    fun saveFile2Gallery2(context: Context, url: String): Boolean {
        val name = System.currentTimeMillis().toString()
        val photoPath = Environment.DIRECTORY_DCIM + "/Camera"
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, photoPath)//保存路径
                put(MediaStore.MediaColumns.IS_PENDING, true)
            }
        }
        //返回出一个URI
        val insert = context.contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        ) ?: return false
        //这个打开了输出流  直接保存图片就好了
        context.contentResolver.openOutputStream(insert).use { os ->
            os ?: return false
            var x = download(url, os)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, false)
            }
            return x
        }
        return false
    }


    fun saveBitmap2Gallery2(context: Context, bitmap: Bitmap): Boolean {
        val name = System.currentTimeMillis().toString()
        val photoPath = Environment.DIRECTORY_DCIM + "/LicencePics"
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, photoPath)//保存路径
                put(MediaStore.MediaColumns.IS_PENDING, true)
            }
        }
        val insert = context.contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        ) ?: return false //为空的话 直接失败返回了
        //这个打开了输出流  直接保存图片就好了
        context.contentResolver.openOutputStream(insert).use {
            it ?: return false
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.put(MediaStore.MediaColumns.IS_PENDING, false)
        }
        return true
    }


    private fun download(url: String, os: OutputStream): Boolean {
        val url = URL(url)
        (url.openConnection() as HttpURLConnection).also { conn ->
            conn.requestMethod = "GET"
            conn.connectTimeout = 5 * 1000
            if (conn.responseCode == 200) {
                conn.inputStream.use { ins ->
                    val buf = ByteArray(2048)
                    var len: Int
                    while (ins.read(buf).also { len = it } != -1) {
                        os.write(buf, 0, len)
                    }
                    os.flush()
                }
                return true
            } else {
                return false
            }
        }
    }

}