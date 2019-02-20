package com.lenddoefl.mobile.offlinepsychodemo

import android.app.AlertDialog
import android.arch.persistence.room.Room
import android.content.Intent
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.os.Bundle
import android.os.Message
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.webkit.*
import android.widget.Toast
import com.lenddoefl.mobile.offlinepsychodemo.database.OfflineAppDatabase
import kotlinx.android.synthetic.main.activity_main.*
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream


class MainActivity : AppCompatActivity() {

    private val TAG = this@MainActivity.javaClass.simpleName
    private lateinit var webview : WebView
    private var loaded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        webview = findViewById(R.id.webview) as WebView

        askPermissions()
        clearWebview(webview)
        initializeWebviewWrapper(webview)


        fab.setOnClickListener { view ->
//            webview.loadUrl("http://www.google.com")

            if (!loaded) {
                loaded = true
                copyFileFromAssets("build.zip")
                unzip(File("$filesDir/www/build.zip"), File("$filesDir/www"))
                webview.loadUrl("file:///data/user/0/com.lenddoefl.mobile.offlinepsychodemo/files/www/build/index.html#/module")
//                webview.loadUrl("file:///android_asset/ryh-module/index.html#/module")
            } else {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                    webview.evaluateJavascript("javascript: " +
                            "showAndroidToast(\"From native android to Web\")", null)
                } else {

                }

            }

        }
    }

    private fun copyFileFromAssets(fileName: String) {
        Log.e("JOEY", "$filesDir/www")
        val dirPath = "$filesDir/www"
        val file = File(dirPath)
        if (!file.exists()) {
            file.mkdirs()
        }

        val assetManager = assets
        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null

        try {
            inputStream = assetManager.open(fileName)
            val outputFile = File(dirPath, fileName)
            outputStream = FileOutputStream(outputFile)
            copyFile(inputStream!!, outputStream)
            Log.e("JOEY", "Saved Successfully!")
            Toast.makeText(this, "Saved Successfully!", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e("JOEY", "Error!")
            Toast.makeText(this, "Error!", Toast.LENGTH_SHORT).show()
        }

    }

    @Throws(IOException::class)
    private fun copyFile(inputStream: InputStream, outputStream: OutputStream) {
        val buffer = ByteArray(1024)
        var read = inputStream.read(buffer)
        while (read != -1) {
            outputStream.write(buffer, 0, read)
            read = inputStream.read(buffer)
        }
    }

    @Throws(IOException::class)
    fun unzip(zipFile: File, targetDirectory: File) {
        val zis = ZipInputStream(BufferedInputStream(FileInputStream(zipFile)))
        zis.use { zis ->
            var ze: ZipEntry? = zis.getNextEntry()
            var count: Int
            val buffer = ByteArray(8192)
            while (ze != null) {
                val file = File(targetDirectory, ze.name)
                Log.e("FILE", file.absolutePath)
                Log.e("DIR", targetDirectory.absolutePath)
                val dir = if (ze.isDirectory) file else file.parentFile
                if (!dir.isDirectory && !dir.mkdirs())
                    throw FileNotFoundException("Failed to ensure directory: " + dir.absolutePath)
                if (ze.isDirectory) {
                    ze = zis.getNextEntry()
                    continue
                }
                val fout = FileOutputStream(file)
                fout.use { fout ->
                    count = zis.read(buffer)
                    while (count != -1) {
                        fout.write(buffer, 0, count)
                        count = zis.read(buffer)
                    }
                }
                ze = zis.getNextEntry()
            }
        }
    }

    fun askPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val permissions = arrayOf("android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE")
            val requestCode = 200
            requestPermissions(permissions, requestCode)
        }
    }

    private fun initializeWebviewWrapper(webView: WebView) {
        webView.addJavascriptInterface(WebAppInterface(this), "Android")
//        webView.settings.setAppCacheEnabled(true)
        webView.settings.allowContentAccess = true
        webView.settings.allowFileAccess = true
        webView.settings.allowFileAccessFromFileURLs = true
        webView.settings.allowUniversalAccessFromFileURLs = true
        webView.settings.domStorageEnabled = true
        webView.settings.javaScriptEnabled = true
//        webView.settings.cacheMode = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG)
        }

        webView.setWebChromeClient(object : WebChromeClient() {
            override fun onCreateWindow(
                view: WebView,
                isDialog: Boolean,
                isUserGesture: Boolean,
                resultMsg: Message
            ): Boolean {
                val newWebView = WebView(this@MainActivity)
                view.addView(newWebView)
                val transport = resultMsg.obj as WebView.WebViewTransport
                transport.webView = newWebView
                resultMsg.sendToTarget()

                newWebView.webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                        Log.e("JOEY2", "url: $url")
                        val browserIntent = Intent(Intent.ACTION_VIEW)
                        browserIntent.data = Uri.parse(url)
                        startActivity(browserIntent)
                        return true
                    }
                }
                return true
            }

            override fun onJsAlert(view: WebView, url: String, message: String, result: JsResult): Boolean {
                return super.onJsAlert(view, url, message, result)
            }

            override fun onProgressChanged(view: WebView, newProgress: Int) {
//                progress.setProgress(newProgress)
                super.onProgressChanged(view, newProgress)
            }

            override fun onCloseWindow(window: WebView) {
                super.onCloseWindow(window)
                Log.d(TAG, "Window closed.")
//                val ft = fragmentManager.beginTransaction()
//                ft.remove(this@WebAuthorizeFragment)
//                ft.commit()
            }
        })

        webView.setWebViewClient(object : WebViewClient() {

            override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
                val activity = this@MainActivity
                if (activity != null) {
                    val builder = AlertDialog.Builder(activity)
                    builder.setTitle("Confirm SSL Certificate")
                    builder.setMessage(error.toString())
                    builder.setPositiveButton(
                        "Continue"
                    ) { dialog, which -> handler.proceed() }
                    builder.setNegativeButton(
                        "Cancel"
                    ) { dialog, which -> handler.cancel() }
                    val dialog = builder.create()
                    dialog.show()
                } else {
                    Log.e(TAG, "onReceivedSslError() activity is null. Dialog not displayed.")
                    Log.e(TAG, "error = " + error.toString())
                }
            }

            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                Log.e("JOEY3", "url: $url")
                webview.loadUrl(url)
                return  false
            }

