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

package by.klnvch.link5dots.domain.models

import by.klnvch.link5dots.domain.models.online.OnlineRoomStarted
import kotlin.test.Test
import kotlin.test.assertEquals

class NetworkRoomTest {
    @Test
    fun testEqual() {
        // dot
        val dot1 = createDot(x = 8, y = 12, dt = 0)
        val dot2 = createDot(x = 8, y = 12, dt = 0)
        assertEquals(dot1, dot2)

        val dots1 = listOf(createDot(x = 8, y = 12, dt = 0))
        val dots2 = listOf(createDot(x = 8, y = 12, dt = 0))
        assertEquals(dots1, dots2)

        // network user
        val user1 = createNetworkUser("uX3E1JDYgeZT0EmujKIaQ51sjdz2", "Web")
        val user2 = createNetworkUser("uX3E1JDYgeZT0EmujKIaQ51sjdz2", "Web")
        assertEquals(user1, user2)

        val room1 = NetworkRoom(
            key = "19a3969e6a8_w_1c79cfca",
            time = 1761899833,
            dots = listOf(
                createDot(x = 8, y = 12, dt = 0),
                createDot(x = 10, y = 10, dt = 0),
                createDot(x = 9, y = 8, dt = 0),
                createDot(x = 11, y = 11, dt = 0),
            ),
            user1 = createNetworkUser("uX3E1JDYgeZT0EmujKIaQ51sjdz2", "Web"),
            user2 = createNetworkUser("dz2S7s9B1EOuLXpBsG081PB0tKH3", "EmulatorTablet6")
        )
        val room2 = NetworkRoom(
            key = "19a3969e6a8_w_1c79cfca",
            time = 1761899833,
            dots = listOf(
                createDot(x = 8, y = 12, dt = 0),
                createDot(x = 10, y = 10, dt = 0),
                createDot(x = 9, y = 8, dt = 0),
                createDot(x = 11, y = 11, dt = 0),
            ),
            user1 = createNetworkUser("uX3E1JDYgeZT0EmujKIaQ51sjdz2", "Web"),
            user2 = createNetworkUser("dz2S7s9B1EOuLXpBsG081PB0tKH3", "EmulatorTablet6")
        )
        assertEquals(room1, room2)

        // online room
        val state1 = OnlineRoomStarted(room = room1)
        val state2 = OnlineRoomStarted(
            room = NetworkRoom(
                key = "19a3969e6a8_w_1c79cfca",
                time = 1761899833,
                dots = listOf(
                    createDot(x = 8, y = 12, dt = 0),
                    createDot(x = 10, y = 10, dt = 0),
                    createDot(x = 9, y = 8, dt = 0),
                    createDot(x = 11, y = 11, dt = 0),
                ),
                user1 = createNetworkUser("uX3E1JDYgeZT0EmujKIaQ51sjdz2", "Web"),
                user2 = createNetworkUser("dz2S7s9B1EOuLXpBsG081PB0tKH3", "EmulatorTablet6")
            )
        )
        assertEquals(state1, state2)
    }
}
