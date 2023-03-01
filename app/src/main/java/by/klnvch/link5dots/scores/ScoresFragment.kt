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
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import by.klnvch.link5dots.R
import by.klnvch.link5dots.databinding.FragmentScoresBinding
import by.klnvch.link5dots.models.HighScore
import by.klnvch.link5dots.utils.IntentExt.getHighScore
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth

class ScoresFragment : Fragment() {
    private lateinit var binding: FragmentScoresBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentScoresBinding.inflate(inflater, container, false)

        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.layoutManager = LinearLayoutManager(context)

        return binding.root
    }

    override fun onStart() {
        super.onStart()

        if (FirebaseUtils.isSupported(requireContext())) {
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
            binding.recyclerView.adapter = ScoresAdapter.create()
            // TODO: what if it is not resumed
            startListening()

            // send score if necessary
            val highScore = activity?.intent?.getHighScore()
            if (highScore != null) {
                activity?.intent?.removeExtra(HighScore.TAG)
                FirebaseUtils.publishScore(requireContext(), highScore)
            }
        }
    }

    private fun showError(msg: Int) {
        binding.recyclerView.visibility = View.GONE
        binding.textError.visibility = View.VISIBLE
        binding.textError.text = getString(msg)
    }

    private fun startListening() {
        (binding.recyclerView.adapter as? ScoresAdapter)?.startListening()
    }

    private fun stopListening() {
        (binding.recyclerView.adapter as? ScoresAdapter)?.stopListening()
    }
}
