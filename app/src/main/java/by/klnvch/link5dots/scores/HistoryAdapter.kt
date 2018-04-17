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
import by.klnvch.link5dots.models.Room
import kotlinx.android.synthetic.main.item_history.view.*

class HistoryAdapter(private val mDataset: MutableList<Room>) :
        RecyclerView.Adapter<HistoryAdapter.HighScoreHolder>() {

    class HighScoreHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textUser1Name: TextView = view.textUser1Name
        val textUser2Name: TextView = view.textUser2Name
        val textTime: TextView = view.textTimeValue
        val textDuration: TextView = view.textDurationValue
        val textDots: TextView = view.textDotsValue
    }

    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): HistoryAdapter.HighScoreHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_history, parent, false)
        return HighScoreHolder(view)
    }

    override fun onBindViewHolder(holder: HighScoreHolder, position: Int) {
        holder.textUser1Name.text = mDataset[position].user1.name
        holder.textUser2Name.text = mDataset[position].user2.name
        holder.textTime.text = mDataset[position].startTime
        holder.textDuration.text = mDataset[position].duration
        holder.textDots.text = mDataset[position].dots.size.toString()
    }

    fun removeAt(position: Int) {
        mDataset.removeAt(position)
        notifyItemRemoved(position)
    }

    fun get(position: Int): Room {
        return mDataset[position]
    }

    override fun getItemCount() = mDataset.size
}