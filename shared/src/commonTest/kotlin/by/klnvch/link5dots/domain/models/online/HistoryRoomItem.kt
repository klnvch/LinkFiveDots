/*
 * MIT License
 *
 * Copyright (c) 2026 klnvch
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

package by.klnvch.link5dots.domain.models.online

import kotlin.test.Test
import kotlin.test.assertEquals

class HistoryOnlineRoomItemTest {
    @Test
    fun decode() {
        val expected = HistoryOnlineRoomItemImpl(
            "EmulatorTablet6",
            null,
            1765456648,
            GameResultImpl(9, 10, GameStatus.LostByTimeout),
        )
        val actual = decode("v2J1MW9FbXVsYXRvclRhYmxldDZidTL2YXQaaTq7CGFyv2FzCWFkCmFvYmx0//8=")
        assertEquals(expected, actual)
    }
}
