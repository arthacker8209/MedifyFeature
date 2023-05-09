package com.example.medifyfeature.ui

import android.os.Bundle
import android.view.LayoutInflater
import com.example.medifyfeature.R
import com.example.medifyfeature.common.BaseActivity
import com.example.medifyfeature.databinding.ActivityHomeBinding

class HomeActivity : BaseActivity<ActivityHomeBinding>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
    }

    override fun inflateLayout(layoutInflater: LayoutInflater): ActivityHomeBinding =
        ActivityHomeBinding.inflate(layoutInflater)

    override fun setupView() {
        callHomeFragment()
    }

    private fun callHomeFragment(){
        val fragment = HomeFragment.newInstance()
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.add(R.id.fragment_container,fragment)
        fragmentTransaction.commit()
    }
}
