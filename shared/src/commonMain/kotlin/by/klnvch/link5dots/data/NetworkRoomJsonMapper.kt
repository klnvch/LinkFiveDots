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

package by.klnvch.link5dots.data

import by.klnvch.link5dots.domain.models.INetworkRoom
import by.klnvch.link5dots.domain.models.INetworkRoomInvitation
import by.klnvch.link5dots.domain.models.NetworkRoom
import by.klnvch.link5dots.domain.models.NetworkRoomEntity
import by.klnvch.link5dots.domain.models.NetworkRoomInvitation
import by.klnvch.link5dots.domain.models.dotModule
import by.klnvch.link5dots.domain.models.userModule
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.plus
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

val roomModule = SerializersModule {
    polymorphic(INetworkRoomInvitation::class) {
        subclass(NetworkRoomInvitation::class)
    }
    polymorphic(INetworkRoom::class) {
        subclass(NetworkRoom::class)
    }
}

val json = Json {
    ignoreUnknownKeys = true
    serializersModule = roomModule + userModule + dotModule
}

private fun INetworkRoom.toJson() = json.encodeToString(this)
private fun INetworkRoomInvitation.toJson() = json.encodeToString(this)
fun NetworkRoomEntity.toJson() = when (this) {
    is INetworkRoom -> this.toJson()
    is INetworkRoomInvitation -> this.toJson()
}

inline fun <reified T : NetworkRoomEntity> String.toRoom() = json.decodeFromString<T>(this)
