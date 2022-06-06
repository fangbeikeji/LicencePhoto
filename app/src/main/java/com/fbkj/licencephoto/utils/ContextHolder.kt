package com.fbkj.licencephoto.utils

import android.annotation.SuppressLint
import android.content.Context

class ContextHolder {
    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
    }
}