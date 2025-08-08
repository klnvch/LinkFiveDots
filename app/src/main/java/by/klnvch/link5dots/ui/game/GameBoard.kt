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

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.MutableRect
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import by.klnvch.link5dots.R
import by.klnvch.link5dots.domain.models.Dot
import by.klnvch.link5dots.domain.models.DotImpl
import by.klnvch.link5dots.domain.models.Point
import by.klnvch.link5dots.domain.models.WinningLineImpl
import by.klnvch.link5dots.ui.game.utils.invertMap
import by.klnvch.link5dots.ui.game.utils.postTranslate
import by.klnvch.link5dots.ui.game.utils.scale
import by.klnvch.link5dots.ui.game.utils.scaleAndTranslate

@Preview()
@Composable
fun GameScreenPreview() {
    GameBoard(
        viewState = GameBoardViewStateImpl(
            dots = arrayOf(
                DotImpl(0, 0, Dot.GUEST, 0),
                DotImpl(1, 1, Dot.HOST, 0),
                DotImpl(2, 2, Dot.GUEST, 0),
                DotImpl(3, 3, Dot.HOST, 0),
                DotImpl(4, 4, Dot.GUEST, 0),
                DotImpl(5, 5, Dot.HOST, 0),
                DotImpl(6, 6, Dot.GUEST, 0),
                DotImpl(7, 7, Dot.HOST, 0),
                DotImpl(8, 8, Dot.GUEST, 0),
                DotImpl(9, 9, Dot.HOST, 0),
                DotImpl(10, 10, Dot.GUEST, 0),
                DotImpl(11, 11, Dot.HOST, 0),
                DotImpl(12, 12, Dot.GUEST, 0),
                DotImpl(13, 13, Dot.HOST, 0),
                DotImpl(14, 14, Dot.GUEST, 0),
                DotImpl(15, 15, Dot.HOST, 0),
                DotImpl(16, 16, Dot.GUEST, 0),
                DotImpl(17, 17, Dot.HOST, 0),
                DotImpl(18, 18, Dot.GUEST, 0),
                DotImpl(19, 19, Dot.HOST, 0),
            ),
            winningLine = WinningLineImpl(
                listOf(
                    Point(5, 5),
                    Point(6, 5),
                    Point(7, 5),
                    Point(8, 5)
                ), Dot.HOST
            )
        ),
        focus = Point(19, 19),
        onMoveDone = { Log.d("GameBoard", it.toString()) },
        onUnfocus = {}
    )
}

@Composable
fun GameBoard(
    modifier: Modifier = Modifier,
    viewState: GameBoardViewState,
    focus: Point?,
    onMoveDone: (point: Point) -> Unit,
    onUnfocus: () -> Unit,
) {
    val density = LocalDensity.current.density

    val arrowsImage = ImageBitmap.imageResource(id = R.drawable.arrows)
    val paperImage = ImageBitmap.imageResource(id = R.drawable.background)
    val paper = Paper(viewState.dotsStyleType, paperImage.width)

    val user1Image = paper.user1Dot.toImageBitmap()
    val user2Image = paper.user2Dot.toImageBitmap()

    val paperSize = paperImage.width.toFloat()

    var matrix by remember { mutableStateOf(Matrix().apply { scale(density, density) }) }

    val state = rememberTransformableState { zoomChange, offsetChange, rotationChange ->
        matrix = matrix.scaleAndTranslate(zoomChange, offsetChange)
    }

    Canvas(
        modifier = modifier
            .background(Color.Black)
            .transformable(state = state)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { tapOffset ->
                        val paperPosition = matrix.invertMap(tapOffset)
                        onMoveDone(paper.toBoardPosition(paperPosition.x, paperPosition.y))
                    }
                )
            }
    ) {
        matrix.fixScale(density)
        focus?.let {
            val paperPosition = paper.toLinePaperPosition(it).toOffset()
            val diff = matrix.map(paperPosition)
            val dx = size.width / 2.0f - diff.x
            val dy = size.height / 2.0f - diff.y
            matrix.postTranslate(dx, dy)
            onUnfocus()
        }
        matrix.fixPosition(size, paperSize)

        withTransform({
            transform(matrix)
        }) {
            drawImage(paperImage)
            for (dot in viewState.dots) {
                val dotImage = if (dot.type == Dot.HOST) user1Image else user2Image
                drawImage(dotImage, topLeft = dot.toDotOffset(paper))
            }
            viewState.lastDot?.let {
                drawImage(
                    image = arrowsImage,
                    dstSize = IntSize(37, 37),
                    dstOffset = it.toArrowsOffset(paper)
                )
            }
            viewState.winningLine?.let {
                val line = paper.toLineOnPaper(it)
                val image = line.lineBitmap.toImageBitmap()
                for (seg in line.linePositions) {
                    drawImage(image, topLeft = seg.toOffset())
                }
            }
        }
    }

}

private fun Matrix.fixScale(density: Float) {
    val minScale = 0.2f * density
    val maxScale = 3.0f * density

    val s = if (scale < minScale) minScale / scale
    else if (scale > maxScale) maxScale / scale
    else 1.0f

    scale(s, s)
}

private fun dx(screenWidth: Float, paperRect: MutableRect): Float {
    if (paperRect.width < screenWidth) {
        return screenWidth / 2.0f - paperRect.center.x
    } else {
        if (paperRect.left > 0) {
            return -paperRect.left
        }
        if (paperRect.right < screenWidth) {
            return screenWidth - paperRect.right
        }
    }
    return .0f
}

private fun dy(screenHeight: Float, paperRect: MutableRect): Float {
    if (paperRect.height < screenHeight) {
        return screenHeight / 2.0f - paperRect.center.y
    } else {
        if (paperRect.top > 0) {
            return -paperRect.top
        }
        if (paperRect.bottom < screenHeight) {
            return screenHeight - paperRect.bottom
        }
    }
    return .0f
}

private fun Matrix.fixPosition(screenSize: Size, paperSize: Float) {
    val paperRect = MutableRect(
        topLeft = Offset(.0f, .0f),
        bottomRight = Offset(paperSize, paperSize)
    )

    map(paperRect)

    val dx = dx(screenSize.width, paperRect)
    val dy = dy(screenSize.height, paperRect)

    postTranslate(dx, dy)
}

private fun GameBitmap.toImageBitmap() = Bitmap
    .createBitmap(buffer, size, size, Bitmap.Config.ARGB_8888)
    .asImageBitmap()

private fun PaperPosition.toOffset() = Offset(x, y)
private fun PaperPosition.toIntOffset() = IntOffset(x.toInt(), y.toInt())
private fun Dot.toDotOffset(paper: Paper) = paper.toDotPaperPosition(this).toOffset()
private fun Dot.toArrowsOffset(paper: Paper) = paper.toArrowsPaperPosition(this).toIntOffset()
