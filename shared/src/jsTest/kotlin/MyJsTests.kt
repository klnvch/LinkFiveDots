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

import by.klnvch.link5dots.domain.models.createNetworkUser
import by.klnvch.link5dots.domain.repositories.StringProvider
import by.klnvch.link5dots.domain.usecases.OnlineGameShortInfo
import by.klnvch.link5dots.formatDateTime
import by.klnvch.link5dots.online.readUserHistory
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.test.runTest
import kotlin.coroutines.resumeWithException
import kotlin.js.Promise
import kotlin.test.Test
import kotlin.test.assertEquals

class MyJsTests {
    @Test
    fun formatDateTime() {
        val actual = 1755519247.formatDateTime()
        assertEquals("Aug 18, 03:14 PM", actual)
    }

    @Test
    fun `readUserHistory returns user history as promised array`() = runTest {
        // ------------------------------------------------------------------
        // Arrange – set up the mocks and test data
        // ------------------------------------------------------------------

        class FakeStringProvider(
            override val botName: String = "bot",
            override val unknownName: String = "unknown",
        ) : StringProvider

        // A fake network user that will be supplied to the function.
        val mockNetworkUser = createNetworkUser(
            id = "user-123",
            name = null,
        )

        // A list of raw strings that the DB would return.
        val dbStrings = listOf(
            "v2J1MW9FbXVsYXRvclRhYmxldDZidTL2YXQaaTq7CGFyv2FzCWFkCmFvYmx0//8=",
            "v2J1MfZidTJic21hdBppS/R8YXK/YXMTYWQYIWFvYXf//w==",
        )

        // The Promise that `onDbRead` will return – it simply resolves with our mock data.
        var onDbReadCalledWith: String? = null
        val fakeOnDbRead: (String) -> Promise<Array<String>> = { path ->
            onDbReadCalledWith = path
            // Return a JS Promise that resolves to a JS array of strings.
            Promise.resolve(dbStrings.toTypedArray())
        }

        // ------------------------------------------------------------------
        // Act – call the function under test
        // ------------------------------------------------------------------
        val promise: Promise<Array<OnlineGameShortInfo>> =
            readUserHistory(
                user = mockNetworkUser,
                stringProvider = FakeStringProvider(),
                onDbRead = fakeOnDbRead
            )

        // Await the JS promise and convert it to a Kotlin array.
        val resultArray = awaitJsPromise(promise)

        // ------------------------------------------------------------------
        // Assert – verify everything happened as expected
        // ------------------------------------------------------------------

        // 1. `onDbRead` was called with the correct path.
        assertEquals("users/user-123/history", onDbReadCalledWith)

        // 2. The returned array has the same number of elements as the DB strings.
        assertEquals(dbStrings.size, resultArray.size)
    }

    /**
     * Utility that bridges Kotlin's `Promise` with `kotlinx.coroutines.test.runTest`.
     *
     * It simply awaits the promise and casts the result into a Kotlin array.
     */
    private suspend fun <T> awaitJsPromise(promise: Promise<Array<T>>): Array<T> =
        suspendCancellableCoroutine { cont ->
            promise.then({ jsArray ->
                @Suppress("UNCHECKED_CAST")
                cont.resume(jsArray, null)
            }, { err ->
                cont.resumeWithException(Throwable(err.toString()))
            })
        }
}
