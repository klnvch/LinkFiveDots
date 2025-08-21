package by.klnvch.link5dots.domain.models

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
            DotImpl(1, 1, 0, 0),
            DotImpl(2, 2, 0, 0),
            DotImpl(3, 3, 0, 10),
        )
        assertEquals(0, dots.getDuration(1))
        assertEquals(0, dots.getDuration(0))
        // second dot
        dots = listOf(
            DotImpl(1, 1, 0, 0),
            DotImpl(2, 2, 0, 0),
            DotImpl(3, 3, 0, 10),
            DotImpl(3, 3, 0, 20),
        )
        assertEquals(0, dots.getDuration(1))
        assertEquals(10, dots.getDuration(0))
        // third dot
        dots = listOf(
            DotImpl(1, 1, 0, 0),
            DotImpl(2, 2, 0, 0),
            DotImpl(3, 3, 0, 10),
            DotImpl(3, 3, 0, 20),
            DotImpl(3, 3, 0, 30),
        )
        assertEquals(10, dots.getDuration(1))
        assertEquals(10, dots.getDuration(0))
    }
}
