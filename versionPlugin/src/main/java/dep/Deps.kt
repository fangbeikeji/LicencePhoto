package dep

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * ignore warnings
 * */

class Deps : Plugin<Project> {
    override fun apply(project: Project) {}
    companion object {}
}

object BuildConfig {
    const val compileSdkVersion = 31
    const val buildToolsVersion = "30.0.3"
    const val minSdkVersion = 21
    const val targetSdkVersion = 31
    const val versionCode = 11
    const val versionName = "1.4.6"
    const val multidexEnabled = true
}

object Versions {
    const val kotlinVersion = "1.5.10"
    const val camera = "1.1.0-alpha04"
    const val legacy = "1.0.0"
    const val ktx = "1.3.2"
    const val material = "1.2.1"
    const val eventBus = "3.2.0"
    const val compat = "1.2.0"
    const val constraint = "2.0.1"
    const val fragment = "1.2.5"
    const val retrofit = "2.8.1"
    const val lifecycle = "2.2.0"
    const val coroutines = "1.3.8"
    const val Um = "2.0.3"
    const val wx = "5.3.1"
    const val mmKv = "1.2.10"
    const val hw = "2.2.0.300"
}
object Common {
    const val junit = "junit:junit:4.+"
    const val junitExt = "androidx.test.ext:junit-ktx:1.1.2"
    const val espressoCore = "androidx.test.espresso:espresso-core:3.3.0"
}

object AndroidX {
    const val stdLib = "org.jetbrains.kotlin:kotlin-stdlib:${Versions.kotlinVersion}"
    const val legacy = "androidx.legacy:legacy-support-v4:${Versions.legacy}"
    const val coreKtx = "androidx.core:core-ktx:${Versions.ktx}"
    const val fragment = "androidx.fragment:fragment-ktx:${Versions.fragment}"
    const val material = "com.google.android.material:material:${Versions.material}"
    const val eventBus = "org.greenrobot:eventbus:${Versions.eventBus}"
    const val appcompat = "androidx.appcompat:appcompat:${Versions.compat}"
    const val constraint = "androidx.constraintlayout:constraintlayout:${Versions.constraint}"
}

object Lifecycle {
    const val liveData = "androidx.lifecycle:lifecycle-livedata-ktx:${Versions.lifecycle}"
    const val runTime = "androidx.lifecycle:lifecycle-runtime-ktx:2.3.1"
    const val viewModel = "androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.lifecycle}"
}

object Coroutines {
    const val coroutinesCore = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}"
    const val coroutinesAndroid = "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.coroutines}"
}

object Square {
    const val gson = "com.squareup.retrofit2:converter-gson:${Versions.retrofit}"
    const val okHttp3 = "com.squareup.okhttp3:okhttp:3.14.7"
    const val retrofit2 = "com.squareup.retrofit2:retrofit:${Versions.retrofit}"
    const val interceptor = "com.squareup.okhttp3:logging-interceptor:3.9.1"
}

object Umeng {
    const val asms = "com.umeng.umsdk:asms:1.4.1"// 必选
    const val main = "com.umeng.umsdk:uverify-main:${Versions.Um}"// 必选
    const val common = "com.umeng.umsdk:common:9.4.0"// 必选
    const val logger = "com.umeng.umsdk:uverify-logger:${Versions.Um}"// 必选
    const val crashShield = "com.umeng.umsdk:uverify-crashshield:${Versions.Um}"// 必选
    const val uApm =  "com.umeng.umsdk:apm:1.4.2"//用不到
}

object Git {
    const val zip = "top.zibin:Luban:1.1.8"
    const val xPop = "com.lxj:xpopup:1.9.0"
    const val walle = "com.meituan.android.walle:library:1.1.7"
    const val glide = "com.github.bumptech.glide:glide:4.12.0" //Glide
    const val brvah =  "com.github.CymChad:BaseRecyclerViewAdapterHelper:2.9.30"//BRVAH
    const val xBanner =  "com.github.xiaohaibin:XBanner:androidx_v1.1.6"//Banner
    const val permission = "com.github.getActivity:XXPermissions:11.6"
    const val checkVersion = "com.github.AlexLiuSheng:CheckVersionLib:2.3.4_appcompat"//VersionCheck
    const val glideCompiler = "com.github.bumptech.glide:compiler:4.10.0"//annotationProcessor
    const val cityPickerView = "liji.library.dev:citypickerview:3.1.13"
    const val applicationProvider = "com.github.florent37:applicationprovider:1.0.5"
    const val GPUImage = "jp.co.cyberagent.android:gpuimage:2.1.0"
}

object Camera {
    // CameraX
    const val camera2 = "androidx.camera:camera-camera2:${Versions.camera}"
    const val cameraView = "androidx.camera:camera-view:1.0.0-alpha24"
    const val cameraCore = "androidx.camera:camera-core:${Versions.camera}"
    const val cameraLifecycle = "androidx.camera:camera-lifecycle:${Versions.camera}"
}

object Sdk {
    const val wxSdk = "com.tencent.mm.opensdk:wechat-sdk-android-without-mta:${Versions.wx}"
    const val mmKv =  "com.tencent:mmkv-static:${Versions.mmKv}"
    const val hwCV = "com.huawei.hms:ml-computer-vision-segmentation:${Versions.hw}"// 引入基础SDK
    const val hwBM = "com.huawei.hms:ml-computer-vision-image-segmentation-body-model:${Versions.hw}"// 引入人像分割模型包
}







