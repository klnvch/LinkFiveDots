/*
 * MIT License
 *
 * Copyright (c) 2025 klnvch
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
import by.klnvch.link5dots.domain.models.FeatureDisabled
import by.klnvch.link5dots.domain.models.UnknownException
import by.klnvch.link5dots.domain.repositories.FirebaseManager
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.Firebase
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class FirebaseManagerImpl @Inject constructor(
    private val context: Context,
) : FirebaseManager {
    override fun isSupported(): Boolean {
        return GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context) ==
                ConnectionResult.SUCCESS
    }

    override suspend fun signInAnonymously() = suspendCancellableCoroutine { cont ->
        val auth = Firebase.auth

        val currentUser = auth.currentUser
        if (currentUser != null) {
            cont.resume(currentUser.uid)
        } else {
            val listener = OnCompleteListener<AuthResult> { task ->
                if (task.isSuccessful) {
                    cont.resume(task.result.user!!.uid)
                } else {
                    val exception = task.exception
                    when (exception) {
                        is FirebaseNetworkException -> cont.resumeWithException(FeatureDisabled())
                        is FirebaseException -> cont.resumeWithException(UnknownException())
                        is Exception -> cont.resumeWithException(exception)
                        else -> cont.resumeWithException(UnknownException())
                    }
                }
            }

            cont.invokeOnCancellation { auth.signOut() }
            auth.signInAnonymously().addOnCompleteListener(listener)
        }
    }

    override fun signOut() = Firebase.auth.signOut()

    override val userId: Flow<String?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener {
            trySend(it.currentUser?.uid)
        }
        Firebase.auth.addAuthStateListener(listener)
        awaitClose {
            Firebase.auth.removeAuthStateListener(listener)
        }
    }
}
