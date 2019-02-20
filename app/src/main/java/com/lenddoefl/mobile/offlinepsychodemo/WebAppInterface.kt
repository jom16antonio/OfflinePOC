package com.lenddoefl.mobile.offlinepsychodemo

import android.content.Context
import android.webkit.JavascriptInterface
import android.widget.Toast

/**
 * Created by Joey Mar Antonio on 22/01/2019.
 */
class WebAppInterface(private val mContext: Context) {

    /** Show a toast from the web page  */
    @JavascriptInterface
    fun showToast(toast: String) {
        Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show()
    }

}