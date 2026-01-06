/*
 * MIT License
 *
 * Copyright (c) 2025-2026 klnvch
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

package by.klnvch.link5dots.online

import by.klnvch.link5dots.data.online.AddDotOnlineRoomRepositoryImpl
import by.klnvch.link5dots.data.online.UpdateStateOnlineRoomRepositoryImpl
import by.klnvch.link5dots.domain.models.Board
import by.klnvch.link5dots.domain.models.INetworkRoom
import by.klnvch.link5dots.domain.models.NetworkUser
import by.klnvch.link5dots.domain.models.Point
import by.klnvch.link5dots.domain.repositories.NetworkUserProvider
import by.klnvch.link5dots.domain.repositories.RoomRemoteRepository
import by.klnvch.link5dots.domain.repositories.online.FirebaseDbSetDot
import by.klnvch.link5dots.domain.repositories.online.FirebaseDbSetState
import by.klnvch.link5dots.domain.usecases.AddDotOnlineUseCase
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import kotlin.js.Promise

@OptIn(DelicateCoroutinesApi::class, ExperimentalJsExport::class)
@JsExport
fun roomAddDot(
    user: NetworkUser?,
    room: INetworkRoom,
    p: Point,
    onDbSet: (key: String, value: Any) -> Promise<Unit>,
    onDbSetDot: (path: String, p: Point) -> Promise<Unit>,
): Promise<Unit> {
    val networkUserProvider = object : NetworkUserProvider {
        override val networkUser = user
    }
    val board = Board()

    val firebaseDb = object : FirebaseDbSetDot, FirebaseDbSetState {
        override suspend fun setDot(path: Array<String>, p: Point) =
            onDbSetDot(path.joinToString("/"), p).await()

        override suspend fun setState(path: Array<String>, state: Int) =
            onDbSet(path.joinToString("/"), state).await()
    }
    val addDotRepository = AddDotOnlineRoomRepositoryImpl(firebaseDb)
    val updateStateRepository = UpdateStateOnlineRoomRepositoryImpl(firebaseDb)
    val roomRemoteRepository = object : RoomRemoteRepository {
        override var room = room
    }

    val useCase = AddDotOnlineUseCase(
        roomRemoteRepository,
        board,
        networkUserProvider,
        addDotRepository,
        updateStateRepository,
    )

    return Promise { resolve, reject ->
        try {
            GlobalScope.launch {
                useCase.addDot(p)
                resolve(Unit)
            }
        } catch (e: Throwable) {
            reject(e)
        }
    }
}
