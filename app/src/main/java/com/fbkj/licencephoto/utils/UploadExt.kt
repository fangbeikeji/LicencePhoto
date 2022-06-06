package com.fbkj.licencephoto.utils

import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okio.*
import java.io.File
import java.io.IOException

class CountingRequestBody(
    private val requestBody: RequestBody,
    private val onProgressUpdate: CountingRequestListener,
) : RequestBody() {
    override fun contentType() = requestBody.contentType()

    @Throws(IOException::class)
    override fun contentLength() = requestBody.contentLength()

    @Throws(IOException::class)
    override fun writeTo(sink: BufferedSink) {
        if (sink is Buffer) {
            // Log Interceptor
            requestBody.writeTo(sink)
            return
        }
        val countingSink = CountingSink(sink, this, onProgressUpdate)
        val bufferedSink = Okio.buffer(countingSink)

        requestBody.writeTo(bufferedSink)

        bufferedSink.flush()
    }
}

typealias CountingRequestListener = (bytesWritten: Long, contentLength: Long) -> Unit


class CountingSink(
    sink: Sink,
    private val requestBody: RequestBody,
    private val onProgressUpdate: CountingRequestListener,
) : ForwardingSink(sink) {
    private var bytesWritten = 0L

    override fun write(source: Buffer, byteCount: Long) {
        super.write(source, byteCount)
        bytesWritten += byteCount
        onProgressUpdate(bytesWritten, requestBody.contentLength())
    }
}

fun createUploadRequestBody(
    file: File,
    mimeType: String,
    listener: OnUploadListener?,
): RequestBody {
    val requestBody = RequestBody.create(MediaType.parse(mimeType), file)
    return CountingRequestBody(requestBody) { bytesWritten, contentLength ->
        val progress = 1.0 * bytesWritten / contentLength
        listener?.onProgress(progress)

        if (progress >= 1.0) {
            listener?.onComplete()
        }
    }
}

interface OnUploadListener {
    fun onProgress(progress: Double)

    fun onComplete()
}


inline fun String.toPlainTextBody() = RequestBody.create(MediaType.parse("text/plain"), this)

inline fun File.createFormData(fieldName: String, mimeType: String, listener: OnUploadListener?) =
    MultipartBody.Part.createFormData(fieldName, this.name, createUploadRequestBody(this, mimeType, listener))
