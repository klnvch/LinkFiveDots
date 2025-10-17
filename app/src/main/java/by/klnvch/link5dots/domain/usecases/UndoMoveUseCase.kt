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

package by.klnvch.link5dots.domain.usecases

import by.klnvch.link5dots.domain.models.IRoom
import by.klnvch.link5dots.domain.models.NetworkRoom
import by.klnvch.link5dots.domain.models.RoomType
import by.klnvch.link5dots.domain.repositories.BluetoothRoomRepository
import by.klnvch.link5dots.domain.repositories.NetworkUserProvider
import by.klnvch.link5dots.domain.repositories.NsdRoomRepository
import by.klnvch.link5dots.domain.repositories.RoomGetRepository
import by.klnvch.link5dots.domain.repositories.RoomRepository
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class UndoMoveInfoUseCase @Inject constructor() : UndoMoveUseCase {
    override suspend fun undo() = Unit
}

class UndoMoveTwoUseCase @Inject constructor(
    getRepository: RoomGetRepository,
    private val saveRepository: RoomRepository,
) : UndoMoveRealUseCase(getRepository) {
    override suspend fun undoInternal(room: IRoom) = room.undo()
    override suspend fun save(room: IRoom) = saveRepository.save(room, RoomType.TWO_PLAYERS)
}

class UndoMoveBluetoothUseCase @Inject constructor(
    private val repository: BluetoothRoomRepository,
    private val networkUserProvider: NetworkUserProvider,
) : UndoMoveUseCase {
    override suspend fun undo() {
        val currentRoom = repository.getFlow().filterNotNull().first()
        if (isAvailable(currentRoom)) {
            repository.send(currentRoom.undo())
        }
    }

    private fun isAvailable(room: NetworkRoom): Boolean {
        val dots = room.dots
        if (dots.isNotEmpty()) {
            val user = networkUserProvider.networkUser
            if (room.user1 == user) {
                if (dots.size % 2 == 1) return true
            } else {
                if (dots.size % 2 == 0) return true
            }
        }
        return false
    }
}

class UndoMoveNsdUseCase @Inject constructor(
    private val repository: NsdRoomRepository,
    private val networkUserProvider: NetworkUserProvider,
) : UndoMoveUseCase {
    override suspend fun undo() {
        val currentRoom = repository.getFlow().filterNotNull().first()
        if (isAvailable(currentRoom)) {
            repository.send(currentRoom.undo())
        }
    }

    private fun isAvailable(room: NetworkRoom): Boolean {
        val dots = room.dots
        if (dots.isNotEmpty()) {
            val user = networkUserProvider.networkUser
            if (room.user1 == user) {
                if (dots.size % 2 == 1) return true
            } else {
                if (dots.size % 2 == 0) return true
            }
        }
        return false
    }
}
