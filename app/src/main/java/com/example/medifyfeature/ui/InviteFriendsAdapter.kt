package com.example.medifyfeature.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.medifyfeature.R
import com.example.medifyfeature.databinding.InviteFriendsItemBinding

class InviteFriendsAdapter(
    var inviteContactsInfoList: List<InviteContactsInfo>,
    private val inviteFriendsListener: InviteFriendsListener
) :
    RecyclerView.Adapter<InviteFriendsAdapter.CustomViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: InviteFriendsItemBinding =
            InviteFriendsItemBinding.inflate(inflater, parent, false)
        return CustomViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CustomViewHolder, i: Int) {
        val inviteContactsInfo: InviteContactsInfo = inviteContactsInfoList[i]
        // Set Tag Name
        if (inviteContactsInfo.contactName.isNotEmpty()) {
            holder.binding.contactNameTextView.text = inviteContactsInfo.contactName
        }
        holder.binding.contactNumberTextView.text = inviteContactsInfo.phoneNumber
        if (inviteContactsInfo.isWhatsApp) {
            if (!inviteContactsInfo.isMessageSent) {
                holder.binding.inviteImageView.setBackgroundResource(R.drawable.ic_whatsapp_small)
                holder.binding.relativeInvite.setBackgroundResource(R.drawable.rounded_invite_friends_button_bg)
                holder.binding.relativeInvite.visibility = View.VISIBLE
                holder.binding.invitationSentView.visibility = View.GONE
            } else {
                holder.binding.invitationSentView.visibility = View.VISIBLE
                holder.binding.relativeInvite.visibility = View.GONE
            }
        } else {
            if (!inviteContactsInfo.isMessageSent) {
                holder.binding.inviteImageView.setBackgroundResource(R.drawable.ic_invite_sms)
                holder.binding.relativeInvite.setBackgroundResource(R.drawable.rounded_tags_add_button_bg)
                holder.binding.relativeInvite.visibility = View.VISIBLE
                holder.binding.invitationSentView.visibility = View.GONE
            } else {
                holder.binding.invitationSentView.visibility = View.VISIBLE
                holder.binding.relativeInvite.visibility = View.GONE
            }
        }
        holder.binding.relativeInvite.setOnClickListener {
            inviteFriendsListener.onInviteSelected(
                inviteContactsInfo.phoneNumber,
                inviteContactsInfo.phoneNumber,
                inviteContactsInfo.isWhatsApp
            )
            inviteContactsInfo.isMessageSent = true
        }
    }

    override fun getItemCount(): Int {
        return inviteContactsInfoList.size
    }

    interface InviteFriendsListener {
        fun onInviteSelected(contactName: String?, contactNumber: String?, isWhatsApp: Boolean)
    }

    inner class CustomViewHolder(binding: InviteFriendsItemBinding) :
        RecyclerView.ViewHolder(binding.getRoot()) {
        val binding: InviteFriendsItemBinding

        init {
            this.binding = binding
        }
    }
}
