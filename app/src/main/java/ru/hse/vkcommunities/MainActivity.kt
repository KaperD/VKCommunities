package ru.hse.vkcommunities

import android.app.AlertDialog
import android.os.Bundle
import android.webkit.WebViewClient
import android.widget.Button
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import com.vk.api.sdk.VK
import com.vk.api.sdk.auth.VKAuthenticationResult
import com.vk.api.sdk.auth.VKScope
import com.vk.api.sdk.exceptions.VKAuthException

class MainActivity : AppCompatActivity() {
    private lateinit var authLauncher: ActivityResultLauncher<Collection<VKScope>>
    private val scopes = arrayListOf(VKScope.GROUPS)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (VK.isLoggedIn()) {
            CommunitiesActivity.startFrom(this)
            finish()
            return
        }

        setContentView(R.layout.activity_main)

        authLauncher = VK.login(this) { result: VKAuthenticationResult ->
            when (result) {
                is VKAuthenticationResult.Success -> onLogin()
                is VKAuthenticationResult.Failed -> onLoginFailed(result.exception)
            }
        }
        val loginButton = findViewById<Button>(R.id.login)
        loginButton.setOnClickListener {
            authLauncher.launch(scopes)
        }
    }

    private fun onLogin() {
        CommunitiesActivity.startFrom(this)
        finish()
    }

    private fun onLoginFailed(exception: VKAuthException) {
        if (!exception.isCanceled) {
            val descriptionResource =
                if (exception.webViewError == WebViewClient.ERROR_CONNECT) R.string.connection_error
                else R.string.unknown_error
            AlertDialog.Builder(this)
                .setMessage(descriptionResource)
                .setPositiveButton(R.string.retry) { _, _ ->
                    authLauncher.launch(scopes)
                }
                .setNegativeButton(R.string.cancel) { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
    }
}
