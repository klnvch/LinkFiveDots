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

package by.klnvch.link5dots.data.online

import by.klnvch.link5dots.BuildConfig
import by.klnvch.link5dots.data.firebase.OnlineRemoteUser
import by.klnvch.link5dots.data.firebase.mapToOnlineRemoteUser
import by.klnvch.link5dots.data.online.models.CreateOnlineRoomInvitation
import by.klnvch.link5dots.domain.models.Point
import by.klnvch.link5dots.domain.models.RoomState
import com.google.firebase.Firebase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.database
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class FirebaseDbImpl @Inject constructor() : FirebaseDb {
    private val path = if (BuildConfig.DEBUG) "rooms_debug" else "rooms_v2"
    private val reference = Firebase.database.reference.child(path)

    private suspend fun set(path: Array<String>, value: Any) {
        reference.child(path.joinToString(separator = "/")).setValue(value).await()
    }

    private suspend fun update(path: Array<String>, update: Map<String, Any>) {
        reference.child(path.joinToString(separator = "/")).updateChildren(update).await()
    }

    override suspend fun setDot(path: Array<String>, p: Point) {
        val data = mapOf(
            "x" to p.x,
            "y" to p.y,
            "t" to ServerValue.TIMESTAMP,
        )
        reference.child(path.joinToString(separator = "/")).setValue(data).await()
    }

    override suspend fun setConnected(
        path: Array<String>,
        state: Int,
        user2: OnlineRemoteUser,
        dots: List<Point>,
    ) = update(path, mapOf("state" to state, "user2" to user2, "dots" to dots))

    override suspend fun createInvitation(invitation: CreateOnlineRoomInvitation) {
        val data = mapOf(
            "time" to ServerValue.TIMESTAMP,
            "state" to RoomState.CREATED.ordinal,
            "user1" to invitation.user1.mapToOnlineRemoteUser(),
        )
        reference.child(invitation.key).setValue(data).await()
    }

    override suspend fun setState(path: Array<String>, state: Int) = set(path, state)
}
