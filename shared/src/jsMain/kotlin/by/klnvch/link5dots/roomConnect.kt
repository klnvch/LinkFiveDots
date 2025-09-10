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

import by.klnvch.link5dots.data.firebase.OnlineRemoteUser
import by.klnvch.link5dots.data.online.ConnectOnlineRoomRepositoryImpl
import by.klnvch.link5dots.data.online.FirebaseDbSetConnected
import by.klnvch.link5dots.data.online.OnlineLocalStoreWriter
import by.klnvch.link5dots.domain.models.FoundRemoteRoom
import by.klnvch.link5dots.domain.models.NetworkUser
import by.klnvch.link5dots.domain.models.Point
import by.klnvch.link5dots.domain.models.online.OnlineRoomInvitation
import by.klnvch.link5dots.domain.models.online.OnlineRoomInvitationRemote
import by.klnvch.link5dots.domain.models.online.toOnlineRoomInvitation
import by.klnvch.link5dots.domain.repositories.ConnectOnlineRoomRepository
import by.klnvch.link5dots.domain.repositories.FirebaseAuthManager
import by.klnvch.link5dots.domain.repositories.StringProvider
import by.klnvch.link5dots.domain.repositories.UserNameSettings
import by.klnvch.link5dots.domain.usecases.network.ScanOnlineRoomDescriptorFactory
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.await
import kotlinx.coroutines.promise
import kotlin.js.Json
import kotlin.js.Promise
import kotlin.js.json

@OptIn(ExperimentalJsExport::class)
@JsExport()
data class RemoteInvitation(
    val key: String?,
    val value: dynamic?,
)

@OptIn(DelicateCoroutinesApi::class, ExperimentalJsExport::class)
@JsExport()
fun toDescriptors(
    user2: NetworkUser,
    invitations: Array<RemoteInvitation>,
    defaultName: String,
    onDbUpdate: (key: String, update: Json) -> Promise<Unit>,
    onSaveKey: (key: String) -> Unit,
): Promise<Array<FoundRemoteRoom>> {
    val connectRepository = createConnectRepository(onDbUpdate, onSaveKey)
    val factory = createScanOnlineRoomDescriptorFactory(user2, defaultName)

    val list = invitations.mapNotNull { invitation ->
        map(
            invitation.key,
            invitation.value,
            connectRepository
        )
    }

    return GlobalScope.promise {
        factory.map(list).toTypedArray()
    }
}

@OptIn(DelicateCoroutinesApi::class)
private fun map(
    key: String?,
    value: dynamic?,
    connectRepository: ConnectOnlineRoomRepository,
): OnlineRoomInvitation? {
    val remote = OnlineRoomInvitationRemote(
        value.time as Double?,
        if (value.user1 != null) OnlineRemoteUser(value.user1.id, value.user1.name) else null
    )
    return toOnlineRoomInvitation(
        key,
        remote
    ) { key, accept ->
        connectRepository.connect(key, accept)
    }
}

private fun createScanOnlineRoomDescriptorFactory(
    user2: NetworkUser,
    defaultName: String,
): ScanOnlineRoomDescriptorFactory {
    val userNameSettings = object : UserNameSettings {
        override suspend fun getUserName() = user2.name
    }
    val firebaseAuthManager = object : FirebaseAuthManager {
        override fun getUserId() = user2.id
    }
    val stringProvider = object : StringProvider {
        override val botName = ""
        override val unknownName = defaultName
    }
    return ScanOnlineRoomDescriptorFactory(stringProvider, userNameSettings, firebaseAuthManager)
}

private fun createConnectRepository(
    onDbUpdate: (key: String, update: Json) -> Promise<Unit>,
    onSaveKey: (key: String) -> Unit,
): ConnectOnlineRoomRepository {
    val firebaseDb = object : FirebaseDbSetConnected {
        override suspend fun setConnected(
            path: Array<String>,
            state: Int,
            user2: OnlineRemoteUser,
            dots: List<Point>,
        ) {
            val jsObject = json(
                "state" to state,
                "user2" to json("id" to user2.id, "name" to user2.name),
                "dots" to dots.map { p -> json("x" to p.x, "y" to p.y) }.toTypedArray(),
            )
            onDbUpdate(path.joinToString("/"), jsObject).await()
        }
    }
    val onlineLocalStore = object : OnlineLocalStoreWriter {
        override suspend fun save(key: String) = onSaveKey(key)
    }

    return ConnectOnlineRoomRepositoryImpl(firebaseDb, onlineLocalStore)
}
