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
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import by.klnvch.link5dots.R
import by.klnvch.link5dots.domain.models.Dot
import by.klnvch.link5dots.domain.models.Point
import by.klnvch.link5dots.domain.models.WinningLineImpl
import by.klnvch.link5dots.domain.models.createDot
import by.klnvch.link5dots.domain.models.createPoint
import by.klnvch.link5dots.ui.game.utils.invertMap
import by.klnvch.link5dots.ui.game.utils.postTranslate
import by.klnvch.link5dots.ui.game.utils.scale
import by.klnvch.link5dots.ui.game.utils.scaleAndTranslate
import by.klnvch.link5dots.ui.theme.dotColorsPalette

@Preview
@Composable
fun GameScreenPreview() {
    GameBoard(
        viewState = GameBoardViewStateImpl(
            dots = arrayOf(
                createDot(0, 0, 0),
                createDot(1, 1, 0),
                createDot(2, 2, 0),
                createDot(3, 3, 0),
                createDot(4, 4, 0),
                createDot(5, 5, 0),
                createDot(6, 6, 0),
                createDot(7, 7, 0),
                createDot(8, 8, 0),
                createDot(9, 9, 0),
                createDot(10, 10, 0),
                createDot(11, 11, 0),
                createDot(12, 12, 0),
                createDot(13, 13, 0),
                createDot(14, 14, 0),
                createDot(15, 15, 0),
                createDot(16, 16, 0),
                createDot(17, 17, 0),
                createDot(18, 18, 0),
                createDot(19, 19, 0),
            ), winningLine = WinningLineImpl(
                listOf(
                    createPoint(5, 5), createPoint(6, 5), createPoint(7, 5), createPoint(8, 5)
                )
            )
        ),
        focus = createPoint(19, 19),
        onMoveDone = { Log.d("GameBoard", it.toString()) },
        onUnfocus = {})
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
    val user1Tint = MaterialTheme.dotColorsPalette.user1.toArgb()
    val user2Tint = MaterialTheme.dotColorsPalette.user2.toArgb()
    val paperColorFilter = MaterialTheme.dotColorsPalette.paperColorFilter

    val arrowsImage = ImageBitmap.imageResource(id = R.drawable.arrows)
    val paperImage = ImageBitmap.imageResource(id = R.drawable.background)
    val paper =
        Paper(viewState.dotsStyleType, paperImage.width, user1Tint, user2Tint)

    val user1Image = paper.user1Dot.toImageBitmap()
    val user2Image = paper.user2Dot.toImageBitmap()
    fun Int.toImage() = if (this % 2 == 0) user1Image else user2Image

    val paperSize = paperImage.width.toFloat()

    var matrix by remember { mutableStateOf(Matrix().apply { scale(density, density) }) }

    val state = rememberTransformableState { zoomChange, offsetChange, _ ->
        matrix = matrix.scaleAndTranslate(zoomChange, offsetChange)
    }

    Canvas(
        modifier = modifier
            .background(Color.DarkGray)
            .transformable(state = state)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { tapOffset ->
                        val paperPosition = matrix.invertMap(tapOffset)
                        onMoveDone(paper.toBoardPosition(paperPosition.x, paperPosition.y))
                    })
            }) {
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
            drawImage(
                image = paperImage,
                colorFilter = paperColorFilter,
            )
            viewState.dots.forEachIndexed { i, dot ->
                drawImage(image = i.toImage(), topLeft = dot.toDotOffset(paper))
            }

            viewState.lastDot?.let {
                drawImage(
                    image = arrowsImage,
                    dstSize = IntSize(37, 37),
                    dstOffset = it.toArrowsOffset(paper)
                )
            }
            viewState.winningLine?.let {
                val color = if (viewState.dots.size % 2 == 1) user1Tint else user2Tint
                val line = paper.toLineOnPaper(it, color)
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
        topLeft = Offset(.0f, .0f), bottomRight = Offset(paperSize, paperSize)
    )

    map(paperRect)

    val dx = dx(screenSize.width, paperRect)
    val dy = dy(screenSize.height, paperRect)

    postTranslate(dx, dy)
}

private fun GameBitmap.toImageBitmap() =
    Bitmap.createBitmap(buffer, size, size, Bitmap.Config.ARGB_8888).asImageBitmap()

private fun PaperPosition.toOffset() = Offset(x, y)
private fun PaperPosition.toIntOffset() = IntOffset(x.toInt(), y.toInt())
private fun Dot.toDotOffset(paper: Paper) = paper.toDotPaperPosition(this).toOffset()
private fun Dot.toArrowsOffset(paper: Paper) = paper.toArrowsPaperPosition(this).toIntOffset()
