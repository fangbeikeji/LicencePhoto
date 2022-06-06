package com.fbkj.licencephoto.network

import com.fbkj.licencephoto.BuildConfig
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.*
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager

/**
- @author:  LZC
- @time:  2021/6/2
- @desc:
 */

object RetrofitHelper {
    val gson: Gson by lazy { GsonBuilder().setLenient().create() }

    val api: ServerApi by lazy {
        val clientBuilder = OkHttpClient.Builder().apply {
            addInterceptor(NetInterceptor())
            readTimeout(60,TimeUnit.SECONDS)
            writeTimeout(60,TimeUnit.SECONDS)
            connectTimeout(60,TimeUnit.SECONDS)
        }
        if (BuildConfig.DEBUG) {
            clientBuilder.addInterceptor(
                HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
            )
        }

        val trustManagers: Array<TrustManager> = arrayOf(
            object : X509TrustManager {
                @Throws(CertificateException::class)
                override fun checkClientTrusted(
                    chain: Array<X509Certificate?>?,
                    authType: String?
                ) {
                }

                @Throws(CertificateException::class)
                override fun checkServerTrusted(
                    chain: Array<X509Certificate?>?,
                    authType: String?
                ) {
                }

                override fun getAcceptedIssuers(): Array<X509Certificate> {
                    return arrayOf()
                }
            }
        )
        check(!(trustManagers.size != 1 || trustManagers[0] !is X509TrustManager)) {
            ("Unexpected default trust managers:" + trustManagers.contentToString())
        }
        val trustManager = trustManagers[0] as X509TrustManager

        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, trustManagers, SecureRandom())
        val sslSocketFactory = sslContext.socketFactory

        clientBuilder.sslSocketFactory(sslSocketFactory, trustManager)
        clientBuilder.hostnameVerifier { _, _ ->
            return@hostnameVerifier true
        }

        val retrofit = Retrofit.Builder()
            .client(clientBuilder.build())
            .baseUrl("http://idphoto.public.fbkjapp.com")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        return@lazy retrofit.create(ServerApi::class.java)
    }
}