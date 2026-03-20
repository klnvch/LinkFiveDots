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

package by.klnvch.link5dots.domain.models.bot

import by.klnvch.link5dots.domain.models.Board
import by.klnvch.link5dots.domain.models.Point
import by.klnvch.link5dots.domain.models.createPoint


private data class Dot(val x: Int, val y: Int, val type: Int) {
    constructor(p: Point, i: Int) : this(p.x, p.y, if (i % 2 == 0) HOST else GUEST)

    val isEmpty = type == EMPTY

    companion object {
        const val EMPTY = 1
        const val HOST = 2
        const val GUEST = 4
    }
}

class Bot(private val board: Board) {
    //temporary table for dots rate
    private val net1: Array<IntArray> = Array(board.width) { IntArray(board.height) }
    private val net2: Array<IntArray> = Array(board.width) { IntArray(board.height) }

    fun findAnswer(dots: List<Point>): Point {
        val net = Array(board.width) { i ->
            Array(board.height) { j ->
                Dot(i, j, Dot.EMPTY)
            }
        }

        dots.forEachIndexed { i, p -> net[p.x][p.y] = Dot(p, i) }

        var maxUserRate = -1f
        val listUser = ArrayList<Dot>()
        var maxBotRate = -1f
        val listBot = ArrayList<Dot>()

        for (i in 0..<board.width) {
            for (j in 0..<board.height) {
                if (net[i][j].isEmpty) {
                    val userRate = getDotRate(net, net[i][j], Dot.HOST)
                    net1[i][j] = userRate.toInt()
                    val botRate = getDotRate(net, net[i][j], Dot.GUEST)
                    net2[i][j] = botRate.toInt()

                    //user rates
                    if (userRate == maxUserRate) {
                        listUser.add(net[i][j])
                    } else if (userRate > maxUserRate) {
                        maxUserRate = userRate
                        listUser.clear()
                        listUser.add(net[i][j])
                    }

                    //bot rates
                    if (botRate == maxBotRate) {
                        listBot.add(net[i][j])
                    } else if (botRate > maxBotRate) {
                        maxBotRate = botRate
                        listBot.clear()
                        listBot.add(net[i][j])
                    }
                } else {
                    net2[i][j] = -1
                    net1[i][j] = net2[i][j]
                }
            }
        }

        var max = -1f
        val resultList = ArrayList<Dot>()

        if (maxBotRate >= maxUserRate) {
            for (dot in listBot) {
                if (max == net1[dot.x][dot.y].toFloat()) {
                    resultList.add(dot)
                } else if (max < net1[dot.x][dot.y]) {
                    max = net1[dot.x][dot.y].toFloat()
                    resultList.clear()
                    resultList.add(dot)
                }
            }
        } else {
            for (dot in listUser) {
                if (max == net2[dot.x][dot.y].toFloat()) {
                    resultList.add(dot)
                } else if (max < net2[dot.x][dot.y]) {
                    max = net2[dot.x][dot.y].toFloat()
                    resultList.clear()
                    resultList.add(dot)
                }
            }
        }

        var result: Dot? = null
        max = -1f

        //density checking
        for (off in resultList) {
            val temp = getDensity(net, off.x, off.y)
            if (temp > max) {
                max = temp.toFloat()
                result = off
            }
        }

        if (result == null) {
            throw RuntimeException("Bot error")
        }

        return createPoint(result.x, result.y)
    }

