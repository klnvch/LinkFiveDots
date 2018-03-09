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

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import by.klnvch.link5dots.R
import by.klnvch.link5dots.models.HighScore
import kotlinx.android.synthetic.main.item_high_score.view.*
import java.util.*

internal class ScoresAdapter(private val mData: List<HighScore>) : RecyclerView.Adapter<ScoresAdapter.ViewHolder>() {

    private fun ViewGroup.inflate(layoutRes: Int): View {
        return LayoutInflater.from(context).inflate(layoutRes, this, false)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = parent.inflate(R.layout.item_high_score)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val score = mData[position]
        holder.mPosition.text = String.format(Locale.getDefault(), FORMAT_POSITION, position + 1)
        holder.mUserName.text = score.userName
        holder.mMoves.text = String.format(Locale.getDefault(), FORMAT_MOVES, score.moves)
        holder.mTime.text = String.format(Locale.getDefault(), FORMAT_TIME, score.time)
        when (score.status) {
            HighScore.WON -> holder.mStatus.setText(R.string.scores_won)
            HighScore.LOST -> holder.mStatus.setText(R.string.scores_lost)
            else -> holder.mStatus.text = ""
        }
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    internal class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val mPosition: TextView = v.scorePosition
        val mUserName: TextView = v.scoreUsername
        val mMoves: TextView = v.scoreMoves
        val mTime: TextView = v.scoreTime
        val mStatus: TextView = v.scoreStatus
    }

    companion object {
        private const val FORMAT_POSITION = "%d."
        private const val FORMAT_MOVES = "%d"
        private const val FORMAT_TIME = "%d"
    }
}