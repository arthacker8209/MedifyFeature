package com.example.medifyfeature.common

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.example.medifyfeature.ui.InviteFriendsActivity

object Utils {
    fun validateString(s: String?): Boolean {
        return if (s == null || s.equals("null", ignoreCase = true)) {
            false
        } else s.isNotEmpty()
    }

    fun showToast(
        inviteFriendsActivity: InviteFriendsActivity,
        s: String,
        b: Boolean,
        b1: Boolean
    ) {

    }

    fun appInstalledOrNot(context: Context, uri: String?): Boolean {
        val pm: PackageManager = context.packageManager
        val app_installed: Boolean = try {
            pm.getPackageInfo(uri!!, PackageManager.GET_META_DATA)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
        return app_installed
    }

    fun launchWhatsApp(appContext: Context, applicationContext: Context, message: String?) {
        val launchIntent = Intent(Intent.ACTION_SEND)
        launchIntent.putExtra(Intent.EXTRA_TEXT, message)
        launchIntent.type = "text/plain"
        launchIntent.setPackage("com.whatsapp")
        val pm: PackageManager = applicationContext.packageManager
        appContext.startActivity(launchIntent)
    }
}
