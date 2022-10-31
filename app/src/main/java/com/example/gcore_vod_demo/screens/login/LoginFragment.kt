package com.example.gcore_vod_demo.screens.login

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.util.Patterns
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.example.gcore_vod_demo.data.remote.RemoteAccessManager
import com.example.gcore_vod_demo.data.remote.auth.AuthRequestBody
import com.example.gcore_vod_demo.data.remote.auth.AuthResponse
import com.example.gcore_vod_demo.extensions.TextWatcherAfterTextCallback
import com.example.gcore_vod_demo.extensions.afterTextChangedListener
import com.example.gcore_vod_demo.utils.connectivity_state.ConnectivityProvider
import gcore_vod_demo.R
import gcore_vod_demo.databinding.FragmentLoginBinding
import io.reactivex.disposables.CompositeDisposable
import java.util.regex.Pattern

class LoginFragment : Fragment(R.layout.fragment_login),
    ConnectivityProvider.ConnectivityStateListener {

    private val provider: ConnectivityProvider by lazy {
        ConnectivityProvider.createProvider(
            requireContext()
        )
    }
    private var hasInternet = false

    private lateinit var binding: FragmentLoginBinding
    private val compositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        provider.addListener(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentLoginBinding.bind(view)

        binding.etEmail.afterTextChangedListener(eMailAfterTextListener)
        binding.etPassword.afterTextChangedListener(passwordAfterTextListener)

        binding.loginButton.setOnClickListener {
            binding.loginButton.isEnabled = false
            hideKeyboard()

            if (!hasInternet) {
                binding.loginButton.isEnabled = true
                showToast(R.string.check_internet)
            } else {
                auth()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        provider.removeListener(this)
        compositeDisposable.dispose()
    }

    private var eMailIsCorrect = false
    private var passwordIsCorrect = false
    private val eMailAfterTextListener = object : TextWatcherAfterTextCallback {
        override fun afterTextChanged(editable: Editable?) {
            eMailIsCorrect = isValidEmail(editable ?: "")
            binding.loginButton.isEnabled = eMailIsCorrect && passwordIsCorrect
        }
    }

    private fun isValidEmail(email: CharSequence) = Patterns.EMAIL_ADDRESS.matcher(email).matches()
        .also {
            if (it) {
                binding.tilEmail.error = ""
            } else {
                binding.tilEmail.error = getString(R.string.etEmail_is_incorrect)
            }
        }

    private val passwordAfterTextListener = object : TextWatcherAfterTextCallback {
        override fun afterTextChanged(editable: Editable?) {
            passwordIsCorrect = isValidPassword(editable ?: "")
            binding.loginButton.isEnabled = eMailIsCorrect && passwordIsCorrect
        }
    }

    private fun isValidPassword(password: CharSequence) =
        Pattern.compile(passwordValidateRegex).matcher(password).matches()
            .also {
                if (it) {
                    binding.tilPassword.error = ""
                } else {
                    binding.tilPassword.error = getString(R.string.etPassword_is_incorrect)
                }
            }

    private fun auth() {
        val requestBody = AuthRequestBody(
            eMail = binding.etEmail.text.toString(),
            password = binding.etPassword.text.toString()
        )
        try {
            compositeDisposable.add(
                RemoteAccessManager
                    .auth(requireActivity(), requestBody)
                    .subscribe({ authResponse ->

                        showToast(R.string.logged_success)
                        saveAuthData(requestBody, authResponse)
                        routeToVideos()

                        Log.e("LoginFragmentSuccess", "Result ${authResponse.accessToken}")
                    }, { throwable ->

                        showToast(R.string.logged_fail)
                        binding.etPassword.text?.clear()
                        binding.loginButton.isEnabled = false

                        Log.e("LoginFragmentError", "Result ${throwable.localizedMessage}")
                    })
            )
        } catch (e: Exception) {
            Log.e("LoginActivityException", "Exception: ${e.message}")
        }
    }

    private fun saveAuthData(requestBody: AuthRequestBody, authResponse: AuthResponse) {
        requireContext().getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE)
            .edit()
            .putString(RemoteAccessManager.EMAIL_KEY, requestBody.eMail)
            .putString(RemoteAccessManager.PASSWORD_KEY, requestBody.password)
            .putString(RemoteAccessManager.REFRESH_TOKEN_KEY, authResponse.refreshAccessToken)
            .putString(RemoteAccessManager.ACCESS_TOKEN_KEY, authResponse.accessToken)
            .apply()
    }

    private fun routeToVideos() {
        findNavController().navigate(
            R.id.action_loginFragment_to_videosFragment,
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

    private fun hideKeyboard() {
        requireActivity().currentFocus.let { view ->
            if (view != null) {
                (requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                    .hideSoftInputFromWindow(view.windowToken, 0)
            }
        }
    }

    override fun onStateChange(state: ConnectivityProvider.NetworkState) {
        hasInternet =
            (state as? ConnectivityProvider.NetworkState.ConnectedState)?.hasInternet == true
    }

    private fun showToast(resId: Int) {
        Toast.makeText(requireContext(), resId, Toast.LENGTH_SHORT).show()
    }

    companion object {
        /*
         * (?=.*[0-9]) - string contains at least one number;
         * (?=.*[!@#$%^&*]) - string contains at least one special character;
         * (?=.*[a-z]) - string contains at least one lowercase Latin letter;
         * (?=.*[A-Z]) - string contains at least one uppercase latin letter;
         * [0-9a-zA-Z!@#$%^&*]{8,} - the string consists of at least 8 of the above characters.
         */
        private const val passwordValidateRegex =
            """(?=.*[0-9])(?=.*[!@#${'$'}%^&*])(?=.*[a-z])(?=.*[A-Z])[0-9a-zA-Z!@#${'$'}%^&*]{8,}"""
    }
}