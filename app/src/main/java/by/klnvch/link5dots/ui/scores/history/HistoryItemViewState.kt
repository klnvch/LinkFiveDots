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

package by.klnvch.link5dots.ui.scores.history

import by.klnvch.link5dots.R
import by.klnvch.link5dots.domain.models.RoomExt.getDuration
import by.klnvch.link5dots.models.Room
import by.klnvch.link5dots.utils.FormatUtils.formatDateTime
import by.klnvch.link5dots.utils.FormatUtils.formatDuration

data class HistoryItemViewState(
    val userName1: String?,
    val userName2: String?,
    val startTime: String,
    val duration: String,
    val size: String,
    val typeStringRes: Int
) {
    constructor(room: Room) : this(
        room.user1?.name,
        room.user2?.name,
        room.timestamp.formatDateTime(),
        room.getDuration().formatDuration(),
        room.dots.size.toString(),
        room.type.toStringRes()
    )

    companion object {
        fun Int.toStringRes(): Int {
            return when (this) {
                Room.TYPE_BLUETOOTH -> R.string.bluetooth
                Room.TYPE_NSD -> R.string.menu_local_network
                Room.TYPE_ONLINE -> R.string.menu_online_game
                Room.TYPE_TWO_PLAYERS -> R.string.menu_two_players
                Room.TYPE_BOT -> R.string.app_name
                else -> R.string.unknown
            }
        }
    }
}
