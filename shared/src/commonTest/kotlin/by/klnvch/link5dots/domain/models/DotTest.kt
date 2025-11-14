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

import kotlin.test.Test
import kotlin.test.assertEquals

class DotTest {
    @Test
    fun testToJson() {
        val dot = createDot(1, 2, 3)
        val json = dot.toJson()
        assertEquals("{\"x\":1,\"y\":2,\"dt\":3}", json)
    }

    @Test
    fun testFromJson() {
        val json = "{\"x\":1,\"y\":2,\"dt\":3}"
        val dot = json.toDot()
        assertEquals(createDot(1, 2, 3), dot)
    }

    @Test
    fun testListToJson() {
        val dots = listOf(createDot(1, 2, 3))
        val json = dots.toJson()
        assertEquals("[{\"x\":1,\"y\":2,\"dt\":3}]", json)
    }

    @Test
    fun testListFromJson() {
        val json = "[{\"x\":1,\"y\":2,\"dt\":3}]"
        val dots = json.toDots()
        assertEquals(listOf(createDot(1, 2, 3)), dots)
    }
}
