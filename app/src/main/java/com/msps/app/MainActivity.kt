
package com.msps.app

import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast

class MainActivity : Activity() {

    private lateinit var webView: WebView
    private var fileChooserCallback: ValueCallback<Array<Uri>>? = null
    private var pendingBackup: ByteArray? = null

    companion object {
        private const val FILE_CHOOSER_REQUEST = 1001
        private const val BACKUP_REQUEST = 1002
        private const val WEBSITE_URL =
            "https://hostel2943-tech.github.io/MSPS-APP/"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        webView = WebView(this)
        setContentView(webView)

        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.allowFileAccess = true
        webView.settings.allowContentAccess = true

        webView.addJavascriptInterface(AndroidBridge(), "Android")
        webView.addJavascriptInterface(AndroidBridge(), "AMUAndroidPrint")

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                return false
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(
                view: WebView?,
                callback: ValueCallback<Array<Uri>>?,
                params: FileChooserParams?
            ): Boolean {
                fileChooserCallback?.onReceiveValue(null)
                fileChooserCallback = callback

                return try {
                    val intent = params?.createIntent()
                        ?: Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                            addCategory(Intent.CATEGORY_OPENABLE)
                            type = "*/*"
                        }

                    startActivityForResult(intent, FILE_CHOOSER_REQUEST)
                    true
                } catch (e: Exception) {
                    fileChooserCallback?.onReceiveValue(null)
                    fileChooserCallback = null
                    false
                }
            }
        }

        webView.setDownloadListener { url, userAgent, contentDisposition,
                                      mimeType, _ ->
            try {
                val request = DownloadManager.Request(Uri.parse(url))
                val fileName = android.webkit.URLUtil.guessFileName(
                    url, contentDisposition, mimeType
                )

                request.setTitle(fileName)
                request.setDescription("Downloading MSPS file")
                request.setMimeType(mimeType)
                request.addRequestHeader("User-Agent", userAgent)

                CookieManager.getInstance().getCookie(url)?.let {
                    request.addRequestHeader("Cookie", it)
                }

                request.setNotificationVisibility(
                    DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
                )
                request.setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS,
                    fileName
                )

                val manager =
                    getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                manager.enqueue(request)

                Toast.makeText(
                    this, "Download started", Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                Toast.makeText(
                    this, "Download could not start", Toast.LENGTH_LONG
                ).show()
            }
        }

        if (savedInstanceState == null) {
            webView.loadUrl(WEBSITE_URL)
        } else {
            webView.restoreState(savedInstanceState)
        }
    }

    inner class AndroidBridge {

        @JavascriptInterface
        fun saveBackup(base64Data: String, fileName: String) {
            try {
                pendingBackup =
                    android.util.Base64.decode(base64Data, android.util.Base64.DEFAULT)

                runOnUiThread {
                    val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                        addCategory(Intent.CATEGORY_OPENABLE)
                        type = "application/octet-stream"
                        putExtra(Intent.EXTRA_TITLE, fileName)
                    }
                    startActivityForResult(intent, BACKUP_REQUEST)
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(
                        this@MainActivity,
                        "Could not prepare backup",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        @JavascriptInterface
        fun print() {
            runOnUiThread {
                val printManager =
                    getSystemService(Context.PRINT_SERVICE) as android.print.PrintManager
                val adapter = webView.createPrintDocumentAdapter("MSPS")
                printManager.print("MSPS", adapter, null)
            }
        }
    }

    @Deprecated("Deprecated in Android API")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == FILE_CHOOSER_REQUEST) {
            val result = if (resultCode == RESULT_OK && data?.data != null) {
                arrayOf(data.data!!)
            } else {
                null
            }

            fileChooserCallback?.onReceiveValue(result)
            fileChooserCallback = null
        }

        if (requestCode == BACKUP_REQUEST) {
            if (resultCode == RESULT_OK && data?.data != null &&
                pendingBackup != null
            ) {
                try {
                    contentResolver.openOutputStream(data.data!!)?.use {
                        it.write(pendingBackup!!)
                    }
                    Toast.makeText(
                        this, "Backup saved successfully", Toast.LENGTH_LONG
                    ).show()
                } catch (e: Exception) {
                    Toast.makeText(
                        this, "Could not save backup", Toast.LENGTH_LONG
                    ).show()
                }
            }
            pendingBackup = null
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        webView.saveState(outState)
    }

    @Deprecated("Deprecated in Android API")
    override fun onBackPressed() {
        if (::webView.isInitialized && webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
