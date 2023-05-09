package com.example.medifyfeature.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.medifyfeature.ContactUtils
import com.example.medifyfeature.R
import com.example.medifyfeature.common.Utils
import com.example.medifyfeature.databinding.ActivityInviteFriendsBinding
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.UnsupportedEncodingException
import java.net.URLEncoder

@Suppress("TooManyFunctions")
class InviteFriendsActivity : AppCompatActivity(), View.OnClickListener,
    InviteFriendsAdapter.InviteFriendsListener {

    companion object {
        private const val READ_CONTACTS_PERMISSIONS_REQUEST = 1
        const val SENT = "SMS_SENT"
        const val SENT_REQUEST_CODE = 1
        const val MY_PERMISSIONS_REQUEST_SEND_SMS = 2
    }

    private var contactUtils: ContactUtils? = null
    private var inviteContactsInfoList = mutableListOf<InviteContactsInfo>()
    private var totalContactsList: List<InviteContactsInfo> = ArrayList()
    private var whatsAppContactsList: List<InviteContactsInfo> = ArrayList()
    private var inviteFriendsAdapter: InviteFriendsAdapter? = null
    private var jsonArray = JsonArray()
    private var jsonObject = JsonObject()
    private var isSingleInvitation = false
    private var contactNumber = ""
    private var contactName = ""

    private lateinit var binding: ActivityInviteFriendsBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_invite_friends)
        setContentView(binding.root)
        initLowPriorityTask()
        initData()

    }

    private lateinit var startForResult: ActivityResultLauncher<Intent>


    private fun initData() {
        displayLoadingDialog()
        startForResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                if (result.resultCode == Activity.RESULT_OK) {
                    if (inviteFriendsAdapter != null) {
                        inviteFriendsAdapter?.notifyDataSetChanged()
                    }
                }
            }
        getPermissionToReadUserContacts()

        binding.recylerView.layoutManager = LinearLayoutManager(this)
        binding.recylerView.setHasFixedSize(false)
        observeInvitesLD()
    }

     fun initLowPriorityTask() {
        binding.inviteAllLayout.setOnClickListener(this)
        binding.backLayout.setOnClickListener(this)
        binding.inviteWhatsAppLayout.setOnClickListener(this)
        binding.giveAccessLayout.setOnClickListener(this)
    }

    private fun observeInvitesLD() {
        if (!isSingleInvitation) {
            binding.inviteAllLayout.setBackgroundResource(R.drawable.rectangle_invite_all_unselected)
//            binding.inviteAllTextView.text = getString(R.string.invite_sent_all_friends)
            binding.inviteAllTextView.isEnabled = true
        } else {
            inviteFriendsAdapter?.notifyDataSetChanged()
        }
        isSingleInvitation = false
    }

    private fun sendSms(){
        val message = InviteResponse.Success(contactNumber, contactName).inviteMessage()
        val smsIntent = Intent(Intent.ACTION_VIEW)
        smsIntent.data = Uri.parse("sms:+91$contactNumber")
        smsIntent.putExtra("sms_body",message)
        startActivity(smsIntent)
    }

    private fun checkSmsPermission(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.SEND_SMS), MY_PERMISSIONS_REQUEST_SEND_SMS)
        } else {
            sendSms()
        }
    }


