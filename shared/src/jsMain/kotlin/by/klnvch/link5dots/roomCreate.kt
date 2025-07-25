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

package by.klnvch.link5dots

import by.klnvch.link5dots.data.RoomKeyGeneratorImpl
import by.klnvch.link5dots.data.TimeServiceImpl
import by.klnvch.link5dots.data.firebase.OnlineRoomInvitationRemote
import by.klnvch.link5dots.data.online.CreateOnlineRoomRepositoryImpl
import by.klnvch.link5dots.data.online.FirebaseDbSetRoom
import by.klnvch.link5dots.data.online.OnlineLocalStoreWriter
import by.klnvch.link5dots.domain.repositories.FirebaseAuthManager
import by.klnvch.link5dots.domain.repositories.UserNameSettings
import by.klnvch.link5dots.domain.usecases.network.CreateOnlineRoomUseCase
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import kotlin.js.Promise
import kotlin.js.json

@OptIn(DelicateCoroutinesApi::class, ExperimentalJsExport::class)
@JsExport()
fun roomCreate(
    userName: String?,
    firebaseUserId: String,
    onDbSet: (key: String, value: Any) -> Promise<Unit>,
): Promise<String> {
    val userNameSettings = object : UserNameSettings {
        override suspend fun getUserName() = userName
    }
    val timeService = TimeServiceImpl()
    val roomKeyGenerator = RoomKeyGeneratorImpl(timeService)
    val firebaseAuthManager = object : FirebaseAuthManager {
        override fun getUserId() = firebaseUserId
    }

    val firebaseDb = object : FirebaseDbSetRoom {
        override suspend fun setInvitation(
            path: Array<String>,
            invitation: OnlineRoomInvitationRemote,
        ) {
            val jsObject = json().also {
                it["state"] = invitation.state
                it["time"] = invitation.time
                it["user1"] = json().also { userJson ->
                    userJson["id"] = invitation.user1?.id
                    userJson["name"] = invitation.user1?.name
                }
            }
            onDbSet(path.joinToString("/"), jsObject).await()
        }
    }

    return Promise { resolve, reject ->
        val onlineLocalStore = object : OnlineLocalStoreWriter {
            override suspend fun save(key: String) = resolve(key)
        }

        val repository = CreateOnlineRoomRepositoryImpl(firebaseDb, onlineLocalStore)

        val useCase = CreateOnlineRoomUseCase(
            userNameSettings,
            timeService,
            roomKeyGenerator,
            firebaseAuthManager,
            repository,
        )

        GlobalScope.launch { useCase.create() }
    }
}
