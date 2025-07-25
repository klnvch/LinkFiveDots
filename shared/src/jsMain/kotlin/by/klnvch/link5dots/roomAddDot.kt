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

import by.klnvch.link5dots.data.TimeServiceImpl
import by.klnvch.link5dots.data.firebase.OnlineDotRemote
import by.klnvch.link5dots.data.online.AddDotOnlineRoomRepositoryImpl
import by.klnvch.link5dots.data.online.FirebaseDbSetDot
import by.klnvch.link5dots.data.online.FirebaseDbSetState
import by.klnvch.link5dots.data.online.GetOnlineRoomRepositoryImpl
import by.klnvch.link5dots.data.online.UpdateStateOnlineRoomRepositoryImpl
import by.klnvch.link5dots.domain.models.Board
import by.klnvch.link5dots.domain.models.IRoom
import by.klnvch.link5dots.domain.models.NetworkRoom
import by.klnvch.link5dots.domain.models.Point
import by.klnvch.link5dots.domain.repositories.FirebaseAuthManager
import by.klnvch.link5dots.domain.usecases.AddDotOnlineUseCase
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import kotlin.js.Promise
import kotlin.js.json

@OptIn(DelicateCoroutinesApi::class, ExperimentalJsExport::class)
@JsExport()
fun roomAddDot(
    firebaseUserId: String,
    room: IRoom,
    p: Point,
    onDbSet: (key: String, value: Any) -> Promise<Unit>,
): Promise<Unit> {
    val firebaseAuthManager = object : FirebaseAuthManager {
        override fun getUserId() = firebaseUserId
    }
    val timeService = TimeServiceImpl()
    val board = Board()

    val firebaseDb = object : FirebaseDbSetDot, FirebaseDbSetState {
        override suspend fun setDot(
            path: Array<String>,
            dot: OnlineDotRemote,
        ) {
            val jsObject = json().also {
                it["dt"] = dot.dt
                it["x"] = dot.x
                it["y"] = dot.y
            }
            onDbSet(path.joinToString("/"), jsObject).await()
        }

        override suspend fun setState(path: Array<String>, state: Int) {
            onDbSet(path.joinToString("/"), state).await()
        }
    }
    val addDotRepository = AddDotOnlineRoomRepositoryImpl(firebaseDb)
    val updateStateRepository = UpdateStateOnlineRoomRepositoryImpl(firebaseDb)
    val getRepository = GetOnlineRoomRepositoryImpl()
    getRepository.room = room as NetworkRoom

    val useCase = AddDotOnlineUseCase(
        firebaseAuthManager,
        timeService,
        board,
        addDotRepository,
        updateStateRepository,
        getRepository,
    )

    return Promise { resolve, reject -> GlobalScope.launch { useCase.addDot(room, p) } }
}
