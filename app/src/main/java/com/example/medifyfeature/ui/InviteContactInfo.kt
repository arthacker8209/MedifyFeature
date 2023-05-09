package com.example.medifyfeature.ui

import android.text.TextUtils
import java.io.Serializable

 class InviteContactsInfo : Serializable {
    var value = ""
    var phoneNumber = ""
    var contactName = ""
    var contactId = ""
    var email = ""
    var isWhatsApp = false
    var isMessageSent = false

    constructor(value: String, phoneNumber: String, contactName: String, isWhatsApp: Boolean) {
        this.value = value
        this.phoneNumber = phoneNumber
        this.contactName = contactName
        this.isWhatsApp = isWhatsApp
    }

    constructor() {}

    fun toContactInfo(): ContactInfo {
        return ContactInfo("", contactName, phoneNumber)
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }

    override fun equals(obj: Any?): Boolean {
        var result = false
        if (this === obj) {
            result = true
        }
        if (obj == null) {
            result = false
        }
        if (this.javaClass != obj!!.javaClass) {
            result = false
        }
        val other = obj as InviteContactsInfo?
        if (value == other!!.value) {
            result = true
        }
        return result
    }

    fun insert(contactId: String, contactName: String, phoneNumber: String, isWhatsApp: Boolean) {
        this.contactId = contactId
        this.contactName = contactName
        this.phoneNumber = phoneNumber
        this.isWhatsApp = isWhatsApp
        if (TextUtils.isEmpty(this.contactName)) {
            this.contactName = phoneNumber
        }
    }


}