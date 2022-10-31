package com.example.gcore_vod_demo.screens.videos

import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.*
import com.example.gcore_vod_demo.data.VideoItemModel
import com.example.gcore_vod_demo.data.remote.RemoteAccessManager
import com.example.gcore_vod_demo.data.remote.UploadVideoWorker
import com.example.gcore_vod_demo.data.remote.auth.AuthResponse
import com.example.gcore_vod_demo.data.remote.video.RequestBodyForCreatingVideo
import com.example.gcore_vod_demo.data.remote.video.UploadVideoResponse
import com.example.gcore_vod_demo.data.remote.video.VideoItemResponse
import com.example.gcore_vod_demo.extensions.toTimeFormat
import com.example.gcore_vod_demo.screens.video_player.VideoPlayerFragment
import com.example.gcore_vod_demo.utils.connectivity_state.ConnectivityProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import gcore_vod_demo.R
import gcore_vod_demo.databinding.FragmentVideosBinding
import io.reactivex.disposables.CompositeDisposable

class VideosFragment :
    Fragment(R.layout.fragment_videos),
    ConnectivityProvider.ConnectivityStateListener,
    VideoItemsAdapterListener {

    private val provider: ConnectivityProvider by lazy {
        ConnectivityProvider.createProvider(
            requireContext()
        )
    }
    private var hasInternet = false

    private lateinit var binding: FragmentVideosBinding
    private val compositeDisposable = CompositeDisposable()
    private lateinit var workManager: WorkManager

    private val videoItemsAdapter = VideoItemsAdapter()
    private val videoItems: MutableList<VideoItemModel> = ArrayList()
    private val localVideoItems: MutableList<VideoItemModel> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        provider.addListener(this)
        workManager = WorkManager.getInstance(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentVideosBinding.bind(view)

        configureItemsDisplay()
        loadVideoItems()
    }

    override fun onDestroy() {
        super.onDestroy()
        provider.removeListener(this)
        compositeDisposable.dispose()
        workManager.cancelAllWorkByTag(UPLOAD_VIDEO_TAG)
    }

    private fun configureItemsDisplay() {
        videoItemsAdapter.setListener(this)
        binding.videosRecyclerView.adapter = videoItemsAdapter
        binding.videosRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.videosRecyclerView.addOnScrollListener(recyclerViewScrollListener)

        binding.refresherVideos.setOnRefreshListener {
            if (hasInternet)
                loadVideoItems()
            else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.check_internet),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        binding.recordVideoBtn.setOnClickListener { getCameraPermission() }
        checkCameraAccess()
    }

    private val recyclerViewScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            if (dy > 0) {
                binding.recordVideoBtn.hide()
            } else {
                binding.recordVideoBtn.show()
            }
        }
    }

    private fun checkCameraAccess() {
        if (requireActivity().packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            binding.recordVideoBtn.isEnabled = true
            binding.recordVideoBtn.show()
        } else {
            binding.recordVideoBtn.isEnabled = false
            binding.recordVideoBtn.hide()
        }
    }

    private fun loadVideoItems(page: Int = 1) {
        var currentPage = page
        if (currentPage == 1) {
            binding.refresherVideos.isRefreshing = true
            videoItems.clear()
        }
        compositeDisposable.add(
            RemoteAccessManager.loadVideoItems(requireActivity(), currentPage)
                .subscribe({ videoItemsResponse ->
                    if (videoItemsResponse.isNotEmpty()) {
                        updateVideoItems(videoItemsResponse)
                        loadVideoItems(page = ++currentPage)    //Loading videoItems from the next page
                    } else {
                        updateDataInAdapter()
                    }
                }, {
                    refreshAccessToken(isFromLoadVideoItems = true)
                    Log.e("TAG", "Result ${it.localizedMessage}")
                })
        )
    }

    private fun refreshAccessToken(
        isFromLoadVideoItems: Boolean = false,
        isFromCreateVideo: Boolean = false
    ) {
        compositeDisposable.add(
            RemoteAccessManager.refreshToken(requireActivity())
                .subscribe({ authResponse ->
                    updateTokens(authResponse)
                    if (isFromLoadVideoItems) loadVideoItems()
                    if (isFromCreateVideo) createPlaceForVideoOnServer(creatingVideo, videoLocalUri)
                }, {

                    auth(isFromLoadVideoItems, isFromCreateVideo)
                    Log.e("TAG_refresh", "Result ${it.localizedMessage}")
                })
        )
    }

    private fun auth(
        isFromLoadVideoItems: Boolean = false,
        isFromCreateVideo: Boolean = false
    ) {
        compositeDisposable.add(
            RemoteAccessManager.auth(requireActivity())
                .subscribe({ authResponse ->
                    updateTokens(authResponse)

                    if (isFromLoadVideoItems) loadVideoItems()
                    if (isFromCreateVideo) createPlaceForVideoOnServer(creatingVideo, videoLocalUri)
                }, {
                    findNavController().navigate(
                        R.id.loginFragment,
                        null,
                        navOptions {
                            launchSingleTop = true
                            popUpTo(R.id.nav_graph_app) { inclusive = true }
                        }
                    )
                    Log.e("TAG_auth", "Result ${it.localizedMessage}")
                })
        )
    }

    private fun updateTokens(authResponse: AuthResponse) {
        requireContext().getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE)
            .edit()
            .putString(RemoteAccessManager.REFRESH_TOKEN_KEY, authResponse.refreshAccessToken)
            .putString(RemoteAccessManager.ACCESS_TOKEN_KEY, authResponse.accessToken)
            .apply()
    }

    private fun updateVideoItems(videoItemsResponse: List<VideoItemResponse>) {
        videoItemsResponse.forEach { videoItemResponse ->
            if (localVideoItems.isNotEmpty()) {
                localVideoItems
                    .find { it.videoId == videoItemResponse.videoId }
                    .let {
                        if (it == null) videoItems.add(VideoItemModel.getInstance(videoItemResponse))
                    }
            } else {
                videoItems.add(VideoItemModel.getInstance(videoItemResponse))
            }
        }
    }

    private fun updateDataInAdapter() {
        if (localVideoItems.isNotEmpty()) videoItems.addAll(0, localVideoItems)
        videoItemsAdapter.setData(videoItems)

        binding.refresherVideos.isRefreshing = false
        if (videoItems.isEmpty())
            binding.emptyListTextView.visibility = View.VISIBLE
        else
            binding.emptyListTextView.visibility = View.GONE
    }

    private fun getCameraPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_DENIED
        ) {
            requestPermissionCamera.launch(Manifest.permission.CAMERA)
        } else {
            recordVideoResult.launch(Intent(MediaStore.ACTION_VIDEO_CAPTURE))
            if (!hasInternet) Toast.makeText(
                requireContext(),
                getString(R.string.video_not_sent),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private val requestPermissionCamera =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                recordVideoResult.launch(Intent(MediaStore.ACTION_VIDEO_CAPTURE))
                if (!hasInternet) Toast.makeText(
                    requireContext(),
                    getString(R.string.video_not_sent),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.camera_is_denied),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    private var videoLocalUri: Uri? = null
    private var creatingVideo: RequestBodyForCreatingVideo? = null
    private val recordVideoResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->

        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            videoLocalUri = result.data?.data

            showVideoUploadDialog()
        }
    }

    private fun showVideoUploadDialog() {
        val dialogButton = DialogInterface.OnClickListener { dialog, which ->
            if (which == DialogInterface.BUTTON_POSITIVE) {
                videoLocalUri?.let {
                    requireContext().contentResolver.query(it, null, null, null, null)
                        ?.use { cursor ->
                            cursor.moveToFirst()
                            creatingVideo = RequestBodyForCreatingVideo.getInstance(cursor)
                            createPlaceForVideoOnServer(creatingVideo, videoLocalUri)
                        }
                }
            }
            dialog.dismiss()
        }
        MaterialAlertDialogBuilder(requireContext(), R.style.MaterialAlertDialog_Rounded)
            .setTitle(R.string.upload_video_dialog_title)
            .setPositiveButton(R.string.yes, dialogButton)
            .setNegativeButton(R.string.no, dialogButton)
            .create()
            .show()
    }

    private fun createPlaceForVideoOnServer(
        requestBody: RequestBodyForCreatingVideo?,
        videoLocalUri: Uri?
    ) {
        if (hasInternet) {
            if (requestBody != null && videoLocalUri != null) {
                compositeDisposable.add(
                    RemoteAccessManager.createVideo(requireActivity(), requestBody)
                        .subscribe({ videoItemResponse ->

                            val videoItem = VideoItemModel.getInstance(videoItemResponse)
                            videoItem.videoUri = videoLocalUri.toString()
                            videoItem.videoSizeInMB =
                                VideoItemModel.getVideoSizeInMB(requestBody.videoSizeInByte ?: 0)
                            videoItem.videoDuration =
                                (requestBody.videoDuration ?: 0).toTimeFormat()

                            localVideoItems.add(videoItem)
                            updateDataInAdapter()

                            getUrlAndTokenToUploadVideo(videoItem)
                        }, {
                            refreshAccessToken(isFromCreateVideo = true)
                        })
                )
            }
        } else {
            // Here should be the logic for creating a video on the server in the absence of the Internet,
            // but I have not yet figured out how to implement this.
        }
    }

    private fun getUrlAndTokenToUploadVideo(videoToSend: VideoItemModel) {
        compositeDisposable.add(
            RemoteAccessManager.getUrlAndTokenToUploadVideo(requireActivity(), videoToSend.videoId)
                .subscribe({
                    Log.e("TOKEN_UPLOAD", it.toString())

                    uploadVideo(videoToSend, it)
                }, {
                    Log.e("TOKEN_UPLOAD", it.localizedMessage ?: "")
                })
        )
    }

    private fun uploadVideo(videoToSend: VideoItemModel, uploadVideoResponse: UploadVideoResponse) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val videoData = Data.Builder()
            .putString(
                UploadVideoWorker.UPLOAD_VIDEO_URL,
                "https://${uploadVideoResponse.servers[0].hostname}/upload/"
            )
            .putString(UploadVideoWorker.VIDEO_NAME, uploadVideoResponse.uploadVideo.videoName)
            .putInt(UploadVideoWorker.VIDEO_ID, uploadVideoResponse.uploadVideo.videoId)
            .putString(UploadVideoWorker.VIDEO_TOKEN, uploadVideoResponse.uploadToken)
            .putString(UploadVideoWorker.VIDEO_LOCAL_URI, videoToSend.videoUri)
            .putInt(UploadVideoWorker.CLIENT_ID, uploadVideoResponse.uploadVideo.clientId)
            .build()

        val uploadVideoTask = OneTimeWorkRequest.Builder(UploadVideoWorker::class.java)
            .setConstraints(constraints)
            .setInputData(videoData)
            .addTag(UPLOAD_VIDEO_TAG)
            .build()

        workManager.enqueueUniqueWork(
            uploadVideoResponse.uploadVideo.videoId.toString(),
            ExistingWorkPolicy.KEEP,
            uploadVideoTask
        )
        Toast.makeText(requireContext(), R.string.video_sent_to_server, Toast.LENGTH_SHORT).show()
    }

    override fun onItemClick(position: Int) {
        findNavController().navigate(
            R.id.action_videosFragment_to_videoPlayerFragment,
            bundleOf(
                VideoPlayerFragment.VIDEO_TITLE_KEY to videoItems[position].videoTitle,
                VideoPlayerFragment.VIDEO_URI_KEY to videoItems[position].videoUri
            ),
            navOptions {
                anim {
                    enter = R.anim.enter_fragment
                    exit = R.anim.exit_fragment
                    popEnter = R.anim.pop_enter_fragment
                    popExit = R.anim.pop_exit_fragment
                }
            }
        )
    }

    override fun onStateChange(state: ConnectivityProvider.NetworkState) {
        hasInternet =
            (state as? ConnectivityProvider.NetworkState.ConnectedState)?.hasInternet == true
    }

    companion object {
        private const val UPLOAD_VIDEO_TAG = "uploadVideoTag"
    }

}