override fun onClick(view: View) {
        if (view === binding.inviteAllLayout) {
            displayLoadingDialog()
            Toast.makeText(this, "inviteall",Toast.LENGTH_SHORT).show()
        } else if (view === binding.backLayout) {
            onBackPressed()
            binding.frameLayoutDialog.visibility = View.GONE
        } else if (view === binding.inviteWhatsAppLayout) {
            displayLoadingDialog()
            invite()
        } else if (view === binding.giveAccessLayout) {
            getPermissionToReadUserContacts()
        }
    }

    private fun invite(contactNumber: String? = null, contactName: String? = null) {
                val successResponse = InviteResponse.Success(contactNumber, contactName)
                dismissLoadingDialog()
                if (successResponse.contactName != null && successResponse.contactNumber != null) {
                        try {
                            val i = Intent(Intent.ACTION_VIEW)
                            val url =
                                "https://api.whatsapp.com/send?phone=" +
                                        successResponse.contactNumber + "&text=" +
                                        URLEncoder.encode(
                                            successResponse.inviteMessage(),
                                            "UTF-8"
                                        )
                            i.setPackage("com.whatsapp")
                            i.data = Uri.parse(url)
                            startForResult.launch(i)
                        } catch (e: UnsupportedEncodingException) {
                            e.printStackTrace()
                        }
                } else {
                    Utils.launchWhatsApp(this, applicationContext, successResponse.inviteMessage())
                }
    }

    override fun onInviteSelected(
        contactName: String?,
        contactNumber: String?,
        isWhatsApp: Boolean
    ) {
        if (isWhatsApp) {
            displayLoadingDialog()
            invite(contactName,contactNumber)
        } else {
            isSingleInvitation = true
            jsonObject = JsonObject()
            jsonArray = JsonArray()
            jsonObject.addProperty("contactName", contactName)
            jsonObject.addProperty("contactNumber", contactNumber)
            jsonArray.add(jsonObject)
            if (contactNumber != null  && contactName != null) {
                this.contactNumber = contactNumber
                this.contactName = contactName
                checkSmsPermission()
            }
        }
    }

    private fun getPermissionToReadUserContacts() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            // This will show the standard permission request dialog UI
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(
                    arrayOf(Manifest.permission.READ_CONTACTS),
                    READ_CONTACTS_PERMISSIONS_REQUEST
                )
            }
        } else {
            fetchContactsToInvite()
            binding.frameLayoutDialog.visibility = View.GONE
            binding.invitePermissionView.visibility = View.GONE
            binding.inviteAllLayout.visibility = View.VISIBLE
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == READ_CONTACTS_PERMISSIONS_REQUEST) {
            if (grantResults.size == 1 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                fetchContactsToInvite()
                binding.frameLayoutDialog.visibility = View.GONE
                binding.invitePermissionView.visibility = View.GONE
                binding.inviteAllLayout.visibility = View.VISIBLE
            } else {
                Toast.makeText(this, "Read Contacts permission denied", Toast.LENGTH_SHORT).show()
                binding.invitePermissionView.visibility = View.VISIBLE
                binding.inviteAllLayout.visibility = View.GONE
                binding.frameLayoutDialog.visibility = View.VISIBLE
            }
        }
        else if(requestCode == MY_PERMISSIONS_REQUEST_SEND_SMS){
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                sendSms()
            } else {
                // Permission denied, show an explanation or disable the feature that depends on this permission
            }
        }
        else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun fetchContactsToInvite() {
        lifecycleScope.launch(Dispatchers.IO) {

            contactUtils = ContactUtils(applicationContext)
            //Fetch Contacts
            totalContactsList = contactUtils!!.getAllContacts()

            whatsAppContactsList = contactUtils!!.getWhatsAppContacts()

            inviteContactsInfoList =
                totalContactsList.toMutableList() //Mutable for Filtering

            //Filter Contacts
            filterContacts(totalContactsList, whatsAppContactsList)

            withContext(Dispatchers.Main) {
                dismissLoadingDialog()
                inviteFriendsAdapter =
                    InviteFriendsAdapter(inviteContactsInfoList, this@InviteFriendsActivity)
                binding.recylerView.adapter = inviteFriendsAdapter
            }
        }

    }

    private fun filterContacts(
        totalContactsList: List<InviteContactsInfo>,
        whatsAppContactsList: List<InviteContactsInfo>
    ) {
        val phoneNumbers: MutableSet<InviteContactsInfo> = HashSet()
        for (s in whatsAppContactsList) {
            phoneNumbers.add(
                InviteContactsInfo(
                    s.contactId,
                    s.phoneNumber,
                    s.contactName,
                    s.isWhatsApp
                )
            )
        }
        jsonArray = JsonArray()
        for ((position, s) in totalContactsList.withIndex()) {
            val phoneNo =
                InviteContactsInfo(s.contactId, s.phoneNumber, s.contactName, s.isWhatsApp)
            jsonObject = JsonObject()
            if (phoneNumbers.contains(phoneNo)) {
                phoneNo.contactName = s.contactName
                phoneNo.phoneNumber = s.phoneNumber
                phoneNo.isWhatsApp = true
                inviteContactsInfoList
                inviteContactsInfoList[position] = phoneNo
                jsonObject.addProperty("contactName", s.contactName)
                jsonObject.addProperty("contactNumber", s.phoneNumber)
            } else {
                if (Utils.validateString(phoneNo.value)) {
                    phoneNo.contactName = s.contactName
                    phoneNo.phoneNumber = s.phoneNumber
                    jsonObject.addProperty("contactName", s.contactName)
                    jsonObject.addProperty("contactNumber", s.phoneNumber)
                    phoneNo.isWhatsApp = false
                    inviteContactsInfoList[position] = phoneNo
                }
            }
            jsonArray.add(jsonObject)
        }
    }


    private fun dismissLoadingDialog() {
        binding.progressBar.visibility = View.GONE
    }

    private fun displayLoadingDialog() {
        binding.progressBar.visibility = View.VISIBLE
    }

    override fun onStop() {
        super.onStop()
        dismissLoadingDialog()
    }
}