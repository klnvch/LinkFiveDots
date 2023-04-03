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
import by.klnvch.link5dots.data.StringProvider
import by.klnvch.link5dots.domain.models.*
import by.klnvch.link5dots.utils.FormatUtils.formatDateTime
import by.klnvch.link5dots.utils.FormatUtils.formatDuration

data class HistoryItemViewState(
    val room: IRoom,
    val userName1: String?,
    val userName2: String?,
    val startTime: String,
    val duration: String,
    val size: String,
    val typeStringRes: Int,
) {
    constructor(room: IRoom, userName: String, stringProvider: StringProvider) : this(
        room,
        room.user1?.map(userName, stringProvider),
        room.user2?.map(userName, stringProvider),
        room.timestamp.formatDateTime(),
        room.getDuration().formatDuration(),
        room.dots.size.toString(),
        room.type.toStringRes()
    )

    companion object {
        private fun Int.toStringRes(): Int {
            return when (this) {
                RoomType.BLUETOOTH -> R.string.bluetooth
                RoomType.NSD -> R.string.menu_local_network
                RoomType.ONLINE -> R.string.menu_online_game
                RoomType.TWO_PLAYERS -> R.string.menu_two_players
                RoomType.BOT -> R.string.menu_single_player
                else -> R.string.unknown
            }
        }

        private fun IUser.map(userName: String, stringProvider: StringProvider): String {
            return when (this) {
                is DeviceOwnerUser -> userName.ifEmpty { stringProvider.getString(R.string.unknown) }
                is BotUser -> stringProvider.getString(R.string.computer)
                is NetworkUser -> name
            }
        }
    }
}
