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
import androidx.compose.foundation.layout.fillMaxSize
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
import by.klnvch.link5dots.R
import by.klnvch.link5dots.domain.models.Dot
import by.klnvch.link5dots.domain.models.DotImpl
import by.klnvch.link5dots.domain.models.Point
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
        ),
        onMoveDone = {
            Log.d("GameBoard", it.toString())
        },
    )
}

@Composable
fun GameBoard(viewState: GameBoardViewState, onMoveDone: (point: Point) -> Unit) {
    val density = LocalDensity.current.density

    val paperImage = ImageBitmap.imageResource(id = R.drawable.background)
    val paper = Paper(paperImage.width)

    val user1Image = paper.user1Dot.toImageBitmap()
    val user2Image = paper.user2Dot.toImageBitmap()

    val paperSize = paperImage.width.toFloat()

    var matrix by remember { mutableStateOf(Matrix().apply { scale(density, density) }) }

    val state = rememberTransformableState { zoomChange, offsetChange, rotationChange ->
        matrix = matrix.scaleAndTranslate(zoomChange, offsetChange)
    }

    Canvas(
        modifier = Modifier
            .background(Color.Gray)
            .fillMaxSize()
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
        matrix.fixPosition(size, paperSize)

        withTransform({
            transform(matrix)
        }) {
            drawImage(paperImage)
            for (dot in viewState.dots) {
                val dotImage = if (dot.type == Dot.HOST) user1Image else user2Image
                drawImage(dotImage, topLeft = dot.toOffset(paper))
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
private fun Dot.toOffset(paper: Paper) = paper.toPaperPosition(this).toOffset()
