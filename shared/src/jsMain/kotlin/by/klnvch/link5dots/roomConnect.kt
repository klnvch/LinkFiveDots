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

import by.klnvch.link5dots.data.online.ConnectOnlineRoomRepositoryImpl
import by.klnvch.link5dots.data.online.FirebaseDbUpdate
import by.klnvch.link5dots.data.online.OnlineLocalStoreWriter
import by.klnvch.link5dots.domain.models.RemoteRoomDescriptor
import by.klnvch.link5dots.domain.repositories.FirebaseAuthManager
import by.klnvch.link5dots.domain.repositories.UserNameSettings
import by.klnvch.link5dots.domain.usecases.network.ConnectOnlineRoomUseCase
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import kotlin.js.Json
import kotlin.js.Promise
import kotlin.js.json

@OptIn(DelicateCoroutinesApi::class, ExperimentalJsExport::class)
@JsExport()
fun roomConnect(
    userName: String?,
    firebaseUserId: String,
    descriptor: RemoteRoomDescriptor,
    onUpdateRoom: (key: String, update: Json) -> Promise<Unit>,
): Promise<String> {
    val userNameSettings = object : UserNameSettings {
        override suspend fun getUserName() = userName
    }
    val firebaseAuthManager = object : FirebaseAuthManager {
        override fun getUserId() = firebaseUserId
    }
    val firebaseDb = object : FirebaseDbUpdate {
        override suspend fun update(path: Array<String>, update: Map<String, Any>) {
            val jsObject = json()
            update.forEach { (key, value) -> jsObject[key] = value }
            onUpdateRoom(path.joinToString("/"), jsObject).await()
        }
    }

    return Promise { resolve, reject ->
        val onlineLocalStore = object : OnlineLocalStoreWriter {
            override suspend fun save(key: String) = resolve(key)
        }

        val repository = ConnectOnlineRoomRepositoryImpl(firebaseDb, onlineLocalStore)

        val useCase = ConnectOnlineRoomUseCase(userNameSettings, firebaseAuthManager, repository)

        GlobalScope.launch { useCase.connect(descriptor) }
    }
}
