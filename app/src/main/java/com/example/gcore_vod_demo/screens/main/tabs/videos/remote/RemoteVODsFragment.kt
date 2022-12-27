package com.example.gcore_vod_demo.screens.main.tabs.videos.remote

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.navOptions
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gcore_vod_demo.screens.main.tabs.videos.remote.adapter.VideoItemsAdapter
import com.example.gcore_vod_demo.screens.main.tabs.videos.remote.model.RemoteVODsState
import com.example.gcore_vod_demo.screens.main.video_player.VideoPlayerFragment
import com.example.gcore_vod_demo.utils.connectivity_state.ConnectivityProvider
import com.example.gcore_vod_demo.utils.extensions.findTopNavController
import com.example.gcore_vod_demo.utils.viewModelCreator
import gcore_vod_demo.R
import gcore_vod_demo.databinding.FragmentRemoteVodsBinding

class RemoteVODsFragment : Fragment(R.layout.fragment_remote_vods),
    ConnectivityProvider.ConnectivityStateListener {

    private var binding: FragmentRemoteVodsBinding? = null
    private val viewModel: RemoteVODsViewModel by viewModelCreator {
        RemoteVODsViewModel(requireActivity().application)
    }

    private var hasInternet = false
    private val connectivityProvider: ConnectivityProvider by lazy {
        ConnectivityProvider.createProvider(
            requireContext()
        )
    }
    private val videoItemsAdapter = VideoItemsAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        connectivityProvider.addListener(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentRemoteVodsBinding.bind(view)

        initUI(binding!!)
        configureButtons(binding!!)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        connectivityProvider.removeListener(this)
    }

    private fun initUI(binding: FragmentRemoteVodsBinding) {
        binding.remoteVODsRV.apply {
            adapter = videoItemsAdapter
            layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.VERTICAL,
                false
            )
        }

        observeRemoteVideos(binding)
    }

    private fun observeRemoteVideos(binding: FragmentRemoteVodsBinding) {
        viewModel.remoteVideosState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is RemoteVODsState.Empty -> {
                    binding.apply {
                        refresherVODs.isRefreshing = false
                        noVideos.visibility = View.VISIBLE
                        remoteVODsRV.visibility = View.INVISIBLE
                    }
                }
                is RemoteVODsState.Loading -> {
                    binding.refresherVODs.isRefreshing = true
                }
                is RemoteVODsState.Success -> {
                    binding.apply {
                        refresherVODs.isRefreshing = false
                        noVideos.visibility = View.GONE
                        remoteVODsRV.visibility = View.VISIBLE
                    }

                    videoItemsAdapter.setData(state.videos)
                }
                is RemoteVODsState.Error -> {
                    binding.apply {
                        refresherVODs.isRefreshing = false
                        noVideos.visibility = View.VISIBLE
                        remoteVODsRV.visibility = View.INVISIBLE
                    }

                    Toast.makeText(
                        requireContext(),
                        R.string.failed_load_videos,
                        Toast.LENGTH_SHORT
                    ).show()
                }
                is RemoteVODsState.AccessDenied -> {
                    findTopNavController().navigate(
                        R.id.loginFragment,
                        null,
                        navOptions {
                            popUpTo(R.id.main_graph) { inclusive = true }
                        }
                    )
                }
            }
        }
    }

    private fun configureButtons(binding: FragmentRemoteVodsBinding) {
        binding.refresherVODs.setOnRefreshListener {
            if (hasInternet)
                viewModel.getVideos()
            else {
                binding.refresherVODs.isRefreshing = false
                Toast.makeText(
                    requireContext(),
                    getString(R.string.check_internet),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        videoItemsAdapter.setOnItemClickListener {
            findTopNavController().navigate(
                R.id.action_tabsFragment_to_videoPlayerFragment,
                bundleOf(
                    VideoPlayerFragment.VIDEO_TITLE_KEY to it.name,
                    VideoPlayerFragment.VIDEO_URI_KEY to it.uri
                )
            )
        }
    }

    override fun onStateChange(state: ConnectivityProvider.NetworkState) {
        hasInternet =
            (state as? ConnectivityProvider.NetworkState.ConnectedState)?.hasInternet == true
    }
}