    private fun getDotRate(net: Array<Array<Dot>>, dot: Dot, type: Int): Float {
        val array2 = IntArray(4)
        //
        array2[0] = getDotRateInLine(net, dot, type, 1, 0).toInt()
        array2[1] = getDotRateInLine(net, dot, type, 1, 1).toInt()
        array2[2] = getDotRateInLine(net, dot, type, 0, 1).toInt()
        array2[3] = getDotRateInLine(net, dot, type, -1, 1).toInt()

        //it is only one dot or building a line is not impossible
        if (array2[0] <= 10 && array2[1] <= 10 && array2[2] <= 10 && array2[3] <= 10) {
            return 0f
        }

        //bingo bot has win
        if (array2[0] >= 100 || array2[1] >= 100 || array2[2] >= 100 || array2[3] >= 100) {
            return 100000000f
        }

        //bingo bot has win
        //if(array2[0]>=99 || array2[1]>=99 || array2[2]>=99 || array2[3]>=99){
        //	return 9999;
        //}
        for (i in 0..3) {
            array2[i] *= array2[i]
        }

        return (array2[0] + array2[1] + array2[2] + array2[3]).toFloat()
    }

    private fun getDensity(net: Array<Array<Dot>>, x: Int, y: Int): Int {
        var result = 0

        for (i in -4..4) {
            for (j in -4..4) {
                if (board.isInside(
                        createPoint(
                            x + i,
                            y + j
                        )
                    ) && net[x + i][y + j].type == Dot.HOST
                ) {
                    result += if (i > 3 || i < -3 || j > 3 || j < -3) {
                        1
                    } else if (i > 2 || i < -2 || j > 2 || j < -2) {
                        3
                    } else if (i > 1 || i < -1 || j > 1 || j < -1) {
                        9
                    } else {
                        27
                    }
                }
            }
        }

        return result
    }

    private fun getDotRateInLine(
        net: Array<Array<Dot>>,
        dot: Dot,
        type: Int,
        dx: Int,
        dy: Int,
    ): Float {
        val x = dot.x
        val y = dot.y

        val array = IntArray(9)

        //init the 9-length array with types of dots
        for (i in -4..4) {
            if (board.isInside(createPoint(x + dx * i, y + dy * i))) {
                array[i + 4] = net[x + dx * i][y + dy * i].type
            } else {
                array[i + 4] = 0
            }
        }
        array[4] = type

        //check array in masks
        val result = checkMasks(array, type)
        if (result != -1.0f) {
            return result
        }

        //check 5 variants
        // 0-not exist; type-is the same; EMPTY-is empty
        //in the 5-length array can be only the same type and EMPTY
        var max = 0

        for (i in 0..4) {
            var count = 0
            for (j in 0..4) {
                if (array[i + j] == type) { //the same type, increase count
                    ++count
                } else if (array[i + j] == Dot.EMPTY) { //empty, do not increase
                } else { //there is something bad
                    count = 0
                    break
                }
            }
            if (count > max) max = count
        }

        return if (max >= 5) 100.0f
        else if (max == 4) 60.0f
        else if (max == 3) 50.0f
        else if (max == 2) 30.0f
        else if (max == 1) 10.0f
        else 0.0f
    }

    /*
     * check line in masks array and if it matches return line rate
     * in the other case return -1.0f
     */
    private fun checkMasks(line: IntArray, type: Int): Float {
        for (i in 0..4) {
            if (checkMasks(line, masks[i], type)) return 100.0f
        }

        for (i in 5..8) {
            if (checkMasks(line, masks[i], type)) return 99.0f
        }

        for (i in 13..22) {
            if (checkMasks(line, masks[i], type)) return 40.0f
        }

        for (i in 23..33) {
            if (checkMasks(line, masks[i], type)) return 20.0f
        }

        for (i in 9..13) {
            if (checkMasks(line, masks[i], type)) return 0.0f
        }

        return -1.0f
    }

    /*
     * compare one line to one masks
     *
     * if line satisfy to mask return true
     */
    private fun checkMasks(line: IntArray, mask: IntArray, type: Int): Boolean {
        //type - my dots number
        for (i in 0..8) {
            when (mask[i]) {
                0 -> if (line[i] != Dot.EMPTY) return false
                1 -> if (line[i] != type) return false
                2 -> if (line[i] == Dot.EMPTY || line[i] == type) return false
                3 -> {}
            }
        }
        return true
    }

