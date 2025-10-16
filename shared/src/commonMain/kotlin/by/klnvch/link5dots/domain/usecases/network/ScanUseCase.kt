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

package by.klnvch.link5dots.domain.usecases.network

import by.klnvch.link5dots.data.online.models.AcceptOnlineRoomInvitation
import by.klnvch.link5dots.domain.models.ConnectException
import by.klnvch.link5dots.domain.models.FoundRemoteRoom
import by.klnvch.link5dots.domain.models.gameSeed
import by.klnvch.link5dots.domain.models.generateInitialGame
import by.klnvch.link5dots.domain.models.online.OnlineRoomInvitation
import by.klnvch.link5dots.domain.repositories.NetworkUserProvider
import by.klnvch.link5dots.domain.repositories.StringProvider
import by.klnvch.link5dots.domain.repositories.networkUserOrThrow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

abstract class CommonOnlineFoundRemoteRoom(
    open val onConnect: suspend () -> Unit,
) : FoundRemoteRoom {
    override fun connect(onSuccess: () -> Unit, onError: (Throwable) -> Unit) {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                onConnect()
                onSuccess()
            } catch (e: Throwable) {
                onError(ConnectException(title, e))
            }
        }
    }
}

private class OnlineFoundRemoteRoom(
    invitation: OnlineRoomInvitation,
    defaultTitle: String,
    getAccept: () -> AcceptOnlineRoomInvitation,
) : CommonOnlineFoundRemoteRoom({
    invitation.onConnect(getAccept())
}) {
    override val title = invitation.user1.name ?: defaultTitle
    override val time = invitation.time
    override val description = null
    override val isFavorite = false
}

class ScanOnlineRoomDescriptorFactory(
    private val networkUserProvider: NetworkUserProvider,
    private val stringProvider: StringProvider,
) {
    fun map(invitations: List<OnlineRoomInvitation>): List<FoundRemoteRoom> = invitations.map {
        OnlineFoundRemoteRoom(it, stringProvider.unknownName) {
            AcceptOnlineRoomInvitation(
                user2 = networkUserProvider.networkUserOrThrow,
                dots = generateInitialGame(gameSeed()).toTypedArray(),
            )
        }
    }
}
