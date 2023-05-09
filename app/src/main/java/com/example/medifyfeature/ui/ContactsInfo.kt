package com.example.medifyfeature.ui

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class ContactInfo(
    @SerializedName("email")
    val email: String = "",
    @SerializedName("name")
    val name: String,
    @SerializedName("phoneNo")
    val phoneNo: String
) : Parcelable