    override fun toString(): String {
        val result = StringBuilder()
        for (i in 0..<board.width) {
            for (j in 0..<board.height) {
                result.append(net1[i][j]).append(" ")
            }
            result.append("\n")
        }
        result.append("\n")
        for (i in 0..<board.width) {
            for (j in 0..<board.height) {
                result.append(net2[i][j]).append(" ")
            }
            result.append("\n")
        }
        return result.toString()
    }

    companion object {
        //masks
        private val masks = arrayOf( // 0 - empty, 1 - my, 2 - not my, 3 - any
            //my wins - 100%
            intArrayOf(1, 1, 1, 1, 1, 3, 3, 3, 3),
            intArrayOf(3, 1, 1, 1, 1, 1, 3, 3, 3),
            intArrayOf(3, 3, 1, 1, 1, 1, 1, 3, 3),
            intArrayOf(3, 3, 3, 1, 1, 1, 1, 1, 3),
            intArrayOf(3, 3, 3, 3, 1, 1, 1, 1, 1),
            //my is near win - 99%
            intArrayOf(0, 1, 1, 1, 1, 0, 3, 3, 3),
            intArrayOf(3, 0, 1, 1, 1, 1, 0, 3, 3),
            intArrayOf(3, 3, 0, 1, 1, 1, 1, 0, 3),
            intArrayOf(3, 3, 3, 0, 1, 1, 1, 1, 0),
            //my can't win - 0%
            intArrayOf(2, 3, 3, 3, 1, 2, 3, 3, 3),
            intArrayOf(3, 2, 3, 3, 1, 3, 2, 3, 3),
            intArrayOf(3, 3, 2, 3, 1, 3, 3, 2, 3),
            intArrayOf(3, 3, 3, 2, 1, 3, 3, 3, 2),
            //my is beaten - 30%
            intArrayOf(2, 1, 1, 1, 1, 0, 3, 3, 3),
            intArrayOf(3, 2, 1, 1, 1, 1, 0, 3, 3),
            intArrayOf(3, 3, 2, 1, 1, 1, 1, 0, 3),
            intArrayOf(3, 3, 3, 2, 1, 1, 1, 1, 0),

            intArrayOf(3, 3, 3, 0, 1, 1, 1, 1, 2),
            intArrayOf(3, 3, 0, 1, 1, 1, 1, 2, 3),
            intArrayOf(3, 0, 1, 1, 1, 1, 2, 3, 3),
            intArrayOf(0, 1, 1, 1, 1, 2, 3, 3, 3),

            intArrayOf(1, 1, 1, 0, 1, 0, 0, 3, 3),
            intArrayOf(3, 3, 0, 0, 1, 0, 1, 1, 1),
            //my is beaten - 15%
            intArrayOf(3, 2, 1, 1, 1, 0, 0, 3, 3),
            intArrayOf(2, 1, 1, 0, 1, 0, 3, 3, 3),
            intArrayOf(2, 1, 0, 1, 1, 0, 3, 3, 3),
            intArrayOf(1, 1, 0, 0, 1, 3, 3, 3, 3),
            intArrayOf(1, 0, 1, 0, 1, 3, 3, 3, 3),
            intArrayOf(1, 0, 0, 1, 1, 3, 3, 3, 3),

            intArrayOf(3, 3, 0, 0, 1, 1, 1, 2, 3),
            intArrayOf(3, 3, 3, 0, 1, 0, 1, 1, 2),
            intArrayOf(3, 3, 3, 0, 1, 1, 0, 1, 2),
            intArrayOf(3, 3, 3, 3, 1, 0, 0, 1, 1),
            intArrayOf(3, 3, 3, 3, 1, 0, 1, 0, 1),
            intArrayOf(3, 3, 3, 3, 1, 1, 0, 0, 1)
        )
    }
}