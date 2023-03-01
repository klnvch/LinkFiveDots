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

import android.text.format.DateUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import by.klnvch.link5dots.R
import by.klnvch.link5dots.databinding.ItemHighScoreBinding
import by.klnvch.link5dots.models.HighScore
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.DatabaseError
import java.util.*

class ScoresAdapter(options: FirebaseRecyclerOptions<HighScore>) :
    FirebaseRecyclerAdapter<HighScore, ScoresAdapter.HighScoreHolder>(options) {

    override fun onBindViewHolder(holder: HighScoreHolder, position: Int, model: HighScore) {
        holder.binding.scorePosition.text =
            String.format(Locale.getDefault(), FORMAT_POSITION, position + 1)
        holder.binding.scoreUsername.text = model.username
        holder.binding.scoreMoves.text =
            String.format(Locale.getDefault(), FORMAT_MOVES, model.moves)
        holder.binding.scoreTime.text = DateUtils.formatElapsedTime(model.time)
        when (model.status) {
            HighScore.WON -> holder.binding.scoreStatus.setText(R.string.scores_won)
            HighScore.LOST -> holder.binding.scoreStatus.setText(R.string.scores_lost)
            else -> holder.binding.scoreStatus.text = null
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HighScoreHolder {
        val binding =
            ItemHighScoreBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HighScoreHolder(binding)
    }

    class HighScoreHolder(val binding: ItemHighScoreBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onDataChanged() {
        super.onDataChanged()
        Log.d("ScoresAdapter", "onDataChanged")
    }

    override fun onError(error: DatabaseError) {
        super.onError(error)
        Log.e("ScoresAdapter", "onError: " + error.message)
    }

    companion object {
        private const val FORMAT_POSITION = "%d."
        private const val FORMAT_MOVES = "%d"

        fun create(): ScoresAdapter {
            return ScoresAdapter(
                FirebaseRecyclerOptions.Builder<HighScore>()
                    .setQuery(FirebaseUtils.getScoresQuery(), HighScore::class.java)
                    .build()
            )
        }
    }
}
