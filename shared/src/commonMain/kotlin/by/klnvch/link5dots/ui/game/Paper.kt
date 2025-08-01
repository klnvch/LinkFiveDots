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

package by.klnvch.link5dots.ui.game

import by.klnvch.link5dots.domain.models.Dot
import by.klnvch.link5dots.domain.models.LineOrientation
import by.klnvch.link5dots.domain.models.Point
import by.klnvch.link5dots.domain.models.WinningLine
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport
import kotlin.math.abs

@OptIn(ExperimentalJsExport::class)
@JsExport()
interface PaperPosition {
    val x: Float
    val y: Float
}

@OptIn(ExperimentalJsExport::class)
@JsExport()
interface LineOnPaper {
    val lineBitmap: GameBitmap
    val linePositions: Array<PaperPosition>
}

data class PaperPositionImpl(override val x: Float, override val y: Float) : PaperPosition

class LineOnPaperImpl(
    override val lineBitmap: GameBitmap,
    override val linePositions: Array<PaperPosition>,
) : LineOnPaper

@OptIn(ExperimentalJsExport::class)
@JsExport()
class Paper(sizePx: Int) {
    private val colorRed = -65536
    private val colorBlue = -16776961
    private val gridSize = 20
    private val imageSizePx = 600
    private val arrowsSizePx = 37
    private val scale = sizePx / imageSizePx.toFloat()

    private val lineLocations = FloatArray(gridSize)
    private val dotLocations = FloatArray(gridSize)
    private val arrowsLocations = FloatArray(gridSize)

    val user1Dot = createGameBitmap(BitmapType.DOT, colorRed, scale)
    val user2Dot = createGameBitmap(BitmapType.DOT, colorBlue, scale)

    init {
        val dotSize = user1Dot.size
        for (i in 0 until gridSize) {
            lineLocations[i] = sizePx / (2f * gridSize) + (i * sizePx) / gridSize.toFloat()
            dotLocations[i] = lineLocations[i] - dotSize / 2f
            arrowsLocations[i] = lineLocations[i] - arrowsSizePx / 2f
        }
    }

    fun toLinePaperPosition(p: Point): PaperPosition =
        PaperPositionImpl(dotLocations[p.x], dotLocations[p.y])

    fun toDotPaperPosition(dot: Dot): PaperPosition =
        PaperPositionImpl(dotLocations[dot.x], dotLocations[dot.y])

    fun toArrowsPaperPosition(dot: Dot): PaperPosition =
        PaperPositionImpl(arrowsLocations[dot.x], arrowsLocations[dot.y])

    fun toBoardPosition(x: Float, y: Float) = Point(findClosestIndex(x), findClosestIndex(y))

    fun toLineOnPaper(line: WinningLine): LineOnPaper {
        val color = if (line.type == Dot.HOST) colorRed else colorBlue
        val lineBitmap = when (line.orientation) {
            LineOrientation.HORIZONTAL -> createGameBitmap(BitmapType.LINE_H, color, scale)
            LineOrientation.VERTICAL -> createGameBitmap(BitmapType.LINE_V, color, scale)
            LineOrientation.DIAGONAL_LEFT -> createGameBitmap(BitmapType.LINE_D_R, color, scale)
            LineOrientation.DIAGONAL_RIGHT -> createGameBitmap(BitmapType.LINE_D_L, color, scale)
        }
        val d = when (line.orientation) {
            LineOrientation.HORIZONTAL -> PaperPositionImpl(0f, lineBitmap.size / 2f)
            LineOrientation.VERTICAL -> PaperPositionImpl(lineBitmap.size / 2f, 0f)
            LineOrientation.DIAGONAL_LEFT -> PaperPositionImpl(0f, lineBitmap.size.toFloat())
            LineOrientation.DIAGONAL_RIGHT -> PaperPositionImpl(0f, 0f)
        }
        val positions = line.points
            .dropLast(1)
            .map { PaperPositionImpl(lineLocations[it.x] - d.x, lineLocations[it.y] - d.y) }
            .toTypedArray<PaperPosition>()
        return LineOnPaperImpl(
            lineBitmap,
            positions
        )
    }

    private fun findClosestIndex(p: Float) =
        lineLocations.map { abs(p - it) }.withIndex().minBy { it.value }.index
}
