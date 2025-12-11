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

package by.klnvch.link5dots.domain.repositories.online

import by.klnvch.link5dots.data.online.models.CreateOnlineRoomInvitation
import by.klnvch.link5dots.data.online.models.OnlineRemoteUser
import by.klnvch.link5dots.domain.models.Point

interface FirebaseDbSetDot {
    suspend fun setDot(path: Array<String>, p: Point)
}

interface FirebaseDbSetConnected {
    suspend fun setConnected(
        path: Array<String>,
        state: Int,
        user2: OnlineRemoteUser,
        dots: List<Point>,
    )
}

interface FirebaseDbCreateInvitation {
    suspend fun createInvitation(invitation: CreateOnlineRoomInvitation)
}

interface FirebaseDbSetState {
    suspend fun setState(path: Array<String>, state: Int)
}

interface FirebaseDbAddToUserHistory {
    suspend fun addToUserHistory(path: String, value: String)
}

interface FirebaseDb :
    FirebaseDbSetDot, FirebaseDbSetConnected, FirebaseDbCreateInvitation, FirebaseDbSetState,
    FirebaseDbAddToUserHistory
