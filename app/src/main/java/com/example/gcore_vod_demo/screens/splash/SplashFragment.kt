package com.example.gcore_vod_demo.screens.splash

import android.os.Bundle
import android.view.View
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.example.gcore_vod_demo.data.remote.RemoteAccessManager
import com.example.gcore_vod_demo.utils.connectivity_state.ConnectivityProvider
import gcore_vod_demo.R

class SplashFragment : Fragment(R.layout.fragment_splash),
    ConnectivityProvider.ConnectivityStateListener {

    private val provider: ConnectivityProvider by lazy {
        ConnectivityProvider.createProvider(
            requireContext()
        )
    }
    private var hasInternet = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        provider.addListener(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (RemoteAccessManager.isAuth(requireActivity())) {
            if (hasInternet) {
                goTo(R.id.action_splashFragment_to_videosFragment)
            } else {
                goTo(R.id.action_splashFragment_to_noInternetFragment)
            }
        } else {
            goTo(R.id.action_splashFragment_to_loginFragment)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        provider.removeListener(this)
    }

    private fun goTo(@IdRes toFragmentId: Int) {
        findNavController().navigate(
            toFragmentId,
            null,
            navOptions {
                anim {
                    enter = R.anim.enter_fragment
                    exit = R.anim.exit_fragment
                    popEnter = R.anim.pop_enter_fragment
                    popExit = R.anim.pop_exit_fragment
                }
                launchSingleTop = true
                popUpTo(R.id.nav_graph_app) { inclusive = true }
            }
        )
    }

    override fun onStateChange(state: ConnectivityProvider.NetworkState) {
        hasInternet =
            (state as? ConnectivityProvider.NetworkState.ConnectedState)?.hasInternet == true
    }
}