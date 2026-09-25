package com.subscription.manager.data.storage

import android.content.Context

object AppContext {
    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    val context: Context
        get() = appContext ?: throw IllegalStateException("AppContext not initialized. Call AppContext.init(context) first.")
}
