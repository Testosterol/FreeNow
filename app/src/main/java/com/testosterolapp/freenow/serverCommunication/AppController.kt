package com.testosterolapp.freenow.serverCommunication

import android.content.Context
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley

/**
 * Networking class
 */
class AppController private constructor(private var mContext: Context) {
    private var mRequestQueue: RequestQueue?

    // getApplicationContext() is key, it keeps you from leaking the
    // Activity or BroadcastReceiver if someone passes one in.
    val requestQueue: RequestQueue?
        get() {
            if (mRequestQueue == null) {
                // getApplicationContext() is key, it keeps you from leaking the
                // Activity or BroadcastReceiver if someone passes one in.
                mRequestQueue = Volley.newRequestQueue(this.mContext.applicationContext)
            }
            return mRequestQueue
        }

    fun <T> addToRequestQueue(req: Request<T>?) {
        requestQueue!!.add(req)
    }

    fun cancelPendingRequests(tag: Any?) {
        if (mRequestQueue != null) {
            mRequestQueue!!.cancelAll(tag)
        }
    }

    companion object {
        private var AppControllerSingleton: AppController? = null
        @Synchronized
        fun getInstance(context: Context): AppController? {
            if (AppControllerSingleton == null) {
                AppControllerSingleton = AppController(context)
            }
            return AppControllerSingleton
        }
    }

    init {
        mRequestQueue = requestQueue
    }
}