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
import android.app.Activity
import android.os.Bundle
import android.provider.Settings
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import by.klnvch.link5dots.BuildConfig
import by.klnvch.link5dots.R
import by.klnvch.link5dots.models.HighScore
import by.klnvch.link5dots.multiplayer.utils.online.FirebaseHelper
import by.klnvch.link5dots.settings.SettingsUtils
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_scores.*

class ScoresActivity : AppCompatActivity() {

    private var mAuth: FirebaseAuth = FirebaseAuth.getInstance()

    private var mFetchReference = FirebaseDatabase.getInstance().reference

    private val mFetchListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            Log.d(TAG, "onDataChange:onCancelled")
            val scores = dataSnapshot
                    .children.mapNotNull { it.getValue(HighScore::class.java) }
            recyclerView.adapter = ScoresAdapter(scores)
            swipeRefreshLayout.isRefreshing = false
        }

        override fun onCancelled(databaseError: DatabaseError) {
            Log.d(TAG, "updateData:onCancelled", databaseError.toException())
            recyclerView.adapter = null
            swipeRefreshLayout.isRefreshing = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scores)
        setTitle(R.string.scores_title)

        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)

        swipeRefreshLayout.isRefreshing = true
        swipeRefreshLayout.setOnRefreshListener { swipeRefreshLayout.isRefreshing = false }

        if (!FirebaseHelper.isSupported(this)) {
            setResult(Activity.RESULT_CANCELED)
            finish()
        } else {
            setResult(Activity.RESULT_OK)
        }
    }

    public override fun onStart() {
        super.onStart()
        connect()
    }

    public override fun onStop() {
        super.onStop()
        mFetchReference.removeEventListener(mFetchListener)
        mAuth.signOut()
    }

    private fun connect() {
        mAuth.signInAnonymously().addOnCompleteListener({ onSignedIn(it) })
    }

    private fun onSignedIn(task: Task<AuthResult>) {
        Log.d(TAG, "onSignedIn:onComplete:" + task.isSuccessful)
        if (task.isSuccessful) {
            sendData()
            updateData()
        } else {
            swipeRefreshLayout.isRefreshing = false
            textNetworkError.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        }
    }

    private fun updateData() {
        Log.d(TAG, "updateData")
        //
        textNetworkError.visibility = View.GONE
        swipeRefreshLayout.isRefreshing = true
        //
        mFetchReference
                .child(CHILD_HIGH_SCORES)
                .orderByChild(HighScore.SCORE)
                .limitToFirst(LIMIT_TO_FIRST)
                .addListenerForSingleValueEvent(mFetchListener)
    }

    private fun sendData() {
        val highScore = intent.getSerializableExtra(HighScore.TAG)
        if (highScore != null) {
            publishScore(highScore as HighScore)
        }
    }

    @SuppressLint("HardwareIds")
    private fun publishScore(highScore: HighScore) {
        // fill high score
        highScore.userId = mAuth.currentUser!!.uid
        highScore.androidId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        highScore.userName = SettingsUtils.getUserNameOrDefault(this)
        highScore.code()

        // write to database
        val mDatabase = FirebaseDatabase.getInstance().reference
        val key = mDatabase.child(CHILD_HIGH_SCORES).push().key
        mDatabase
                .child(CHILD_HIGH_SCORES)
                .child(key)
                .setValue(highScore)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        intent.removeExtra(HighScore.TAG)
                        Log.d(TAG, "add success")
                    } else {
                        Log.d(TAG, "add error")
                    }
                }
    }

    companion object {
        private const val TAG = "ScoresActivity"

        private val CHILD_HIGH_SCORES = if (BuildConfig.DEBUG) "high_scores_debug" else "high_scores"
        private const val LIMIT_TO_FIRST = 500
    }
}