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
import by.klnvch.link5dots.domain.models.FoundRemoteRoom
import by.klnvch.link5dots.domain.models.NetworkUser
import by.klnvch.link5dots.domain.models.gameSeed
import by.klnvch.link5dots.domain.models.generateInitialGame
import by.klnvch.link5dots.domain.models.online.OnlineRoomInvitation
import by.klnvch.link5dots.domain.repositories.FirebaseAuthManager
import by.klnvch.link5dots.domain.repositories.StringProvider
import by.klnvch.link5dots.domain.repositories.UserNameSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ScanOnlineRoomDescriptor(
    override val title: String,
    override val time: Int,
    val onConnect: suspend () -> Unit,
) : FoundRemoteRoom {
    override val description = null
    override val isFavorite = false
    override fun connect(onSuccess: () -> Unit, onError: (Throwable) -> Unit) {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                onConnect()
                onSuccess()
            } catch (e: Throwable) {
                onError(e)
            }
        }
    }
}

class ScanOnlineRoomDescriptorFactory(
    private val stringProvider: StringProvider,
    private val userNameSettings: UserNameSettings,
    private val firebaseAuthManager: FirebaseAuthManager,
) {
    suspend fun map(invitations: List<OnlineRoomInvitation>): List<ScanOnlineRoomDescriptor> {
        val userId = firebaseAuthManager.getUserId()
        val dots = generateInitialGame(gameSeed()).toTypedArray()
        val userName = userNameSettings.getUserName()
        val user2 = NetworkUser(userId, userName)
        val accept = AcceptOnlineRoomInvitation(user2, dots)

        return invitations.map { invitation ->
            ScanOnlineRoomDescriptor(
                invitation.user1.name ?: stringProvider.unknownName,
                invitation.time
            ) { invitation.onConnect(accept) }
        }
    }
}
