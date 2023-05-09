package com.example.medifyfeature.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.medifyfeature.databinding.FragmentHomeBinding

class HomeFragment: Fragment() {

    companion object{
        const val TAG = "HomeFragment"
        fun newInstance() = HomeFragment()
    }

    private var binding: FragmentHomeBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
       binding = FragmentHomeBinding.inflate(layoutInflater, container , false)
        return binding!!.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.llInviteContacts?.setOnClickListener {
            val intent = Intent(context, InviteFriendsActivity::class.java)
            startActivity(intent)
        }
    }
}