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

import by.klnvch.link5dots.ui.game.getDuration
import kotlin.test.Test
import kotlin.test.assertEquals

class RoomExtTest {
    @Test
    fun getDuration() {
        var dots: List<Dot>? = null
        // null
        assertEquals(0, dots.getDuration(0))
        // empty
        dots = emptyList()
        assertEquals(0, dots.getDuration(0))
        // first dot
        dots = listOf(
            createDot(1, 1, 0),
            createDot(2, 2, 0),
            createDot(3, 3, 10),
        )
        assertEquals(0, dots.getDuration(1))
        assertEquals(0, dots.getDuration(0))
        // second dot
        dots = listOf(
            createDot(1, 1, 0),
            createDot(2, 2, 0),
            createDot(3, 3, 10),
            createDot(3, 3, 20),
        )
        assertEquals(0, dots.getDuration(1))
        assertEquals(10, dots.getDuration(0))
        // third dot
        dots = listOf(
            createDot(1, 1, 0),
            createDot(2, 2, 0),
            createDot(3, 3, 10),
            createDot(3, 3, 20),
            createDot(3, 3, 30),
        )
        assertEquals(10, dots.getDuration(1))
        assertEquals(10, dots.getDuration(0))
    }
}
