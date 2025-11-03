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

import by.klnvch.link5dots.data.online.models.OnlineRoomStarted
import kotlin.test.Test
import kotlin.test.assertEquals

class NetworkRoomTest {
    @Test
    fun testEqual() {
        val room1 = OnlineRoomStarted(
            room = NetworkRoom(
                key = "19a3969e6a8_w_1c79cfca",
                time = 1761899833,
                dots = listOf(
                    createDot(x = 8, y = 12, dt = 0),
                    createDot(x = 10, y = 10, dt = 0),
                    createDot(x = 9, y = 8, dt = 0),
                    createDot(x = 11, y = 11, dt = 0),
                    createDot(x = 9, y = 10, dt = 0),
                    createDot(x = 9, y = 11, dt = 0),
                    createDot(x = 8, y = 9, dt = 3436),
                    createDot(x = 10, y = 12, dt = 3462),
                    createDot(x = 10, y = 9, dt = 3508),
                    createDot(x = 7, y = 10, dt = 3516),
                    createDot(x = 10, y = 8, dt = 3655),
                    createDot(x = 7, y = 8, dt = 3673),
                    createDot(x = 11, y = 7, dt = 3751),
                    createDot(x = 7, y = 3, dt = 3775),
                    createDot(x = 11, y = 8, dt = 4414),
                    createDot(x = 5, y = 11, dt = 4505),
                    createDot(x = 9, y = 13, dt = 4575),
                    createDot(x = 8, y = 13, dt = 4615),
                    createDot(x = 12, y = 9, dt = 4644),
                    createDot(x = 8, y = 5, dt = 4701),
                    createDot(x = 10, y = 16, dt = 4832),
                    createDot(x = 1, y = 14, dt = 4835),
                    createDot(x = 17, y = 12, dt = 4837),
                    createDot(x = 6, y = 7, dt = 4854),
                    createDot(x = 13, y = 8, dt = 4860),
                    createDot(x = 11, y = 5, dt = 4871),
                    createDot(x = 14, y = 8, dt = 4884),
                    createDot(x = 4, y = 9, dt = 4900),
                    createDot(x = 7, y = 14, dt = 5556),
                    createDot(x = 12, y = 12, dt = 7265),
                    createDot(x = 12, y = 6, dt = 7393),
                    createDot(x = 5, y = 12, dt = 7452),
                    createDot(x = 12, y = 10, dt = 7458),
                    createDot(x = 8, y = 4, dt = 7567),
                    createDot(x = 15, y = 7, dt = 7572),
                    createDot(x = 14, y = 11, dt = 7606),
                    createDot(x = 14, y = 13, dt = 7609),
                    createDot(x = 6, y = 9, dt = 7611),
                    createDot(x = 6, y = 6, dt = 7617),
                    createDot(x = 8, y = 6, dt = 7630),
                    createDot(x = 12, y = 5, dt = 7839),
                    createDot(x = 9, y = 4, dt = 7844),
                    createDot(x = 10, y = 6, dt = 8176),
                    createDot(x = 5, y = 14, dt = 8209),
                    createDot(x = 15, y = 9, dt = 8217),
                    createDot(x = 7, y = 15, dt = 8222)
                ),
                user1 = createNetworkUser("uX3E1JDYgeZT0EmujKIaQ51sjdz2", "Web"),
                user2 = createNetworkUser("dz2S7s9B1EOuLXpBsG081PB0tKH3", "EmulatorTablet6")
            )
        )
        val room2 = OnlineRoomStarted(
            room = NetworkRoom(
                key = "19a3969e6a8_w_1c79cfca",
                time = 1761899833,
                dots = listOf(
                    createDot(x = 8, y = 12, dt = 0),
                    createDot(x = 10, y = 10, dt = 0),
                    createDot(x = 9, y = 8, dt = 0),
                    createDot(x = 11, y = 11, dt = 0),
                    createDot(x = 9, y = 10, dt = 0),
                    createDot(x = 9, y = 11, dt = 0),
                    createDot(x = 8, y = 9, dt = 3436),
                    createDot(x = 10, y = 12, dt = 3462),
                    createDot(x = 10, y = 9, dt = 3508),
                    createDot(x = 7, y = 10, dt = 3516),
                    createDot(x = 10, y = 8, dt = 3655),
                    createDot(x = 7, y = 8, dt = 3673),
                    createDot(x = 11, y = 7, dt = 3751),
                    createDot(x = 7, y = 3, dt = 3775),
                    createDot(x = 11, y = 8, dt = 4414),
                    createDot(x = 5, y = 11, dt = 4505),
                    createDot(x = 9, y = 13, dt = 4575),
                    createDot(x = 8, y = 13, dt = 4615),
                    createDot(x = 12, y = 9, dt = 4644),
                    createDot(x = 8, y = 5, dt = 4701),
                    createDot(x = 10, y = 16, dt = 4832),
                    createDot(x = 1, y = 14, dt = 4835),
                    createDot(x = 17, y = 12, dt = 4837),
                    createDot(x = 6, y = 7, dt = 4854),
                    createDot(x = 13, y = 8, dt = 4860),
                    createDot(x = 11, y = 5, dt = 4871),
                    createDot(x = 14, y = 8, dt = 4884),
                    createDot(x = 4, y = 9, dt = 4900),
                    createDot(x = 7, y = 14, dt = 5556),
                    createDot(x = 12, y = 12, dt = 7265),
                    createDot(x = 12, y = 6, dt = 7393),
                    createDot(x = 5, y = 12, dt = 7452),
                    createDot(x = 12, y = 10, dt = 7458),
                    createDot(x = 8, y = 4, dt = 7567),
                    createDot(x = 15, y = 7, dt = 7572),
                    createDot(x = 14, y = 11, dt = 7606),
                    createDot(x = 14, y = 13, dt = 7609),
                    createDot(x = 6, y = 9, dt = 7611),
                    createDot(x = 6, y = 6, dt = 7617),
                    createDot(x = 8, y = 6, dt = 7630),
                    createDot(x = 12, y = 5, dt = 7839),
                    createDot(x = 9, y = 4, dt = 7844),
                    createDot(x = 10, y = 6, dt = 8176),
                    createDot(x = 5, y = 14, dt = 8209),
                    createDot(x = 15, y = 9, dt = 8217),
                    createDot(x = 7, y = 15, dt = 8222)
                ),
                user1 = createNetworkUser("uX3E1JDYgeZT0EmujKIaQ51sjdz2", "Web"),
                user2 = createNetworkUser("dz2S7s9B1EOuLXpBsG081PB0tKH3", "EmulatorTablet6")
            )
        )
        assertEquals(room1, room2)
    }
}