//            override fun onPageFinished(view: WebView, url: String) {
//                super.onPageFinished(view, url)
//                progress.setProgress(100)
//                progress.setVisibility(View.GONE)
//
//                if (OnboardingConfiguration.Companion.getInstance().isAssistedPsychometrics()) {
//                    if (!isCameraStarted) {
//                        if (ActivityCompat.checkSelfPermission(
//                                getActivity().getApplicationContext(),
//                                Manifest.permission.CAMERA
//                            ) == PackageManager.PERMISSION_GRANTED
//                        ) {
//                            isCameraStarted = startCamera(mCameraConfig)
//                        }
//                    }
//                    // check for questionnaire number
//                    if (!isCameraError && url.contains("&question_number=")) {
//                        no = Utils.INSTANCE.getPsychoQuestionNumber(url)
//                        pstoken = Utils.INSTANCE.getPsychoPSToken(url)
//                        if (no != null && !no.isEmpty()) {
//                            val page = Integer.parseInt(no)
//                            if (page > 0 && page <= 20) {
//                                Log.d(TAG, "Psycho-selfie no:$no")
//                                takePicture()
//                            }
//                        }
//                    } else if (!isPsychoSelfieExplanationLoaded && url.contains("connect/psychometrics")) {
//                        val intent = Intent(getActivity(), PsychometricsIntroActivity::class.java)
//                        startActivity(intent)
//                        isPsychoSelfieExplanationLoaded = true
//                    }// Check if start of psychometrics
//                }
//            }

//            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap) {
//                Log.e("JOEY", "onPageStarted url: $url")
//                progress.setProgress(0)
//                progress.setVisibility(View.VISIBLE)
//                super.onPageStarted(view, url, favicon)
//            }
        })
    }

    private fun clearWebview(webView: WebView) {
        webView.clearFormData()
        webView.clearCache(true)
        webView.clearMatches()
        webView.clearHistory()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun buildDb() {
        val db = Room.databaseBuilder(
            applicationContext,
            OfflineAppDatabase::class.java, "offline-db"
        ).build()
    }
}
