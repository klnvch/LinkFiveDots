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

import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import by.klnvch.link5dots.BuildConfig
import by.klnvch.link5dots.R
import by.klnvch.link5dots.models.Room
import by.klnvch.link5dots.utils.RoomUtils
import kotlinx.android.synthetic.main.item_history.view.*

class HistoryAdapter(private val rooms: MutableList<Room>) :
        RecyclerView.Adapter<HistoryAdapter.HighScoreHolder>() {

    var onItemClickListener: OnItemClickListener? = null

    class HighScoreHolder(view: View) : RecyclerView.ViewHolder(view) {
        val root = view
        val textUser1Name: TextView = view.textUser1Name
        val textUser2Name: TextView = view.textUser2Name
        val textTime: TextView = view.textTimeValue
        val textType: TextView = view.textTypeValue
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
        holder.root.setOnClickListener { onItemClickListener?.onItemSelected(rooms[position]) }
        holder.textUser1Name.text = rooms[position].user1?.name
        holder.textUser2Name.text = rooms[position].user2?.name
        holder.textTime.text = RoomUtils.formatStartTime(rooms[position])
        holder.textDuration.text = DateUtils.formatElapsedTime(RoomUtils.getDuration(rooms[position]) / 1000)
        holder.textDots.text = rooms[position].dots.size.toString()
        when (rooms[position].type) {
            Room.TYPE_BLUETOOTH -> holder.textType.setText(R.string.bluetooth)
            Room.TYPE_NSD -> holder.textType.setText(R.string.menu_local_network)
            Room.TYPE_ONLINE -> holder.textType.setText(R.string.menu_online_game)
            Room.TYPE_TWO_PLAYERS -> holder.textType.setText(R.string.menu_two_players)
            Room.TYPE_BOT -> holder.textType.setText(R.string.app_name)
        }
        if (BuildConfig.DEBUG) {
            if (rooms[position].isSend) {
                holder.root.setBackgroundColor(Color.argb(255, 128, 255, 128))
            } else {
                holder.root.setBackgroundColor(Color.argb(255, 255, 128, 128))
            }
        }
    }

    fun removeAt(position: Int) {
        rooms.removeAt(position)
        notifyItemRemoved(position)
    }

    fun insertAt(position: Int, room: Room) {
        rooms.add(position, room)
        notifyItemInserted(position)
    }

    fun get(position: Int): Room {
        return rooms[position]
    }

    override fun getItemCount() = rooms.size

    interface OnItemClickListener {
        fun onItemSelected(room: Room)
    }
}