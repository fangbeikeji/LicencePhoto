package com.fbkj.licencephoto.ui.logic

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.fbkj.licencephoto.base.BaseViewModel
import com.fbkj.licencephoto.network.RetrofitHelper
import com.fbkj.licencephoto.utils.FileHandler
import com.fbkj.licencephoto.utils.toast
import com.huawei.hms.mlsdk.MLAnalyzerFactory
import com.huawei.hms.mlsdk.common.MLFrame
import com.huawei.hms.mlsdk.imgseg.MLImageSegmentationScene
import com.huawei.hms.mlsdk.imgseg.MLImageSegmentationSetting
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okio.Okio
import top.zibin.luban.Luban
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream

/**
- @author:  LZC
- @time:  2021/6/28
- @desc:
 */

class EditImageViewModel : BaseViewModel() {

    val rembgFile = MutableLiveData<File>()
    val rembgBitmap = MutableLiveData<Bitmap>()

    private val analyzer by lazy {
        val setting = MLImageSegmentationSetting.Factory() // 设置分割精细模式，true为精细分割模式，false为速度优先分割模式。
            .setAnalyzerType(MLImageSegmentationSetting.BODY_SEG) // 设置返回结果种类。
            // MLImageSegmentationScene.ALL: 返回所有分割结果，包括：像素级标记信息、背景透明的人像图和人像为白色，背景为黑色的灰度图以及被分割的原图。
            // MLImageSegmentationScene.MASK_ONLY: 只返回像素级标记信息和被分割的原图。
            // MLImageSegmentationScene.FOREGROUND_ONLY: 只返回背景透明的人像图和被分割的原图。
            // MLImageSegmentationScene.GRAYSCALE_ONLY: 只返回人像为白色，背景为黑色的灰度图和被分割的原图。
            .setScene(MLImageSegmentationScene.FOREGROUND_ONLY)
            .setExact(true)
            .create()
        return@lazy MLAnalyzerFactory.getInstance().getImageSegmentationAnalyzer(setting)
    }

    override fun onCleared() {
        super.onCleared()
        analyzer.stop()
    }

    fun removeBackground(context: Context, uri: Uri, byCameras: Boolean) {
        val t = System.currentTimeMillis()

        val bitmap = if (byCameras) {//拍照的照片
            val b1 = Luban.with(context).ignoreBy(400).load(
                uri).get()[0]//少于400k不压缩
            ex(uri.path!!, BitmapFactory.decodeFile(b1.path))
        } else {//从相册里边选择的照片.
            val b2 = Luban.with(context).ignoreBy(400).load(
                FileHandler.getPath(context, uri)).get()[0]//少于400k不压缩
            BitmapFactory.decodeFile(b2.path)
        }

        val frame = MLFrame.fromBitmap(bitmap)
        val task = analyzer.asyncAnalyseFrame(frame)
        task.addOnSuccessListener {
            if (it?.foreground != null) {
                var b = 0
                it.masks.forEach { itsByte ->
                    b += itsByte
                }
                // 当为 0 的时候，HMS 本地抠图未检测到人像
                if (b == 0) {
                    context.toast("未检测到人像，切换至第二模式处理")
                    removeBackgroundByServer(context, uri, byCameras)
                    return@addOnSuccessListener
                }
                val ms = System.currentTimeMillis() - t
                if (ms < 1000) {
                    viewModelScope.launch {
                        delay(1000 - ms)
                        rembgBitmap.value = it.foreground
                    }
                } else {
                    rembgBitmap.value = it.foreground
                }
            } else {
                removeBackgroundByServer(context, uri, byCameras)
            }
        }.addOnFailureListener {
            removeBackgroundByServer(context, uri, byCameras)
        }
    }

    private fun removeBackgroundByServer(context: Context, uri: Uri, byCameras: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val file = saveBitmapFile(uri, context, byCameras)

            val image = MultipartBody.Part.createFormData(
                "file",
                file.name,
                RequestBody.create(MediaType.parse("image/*"), file)
            )

            val body = RetrofitHelper.api.rembg(image)
            val rembgFile = File(
                context.externalCacheDir,
                "${System.currentTimeMillis()}"
            )
            Okio.buffer(Okio.sink(rembgFile))?.use { it.writeAll(body.source()) }
            this@EditImageViewModel.rembgFile.postValue(rembgFile)
        }
    }

    private fun saveBitmapFile(uri: Uri, context: Context, byCameras: Boolean): File {
        return if (byCameras) {
            orientation(
                context, uri.path!!,
                BitmapFactory.decodeStream(context.contentResolver.openInputStream(uri))
            )
        } else {
            orientation(
                context, FileHandler.getPath(context, uri),
                BitmapFactory.decodeStream(context.contentResolver.openInputStream(uri))
            )
        }
    }

    private fun orientation(context: Context, path: String, bm: Bitmap): File {
        val b = ex(path, bm)
        val file = File(context.externalCacheDir, "b2f${System.currentTimeMillis()}")
        val bos = BufferedOutputStream(FileOutputStream(file))
        b.compress(Bitmap.CompressFormat.PNG, 100, bos)
        bos.flush()
        bos.close()
        return Luban.with(context).ignoreBy(400).load(file).get()[0]//少于400k不压缩
    }

    private fun ex(path: String, bm: Bitmap): Bitmap {
        var d = 0
        val m = Matrix()
        val ori = ExifInterface(path).getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )
        when (ori) {
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> {
//                d = 0
                m.postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_TRANSPOSE -> {
                d = 90
                m.postRotate(d.toFloat())
                m.postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                d = 270
                m.postRotate(d.toFloat())
                m.postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_ROTATE_270 -> {
                d = 270
                m.postRotate(d.toFloat())
                m.postScale(-1f, 1f)
            }
        }
        return Bitmap.createBitmap(bm, 0, 0, bm.width, bm.height, m, true)
    }

}