package com.example.gcore_vod_demo.screens

import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.gcore_vod_demo.utils.connectivity_state.ConnectivityProvider
import gcore_vod_demo.R

class NoInternetFragment : Fragment(R.layout.fragment_no_internet),
    ConnectivityProvider.ConnectivityStateListener {

    private val provider: ConnectivityProvider by lazy {
        ConnectivityProvider.createProvider(
            requireContext()
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        provider.addListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        provider.removeListener(this)
    }

    override fun onStateChange(state: ConnectivityProvider.NetworkState) {
        val hasInternet =
            (state as? ConnectivityProvider.NetworkState.ConnectedState)?.hasInternet == true

        if (hasInternet) {
            Toast.makeText(
                requireContext(),
                getString(R.string.connection_restored),
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}