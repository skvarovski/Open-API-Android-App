package com.codingwithmitch.openapi.ui.auth


import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.animation.TranslateAnimation
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.afollestad.materialdialogs.MaterialDialog

import com.codingwithmitch.openapi.R
import com.codingwithmitch.openapi.ui.auth.ForgotPasswordFragment.WebAppInterface.*
import com.codingwithmitch.openapi.ui.auth.state.AuthDataState.*
import com.codingwithmitch.openapi.util.Constants
import kotlinx.android.synthetic.main.fragment_forgot_password.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch


class ForgotPasswordFragment : BaseAuthFragment() {

    lateinit var webView: WebView

    val webInteractionCallback = object: OnWebInteractionCallback {

        override fun onError(errorMessage: String) {
            Log.e(TAG, "onError: $errorMessage")

            activity?.also{
                MaterialDialog(it)
                    .title(R.string.text_error)
                    .message(text = errorMessage){
                        lineSpacing(2F)
                    }
                    .positiveButton(R.string.text_ok)
                    .show()
            }
        }

        override fun onSuccess(email: String) {
            Log.d(TAG, "onSuccess: a reset link will be sent to $email.")
            onPasswordResetLinkSent()
        }

        override fun onLoading(isLoading: Boolean) {
            CoroutineScope(Main).launch {
                if(isLoading){
                    viewModel.setDataState(data_state = Loading)
                } else{
                    viewModel.setDataState(data_state = Data(null))
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_forgot_password, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        webView = view.findViewById(R.id.webview)

        loadPasswordResetWebView()

        return_to_launcher_fragment.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    fun loadPasswordResetWebView(){
        viewModel.setDataState(data_state = Loading)
        webView.webViewClient = object: WebViewClient(){
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                viewModel.setDataState(data_state = Data(null))
            }
        }
        webView.loadUrl(Constants.PASSWORD_RESET_URL)
        webView.settings.javaScriptEnabled = true
        webView.addJavascriptInterface(WebAppInterface(webInteractionCallback), "AndroidTextListener")
    }

    fun onPasswordResetLinkSent(){
        CoroutineScope(Main).launch{
            parent_view.removeView(webView)
            webView.destroy()

            val animation = TranslateAnimation(
                password_reset_done_container.width.toFloat(),
                0f,
                0f,
                0f
            )
            animation.duration = 500
            password_reset_done_container.startAnimation(animation)
            password_reset_done_container.visibility = View.VISIBLE
        }
    }


    class WebAppInterface constructor(val callback: OnWebInteractionCallback) {

        private val TAG: String = "AppDebug"

        @JavascriptInterface
        fun onSuccess(email: String) {
            callback.onSuccess(email)
        }

        @JavascriptInterface
        fun onError(errorMessage: String) {
            callback.onError(errorMessage)
        }

        @JavascriptInterface
        fun onLoading(isLoading: Boolean) {
            callback.onLoading(isLoading)
        }

        interface OnWebInteractionCallback{

            fun onSuccess(email: String)

            fun onError(errorMessage: String)

            fun onLoading(isLoading: Boolean)
        }

    }
}














