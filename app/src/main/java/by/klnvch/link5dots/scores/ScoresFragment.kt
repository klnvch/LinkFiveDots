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

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import by.klnvch.link5dots.R
import by.klnvch.link5dots.models.HighScore
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_scores.view.*

class ScoresFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_scores, container, false)

        view.recyclerView.setHasFixedSize(true)
        view.recyclerView.layoutManager = LinearLayoutManager(context)

        return view
    }

    override fun onStart() {
        super.onStart()

        if (FirebaseUtils.isSupported(context!!)) {
            // TODO: StrictMode policy violation
            FirebaseAuth.getInstance().signInAnonymously().addOnCompleteListener { onSignedIn(it) }
        } else {
            showError(R.string.error_feature_not_available)
        }
    }

    override fun onResume() {
        super.onResume()
        startListening()
    }

    override fun onPause() {
        super.onPause()
        stopListening()
    }

    private fun onSignedIn(task: Task<AuthResult>) {
        Log.d("ScoresFragment", "onSignedIn: " + task.exception)
        if (!task.isSuccessful) {
            showError(R.string.connection_error_message)
        } else {
            // init firebase adapter
            // TODO: add loading progress
            view?.recyclerView?.adapter = ScoresAdapter.create()
            // TODO: what if it is not resumed
            startListening()

            // send score if necessary
            val highScore = activity?.intent?.getSerializableExtra(HighScore.TAG)
            if (highScore != null) {
                activity?.intent?.removeExtra(HighScore.TAG)
                FirebaseUtils.publishScore(context!!, highScore as HighScore)
            }
        }
    }

    private fun showError(msg: Int) {
        view?.recyclerView?.visibility = View.GONE
        view?.textError?.visibility = View.VISIBLE
        view?.textError?.text = getString(msg)
    }

    private fun startListening() {
        (view?.recyclerView?.adapter as? ScoresAdapter)?.startListening()
    }

    private fun stopListening() {
        (view?.recyclerView?.adapter as? ScoresAdapter)?.stopListening()
    }
}