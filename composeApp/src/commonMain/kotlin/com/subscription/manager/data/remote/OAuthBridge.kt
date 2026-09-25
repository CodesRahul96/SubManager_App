package com.subscription.manager.data.remote

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object OAuthBridge {
    private val _oauthTokens = MutableSharedFlow<Pair<String, String>>(extraBufferCapacity = 1)
    val oauthTokens: SharedFlow<Pair<String, String>> = _oauthTokens.asSharedFlow()

    fun onAuthTokensReceived(accessToken: String, refreshToken: String) {
        _oauthTokens.tryEmit(Pair(accessToken, refreshToken))
    }
}
