/*
 * MIT License
 *
 * Copyright (c) 2025 klnvch
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

package by.klnvch.link5dots.domain.usecases

import by.klnvch.link5dots.domain.formatDuration
import by.klnvch.link5dots.domain.models.online.GameStatus
import by.klnvch.link5dots.domain.models.online.HistoryOnlineRoomItem
import by.klnvch.link5dots.domain.models.online.decode
import by.klnvch.link5dots.domain.repositories.NetworkUserProvider
import by.klnvch.link5dots.domain.repositories.StringProvider
import by.klnvch.link5dots.domain.repositories.networkUserOrThrow
import by.klnvch.link5dots.domain.repositories.online.FirebaseDbGetUserHistory
import by.klnvch.link5dots.formatDateTime

enum class OnlineGameShortInfoStatus { Won, Lost, Draw, InProgress }

data class OnlineGameShortInfo(
    val user1Name: String,          // real name or default
    val user2Name: String,          // real name or default
    val timeText: String,           // e.g. "02:15"
    val sizeText: String,           // e.g. "12"
    val durationText: String,       // e.g. "Dec 18, 05:22 PM"
    val status: OnlineGameShortInfoStatus,
)

fun HistoryOnlineRoomItem.toOnlineGameShortInfo(defaultName: String) = OnlineGameShortInfo(
    user1Name = this.user1Name ?: defaultName,
    user2Name = this.user2Name ?: defaultName,
    timeText = this.time.formatDateTime(),
    sizeText = result?.size?.toString() ?: "…",
    durationText = result?.duration?.formatDuration() ?: "…",
    status = when (result?.status) {
        GameStatus.Won -> OnlineGameShortInfoStatus.Won
        GameStatus.WonByTimeout -> OnlineGameShortInfoStatus.Won
        GameStatus.Lost -> OnlineGameShortInfoStatus.Lost
        GameStatus.LostByTimeout -> OnlineGameShortInfoStatus.Lost
        GameStatus.Draw -> OnlineGameShortInfoStatus.Draw
        null -> OnlineGameShortInfoStatus.InProgress
    }
)

class GetOnlineUserHistoryUseCase(
    private val repository: FirebaseDbGetUserHistory,
    private val networkUserProvider: NetworkUserProvider,
    private val stringProvider: StringProvider,
) {
    suspend fun getUserHistory(): List<OnlineGameShortInfo> {
        try {
            val user = networkUserProvider.networkUserOrThrow
            val path = "users/${user.id}/history"
            return repository.getUserHistory(path)
                .map { decode(it) }
                .map { it.toOnlineGameShortInfo(stringProvider.unknownName) }
        } catch (e: Throwable) {
            println(e.message)
            // can be continued
            return emptyList()
        }
    }
}
