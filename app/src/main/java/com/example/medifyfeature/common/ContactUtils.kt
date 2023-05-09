package com.example.medifyfeature

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.provider.ContactsContract
import com.example.medifyfeature.common.Utils
import com.example.medifyfeature.ui.InviteContactsInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlin.coroutines.coroutineContext

interface ContactManager {
    suspend fun getAllContacts(): List<InviteContactsInfo>
    suspend fun getWhatsAppContacts(): List<InviteContactsInfo>
    suspend fun getPhoneContacts(): List<InviteContactsInfo>
}

private const val VALID_PHONE_NO_LENGTH = 13

@Suppress("ReturnCount", "NestedBlockDepth", "LongMethod","ComplexMethod")
class ContactUtils
    (private val context: Context) :
    ContactManager {

    var allContactsList = listOf<InviteContactsInfo>()
    var phoneContactList = listOf<InviteContactsInfo>()
    var whatsappContactsList = listOf<InviteContactsInfo>()

    override suspend fun getAllContacts(): List<InviteContactsInfo> {
        val contacts = mutableListOf<InviteContactsInfo>()

        if(allContactsList.isNotEmpty()) return allContactsList

        //Parallel Fetch phone & whatsapp contacts
        val phoneContactsDef = CoroutineScope(coroutineContext).async { getPhoneContacts() }
        val whatsAppContactsDef = CoroutineScope(coroutineContext).async { getWhatsAppContacts() }

        //Add Phone Contacts
        val phoneContacts = phoneContactsDef.await()
        contacts.addAll(phoneContacts)

        //Add Whatsapp contacts
        val whatsAppContacts = whatsAppContactsDef.await()
        whatsAppContacts.forEach { whatsAppContact ->
            contacts.removeAll { it.phoneNumber == whatsAppContact.phoneNumber }
        }
        contacts.addAll(whatsAppContacts)

        //Sort By Name
        contacts.sortBy { it.contactName }

        //Set local properties
        allContactsList = contacts

        return contacts
    }

    @SuppressLint("Recycle")
    override suspend fun getPhoneContacts(): List<InviteContactsInfo> {

        if(phoneContactList.isNotEmpty()) return phoneContactList

        val contacts = mutableListOf<InviteContactsInfo>()

        val contentResolver: ContentResolver = context.contentResolver

        val cursor = contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            null,
            null,
            null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        )
            ?: return contacts

        while (cursor.moveToNext()) {
            val hasPhoneNumber =
                cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.HAS_PHONE_NUMBER))
                    .toInt()
            if (hasPhoneNumber > 0) {
                val id = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
                val name =
                    cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME))
                        ?: ""
                val inviteContactsInfo =
                    InviteContactsInfo()
                val phoneCursor = contentResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                    arrayOf(id),
                    null
                )
                if (phoneCursor != null && phoneCursor.moveToNext()) {
                    val ccNumber = ContactsContract.CommonDataKinds.Phone.NUMBER
                    val phoneNumber =
                        phoneCursor.getString(phoneCursor.getColumnIndexOrThrow(ccNumber))
                            .replace("[()\\s-]".toRegex(), "")
                    if (phoneNumber.startsWith("+91")) {
                        if (phoneNumber.length == VALID_PHONE_NO_LENGTH) {
                            inviteContactsInfo.insert(id, name, phoneNumber, false)
                        }
                    } else if (phoneNumber.startsWith("0") || phoneNumber.matches("^[6-9].*$".toRegex())) {
                        if (phoneNumber.startsWith("0")) {
                            inviteContactsInfo.insert(
                                id,
                                name,
                                "+91" + phoneNumber.substring(1),
                                false
                            )
                        } else {
                            inviteContactsInfo.insert(id, name, "+91$phoneNumber", false)
                        }
                    } else {
                        inviteContactsInfo.insert(
                            id,
                            name,
                            phoneNumber.replace("(?<=\\+)0*".toRegex(), ""),
                            false
                        )
                    }
                }

                val emailCur = contentResolver.query(
                    ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                    null,
                    ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
                    arrayOf(id),
                    null
                )
                while (emailCur != null && emailCur.moveToNext()) {
                    val email =
                        emailCur.getString(
                            emailCur.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Email.DATA)
                        )
                    if (!email.isNullOrEmpty()) {
                        inviteContactsInfo.email = email
                    }
                }
                emailCur?.close()

                phoneCursor?.close()
                if (Utils.validateString(inviteContactsInfo.phoneNumber)) {
                    contacts.add(inviteContactsInfo)
                }
            }
        }

        //Set Local properties
        phoneContactList = contacts

        return contacts
    }

    @SuppressLint("Recycle")
    override suspend fun getWhatsAppContacts(): List<InviteContactsInfo> {
        if(whatsappContactsList.isNotEmpty()) return whatsappContactsList

        val contacts = mutableListOf<InviteContactsInfo>()

        val cursor = context.contentResolver.query(
            ContactsContract.Data.CONTENT_URI,
            arrayOf(
                ContactsContract.Data.CONTACT_ID,
                ContactsContract.Data.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.Data.DATA1,
                ContactsContract.CommonDataKinds.Phone.TYPE
            ),
            ContactsContract.Data.MIMETYPE + " =? and account_type=?",
            arrayOf("vnd.android.cursor.item/vnd.com.whatsapp.profile", "com.whatsapp"),
            ContactsContract.Data.DISPLAY_NAME + " ASC"
        ) ?: return contacts

        val contactId = cursor.getColumnIndex(ContactsContract.Data.CONTACT_ID)

        while (cursor.moveToNext()) {
            val inviteContactsInfo =
                InviteContactsInfo()
            val id = cursor.getString(contactId)
            val numberW =
                cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Data.DATA1))
            val parts = numberW.split("@".toRegex()).toTypedArray()
            val numberPhone = parts[0]
            val formattedNumber =
                "+" + numberPhone.substring(0, 2) + numberPhone.substring(2, numberPhone.length)
            val name =
                cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME))

            val emailCur = context.contentResolver.query(
                ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                null,
                ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
                arrayOf(id),
                null
            )
            while (emailCur != null && emailCur.moveToNext()) {
                val email =
                    emailCur.getString(
                        emailCur.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Email.DATA)
                    )
                if (!email.isNullOrEmpty()) {
                    inviteContactsInfo.email = email
                }
            }
            emailCur?.close()
            inviteContactsInfo.insert(id, name, formattedNumber, true)
            if (Utils.validateString(inviteContactsInfo.phoneNumber)) contacts.add(
                inviteContactsInfo
            )
        }
        cursor.close()

        //Set Local properties
        whatsappContactsList = contacts

        return contacts
    }
}
