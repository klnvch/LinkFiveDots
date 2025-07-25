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

import by.klnvch.link5dots.data.firebase.OnlineDotRemote
import by.klnvch.link5dots.data.firebase.OnlineRemoteUser
import by.klnvch.link5dots.data.firebase.OnlineRoomRemote
import by.klnvch.link5dots.data.firebase.mapToOnlineRoomRemote
import kotlin.test.Test
import kotlin.test.assertEquals

class MyJsTests {
    @Test
    fun mapToOnlineRoomRemote() {
        val jsObj = js(
            "({" +
                    "state:2," +
                    "time: 1753106016872," +
                    "dots: [{dt: 35971, x: 5, y: 8,}]," +
                    "user1: {id: '111'}," +
                    "user2: {id: '222', name: 'web user'}" +
                    "})"
        )
        val result = (jsObj as Any).mapToOnlineRoomRemote()
        assertEquals(
            OnlineRoomRemote(
                state = 2,
                time = 1753106016872.0,
                dots = listOf(OnlineDotRemote(35971, 5, 8)),
                user1 = OnlineRemoteUser("111", null),
                user2 = OnlineRemoteUser("222", "web user"),
            ), result
        )
    }
}
