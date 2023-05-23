/*
 * MIT License
 *
 * Copyright (c) 2023 klnvch
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
package by.klnvch.link5dots.domain.repositories

import by.klnvch.link5dots.domain.models.Dot
import by.klnvch.link5dots.domain.models.NetworkRoom
import by.klnvch.link5dots.domain.models.NetworkUser
import by.klnvch.link5dots.domain.models.RemoteRoomDescriptor
import kotlinx.coroutines.flow.Flow

interface OnlineRoomRepository {
    suspend fun generateKey(): String
    suspend fun create(room: NetworkRoom): RemoteRoomDescriptor
    suspend fun updateState(key: String, state: Int)
    fun getState(key: String): Flow<Int>
    suspend fun isConnected(): Boolean
    suspend fun connect(descriptor: RemoteRoomDescriptor, user2: NetworkUser)
    fun get(descriptor: RemoteRoomDescriptor): Flow<NetworkRoom>
    suspend fun addDot(key: String, position: Int, dot: Dot)
    val path: String
    fun getRemoteRooms(): Flow<List<RemoteRoomDescriptor>>
}
