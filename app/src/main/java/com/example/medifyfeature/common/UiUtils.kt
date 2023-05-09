package com.example.medifyfeature.common

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.os.Handler
import android.os.Process
import android.view.Gravity
import android.widget.Toast
import androidx.fragment.app.Fragment

object UiUtils {
    private val context: Context
        get() = context.applicationContext

    private var mainthreadId = 0

    private val mainThreadId: Int
        get() = mainthreadId

    var mainThreadHandler: Handler? = null
    private val handler: Handler
        get() = mainThreadHandler!!

    fun post(runnable: Runnable?): Boolean {
        return handler.post(runnable!!)
    }

    val resources: Resources
        get() = context.resources

    fun getString(resId: Int): String {
        return resources.getString(resId)
    }

    val isRunInMainThread: Boolean
        get() = Process.myTid().toInt() == mainThreadId

    fun runInMainThread(runnable: Runnable) {
        if (isRunInMainThread) {
            runnable.run()
        } else {
            post(runnable)
        }
    }

    fun showToastSafe(resId: Int) {
        showToastSafe(getString(resId))
    }

    fun showToastSafe(str: String?) {
        if (str == null || str.length == 0) {
            return
        }
        if (isRunInMainThread) {
            showToast(str)
        } else {
            post { showToast(str) }
        }
    }

    private fun showToast(str: String) {
        if (str.length == 0) {
            return
        }
            val toast = Toast.makeText(context, str, Toast.LENGTH_SHORT)
            toast.setGravity(Gravity.CENTER, 0, 0)
            toast.show()
    }

    fun showToast(fragment: Fragment, message: String?) {
        val activity: Activity? = fragment.activity
        if (fragment.isAdded && activity != null) {
            showToastSafe(message)
        }
    }
}