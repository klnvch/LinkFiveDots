/*
 * MIT License
 *
 * Copyright (c) 2017 klnvch
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

package by.klnvch.link5dots.scores

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import android.util.Log
import by.klnvch.link5dots.BuildConfig
import by.klnvch.link5dots.models.HighScore
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query

class FirebaseUtils {
    companion object {
        private val CHILD_HIGH_SCORES =
            if (BuildConfig.DEBUG) "high_scores_debug" else "high_scores"
        private const val LIMIT_TO_FIRST = 500

        fun isSupported(context: Context): Boolean {
            return GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context) ==
                    ConnectionResult.SUCCESS
        }

        fun getScoresQuery(): Query {
            return FirebaseDatabase.getInstance().reference
                .child(CHILD_HIGH_SCORES)
                .limitToLast(LIMIT_TO_FIRST)
        }

        @SuppressLint("HardwareIds")
        fun publishScore(context: Context, highScore: HighScore) {
            highScore.userId = FirebaseAuth.getInstance().currentUser!!.uid
            highScore.androidId =
                Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            highScore.code()

            // write to database
            val mDatabase = FirebaseDatabase.getInstance().reference.child(CHILD_HIGH_SCORES)
            val key = mDatabase.push().key

            if (key != null) {
                mDatabase
                    .child(key)
                    .setValue(highScore)
                    .addOnCompleteListener { task ->
                        Log.d("FirebaseUtils", "publishScore: " + task.exception)
                    }
            } else {
                FirebaseCrashlytics.getInstance()
                    .recordException(NullPointerException("Firebase key is null"))
            }
        }
    }
}