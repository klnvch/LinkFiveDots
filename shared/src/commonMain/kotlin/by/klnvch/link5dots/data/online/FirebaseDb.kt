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

package by.klnvch.link5dots.data.online

import by.klnvch.link5dots.data.firebase.OnlineDotRemote
import by.klnvch.link5dots.data.firebase.OnlineRemoteUser
import by.klnvch.link5dots.data.firebase.OnlineRoomInvitationRemote

interface FirebaseDbSetDot {
    suspend fun setDot(path: Array<String>, dot: OnlineDotRemote)
}

interface FirebaseDbSetConnected {
    suspend fun setConnected(path: Array<String>, state: Int, user2: OnlineRemoteUser)
}

interface FirebaseDbSetRoom {
    suspend fun setInvitation(path: Array<String>, invitation: OnlineRoomInvitationRemote)
}

interface FirebaseDbSetState {
    suspend fun setState(path: Array<String>, state: Int)
}

interface FirebaseDb :
    FirebaseDbSetDot, FirebaseDbSetConnected, FirebaseDbSetRoom, FirebaseDbSetState
