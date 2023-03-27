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

package by.klnvch.link5dots.data.firebase

import android.content.Context
import by.klnvch.link5dots.domain.repositories.FirebaseManager
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class FirebaseManagerImpl(private val context: Context) : FirebaseManager {

    override fun isSupported(): Boolean {
        return GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context) ==
                ConnectionResult.SUCCESS
    }

    override suspend fun signInAnonymously() = suspendCancellableCoroutine { continuation ->
        val listener = OnCompleteListener<AuthResult> {
            if (it.isSuccessful) {
                continuation.resume(Unit)
            } else {
                continuation.resumeWithException(it.exception ?: Error("Unknown error"))
            }
        }

        continuation.invokeOnCancellation { FirebaseAuth.getInstance().signOut() }
        FirebaseAuth.getInstance().signInAnonymously().addOnCompleteListener(listener)
    }

    override fun signOut() {
        FirebaseAuth.getInstance().signOut()
    }

    override fun getUserId(): String? = FirebaseAuth.getInstance().currentUser?.uid
}
