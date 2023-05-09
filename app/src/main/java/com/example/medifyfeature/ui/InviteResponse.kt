package com.example.medifyfeature.ui

sealed class InviteResponse {
    data class Success(
        val contactNumber: String?,
        val contactName: String?
    ) : InviteResponse() {
        fun inviteMessage(): String {
            return "Hey there! Download Medify app for medical consultation and take your of your family"
        }
    }

    object Error : InviteResponse()
}
