package com.fbkj.licencephoto.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.DownloadListener
import android.webkit.WebChromeClient
import android.webkit.WebSettings.LOAD_DEFAULT
import android.webkit.WebView
import android.webkit.WebViewClient
import com.fbkj.licencephoto.base.BaseActivity
import com.fbkj.licencephoto.databinding.ActivityWebViewBinding

@Suppress("DEPRECATION")
class WebViewActivity : BaseActivity() {
    private lateinit var binding: ActivityWebViewBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebViewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
        binding.toolbarTxt.text = intent.getStringExtra("about_title")
        val mInitUrl = intent.getStringExtra("WEB_VIEW_URL").toString()
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        if (mInitUrl.isNotEmpty()) {
            setupWebView()
            binding.webView.loadUrl(mInitUrl)
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        binding.webViewProgress.max = 100
        binding.webView.settings.apply {
            setSupportZoom(false)
            allowFileAccess = true
            useWideViewPort = true
            databaseEnabled = true
            javaScriptEnabled = true
            domStorageEnabled = true
            loadWithOverviewMode = true
            loadsImagesAutomatically = true
            cacheMode = LOAD_DEFAULT
            javaScriptCanOpenWindowsAutomatically = true
        }
        binding.webView.webViewClient = webViewClient
        binding.webView.webChromeClient = webChromeClient
        binding.webView.setDownloadListener(downloadListener)
    }

    private val downloadListener: DownloadListener
        get() = DownloadListener { url: String?,
                                   userAgent: String?,
                                   contentDisposition: String?,
                                   mimetype: String?,
                                   contentLength: Long ->
            val intent = Intent(
                Intent.ACTION_VIEW
            )
            intent.addCategory(Intent.CATEGORY_BROWSABLE)
            intent.data = Uri.parse(url)
            startActivity(intent)
        }
    private val webViewClient: WebViewClient
        get() = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                if (url.startsWith("http")) {
                    view.loadUrl(url)
                    return true
                }
                return super.shouldOverrideUrlLoading(view, url)
            }
        }
    private val webChromeClient: WebChromeClient
        get() = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, newProgress: Int) {
                if (newProgress in 1..99) {
                    binding.webViewProgress.progress = newProgress
                    binding.webViewProgress.visibility = View.VISIBLE
                } else {
                    binding.webViewProgress.visibility = View.GONE
                }
            }

            override fun onReceivedTitle(view: WebView, title: String) {
                setTitle(title)
            }
        }

    public override fun onPause() {
        super.onPause()
        binding.webView.onPause()
    }

    public override fun onResume() {
        super.onResume()
        binding.webView.onResume()
    }

    public override fun onDestroy() {
        binding.webView.loadDataWithBaseURL(
            null, "",
            "text/html", "utf-8", null
        )
        binding.webView.clearHistory()
        binding.webView.destroy()
        super.onDestroy()
    }

    companion object {
        @JvmStatic
        fun startUrl(context: Context?, url: String?,title: String) {
            context?.let {
                val intent = Intent(context, WebViewActivity::class.java)
                intent.putExtra("WEB_VIEW_URL", url)
                intent.putExtra("about_title", title)
                context.startActivity(intent)
            }

        }
    }
}