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

package by.klnvch.link5dots.online

import by.klnvch.link5dots.domain.models.NetworkUser
import by.klnvch.link5dots.domain.models.online.OnlineRoomLive
import by.klnvch.link5dots.domain.repositories.NetworkUserProvider
import by.klnvch.link5dots.domain.repositories.online.FirebaseDbAddToUserHistory
import by.klnvch.link5dots.domain.repositories.online.OnlineUserHistoryRepository
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import kotlin.js.Promise

@OptIn(ExperimentalJsExport::class, DelicateCoroutinesApi::class)
@JsExport
fun saveToUserHistory(
    prev: OnlineRoomLive?,
    next: OnlineRoomLive,
    user: NetworkUser,
    onDbUpdate: (path: String, value: String) -> Promise<Unit>,
): Promise<Unit> {
    val firebaseDb = object : FirebaseDbAddToUserHistory {
        override suspend fun addToUserHistory(path: String, value: String) {
            onDbUpdate(path, value).await()
        }
    }
    val networkUserProvider = object : NetworkUserProvider {
        override val networkUser = user
    }
    val repository = OnlineUserHistoryRepository(firebaseDb, networkUserProvider)

    return Promise { _, _ -> GlobalScope.launch { repository.save(prev, next) } }
}
