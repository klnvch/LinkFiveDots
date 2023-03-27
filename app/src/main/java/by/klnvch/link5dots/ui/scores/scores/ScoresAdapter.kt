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

package by.klnvch.link5dots.ui.scores.scores

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import by.klnvch.link5dots.data.firebase.GameScoreRemote
import by.klnvch.link5dots.databinding.ItemHighScoreBinding
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query

class ScoresAdapter(options: FirebaseRecyclerOptions<GameScoreRemote>) :
    FirebaseRecyclerAdapter<GameScoreRemote, ScoresAdapter.HighScoreHolder>(options) {

    override fun onBindViewHolder(holder: HighScoreHolder, position: Int, model: GameScoreRemote) {
        holder.binding.viewState = HighScoreViewState(position, model)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HighScoreHolder {
        val binding =
            ItemHighScoreBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HighScoreHolder(binding)
    }

    class HighScoreHolder(val binding: ItemHighScoreBinding) : RecyclerView.ViewHolder(binding.root)

    companion object {
        private const val LIMIT_TO_FIRST = 500

        private fun getScoresQuery(highScorePath: String): Query {
            return FirebaseDatabase.getInstance().reference
                .child(highScorePath)
                .orderByChild("score")
                .limitToLast(LIMIT_TO_FIRST)
        }

        fun create(highScorePath: String): ScoresAdapter {
            return ScoresAdapter(
                FirebaseRecyclerOptions.Builder<GameScoreRemote>()
                    .setQuery(getScoresQuery(highScorePath), GameScoreRemote::class.java)
                    .build()
            )
        }
    }
}
