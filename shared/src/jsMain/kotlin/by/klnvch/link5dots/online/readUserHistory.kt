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

import by.klnvch.link5dots.domain.models.NetworkUser
import by.klnvch.link5dots.domain.repositories.StringProvider
import by.klnvch.link5dots.domain.repositories.online.FirebaseDbGetUserHistory
import by.klnvch.link5dots.domain.usecases.GetOnlineUserHistoryUseCase
import by.klnvch.link5dots.domain.usecases.OnlineGameShortInfo
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import kotlin.js.Promise

@OptIn(ExperimentalJsExport::class, DelicateCoroutinesApi::class)
@JsExport
fun readUserHistory(
    user: NetworkUser?,
    stringProvider: StringProvider,
    onDbRead: (path: String) -> Promise<Array<String>>,
): Promise<Array<OnlineGameShortInfo>> {
    val repository = object : FirebaseDbGetUserHistory {
        override suspend fun getUserHistory(path: String): List<String> {
            return onDbRead(path).await().toList()
        }
    }

    val networkUserProvider = Factory.createNetworkUserProvider(user)

    val useCase = GetOnlineUserHistoryUseCase(repository, networkUserProvider, stringProvider)
    return Promise { resolve, reject ->
        try {
            GlobalScope.launch {
                val result = useCase.getUserHistory().toTypedArray()
                resolve(result)
            }
        } catch (e: Throwable) {
            reject(e)
        }
    }
}
