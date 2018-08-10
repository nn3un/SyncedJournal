package com.example.nnuzaba47.syncedjournal

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.*
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_retrieve_instagram_access_token.*

class RetrieveInstagramAccessTokenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_retrieve_instagram_access_token)
        webView.webViewClient = object : WebViewClient() {
            var access_token: String? = null
            var authComplete: Boolean = false

            override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
                handler.proceed()
            }

            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    view!!.loadUrl(request!!.url.toString())
                }
                return true
            }

            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                if (url.contains("#access_token=") && !authComplete) {
                    val uri = Uri.parse(url)
                    access_token = uri.encodedFragment
                    // get the whole token after the '=' sign
                    access_token = access_token!!.substring(access_token!!.lastIndexOf("=") + 1)
                    authComplete = true
                    var intent = Intent()
                    intent.putExtra("ACCESS_TOKEN", access_token)
                    Log.i("tag", access_token)
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                } else if(url.contains("?error")){
                    Toast.makeText(applicationContext, "Error Occurred", Toast.LENGTH_SHORT).show()
                    setResult(Activity.RESULT_CANCELED)
                    finish()
                }
            }
        }
        webView.settings.domStorageEnabled = true;
        webView.settings.setSupportMultipleWindows(true)
        webView.settings.javaScriptCanOpenWindowsAutomatically = true
        webView.settings.javaScriptEnabled = true
        webView.webChromeClient = WebChromeClient()
        }

    fun startSync(view: View) {
        webView.loadUrl("https://api.instagram.com/oauth/authorize/?client_id=dbf2ef2cda7d45b68131d56a71cf7fce&redirect_uri=https://this-page-intentionally-left-blank.org/&response_type=token")
    }
    }
