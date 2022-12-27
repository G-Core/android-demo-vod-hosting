package com.example.gcore_vod_demo.screens.splash

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.gcore_vod_demo.data.remote.RemoteAccessManager
import com.example.gcore_vod_demo.screens.main.MainActivity
import gcore_vod_demo.R

class SplashFragment : Fragment(R.layout.fragment_splash) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        launchMainScreen(RemoteAccessManager.isAuth(requireActivity().application))
    }

    private fun launchMainScreen(isAuth: Boolean) {
        val intent = Intent(requireContext(), MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }

        intent.putExtra(MainActivity.isAuthKey, isAuth)
        startActivity(intent)
    }
}