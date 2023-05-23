/*
 * MIT License
 *
 * Copyright (c) 2023 klnvch
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package by.klnvch.link5dots.data.nsd

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import by.klnvch.link5dots.data.nsd.NsdExt.registerService
import by.klnvch.link5dots.data.nsd.NsdParams.TAG
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class NsdRegistration @Inject constructor(context: Context) {
    private val nsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager
    private var nsdRegistrationListener: NsdManager.RegistrationListener? = null

    suspend fun register(localPort: Int) = suspendCancellableCoroutine { continuation ->
        val listener = object : NsdManager.RegistrationListener {
            override fun onRegistrationFailed(info: NsdServiceInfo, errorCode: Int) {
                Log.e(TAG, "onRegistrationFailed: $errorCode")
                continuation.resumeWithException(Error("$TAG error: $errorCode"))
            }

            override fun onUnregistrationFailed(info: NsdServiceInfo, errorCode: Int) {
                Log.e(TAG, "onRegistrationFailed: $errorCode")
            }

            override fun onServiceRegistered(info: NsdServiceInfo) = continuation.resume(info)
            override fun onServiceUnregistered(info: NsdServiceInfo) = Unit
        }

        nsdRegistrationListener = listener
        nsdManager.registerService(localPort, listener)
    }

    fun unregister() {
        if (nsdRegistrationListener != null) {
            try {
                nsdManager.unregisterService(nsdRegistrationListener)
            } catch (e: Throwable) {
                Log.e(TAG, "unregisterService: ${e.message}")
            } finally {
                nsdRegistrationListener = null
            }
        }
    }
}
