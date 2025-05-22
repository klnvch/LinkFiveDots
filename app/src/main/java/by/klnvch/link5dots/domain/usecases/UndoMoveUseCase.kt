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
import by.klnvch.link5dots.domain.models.Room
import by.klnvch.link5dots.domain.repositories.BluetoothRoomRepository
import by.klnvch.link5dots.domain.repositories.NsdRoomRepository
import by.klnvch.link5dots.domain.repositories.RoomRepository
import by.klnvch.link5dots.domain.repositories.Settings
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import javax.inject.Inject

interface UndoMoveUseCase {
    val isSupported: Boolean
    suspend fun isAvailable(room: IRoom): Boolean
    suspend fun undo(room: IRoom)
}

class UndoMoveInfoUseCase @Inject constructor() : UndoMoveUseCase {
    override val isSupported = false
    override suspend fun isAvailable(room: IRoom) = false
    override suspend fun undo(room: IRoom) = Unit
}

abstract class UndoMoveRealUseCase(
    private val roomRepository: RoomRepository,
) : UndoMoveUseCase {
    override val isSupported = true
    override suspend fun isAvailable(room: IRoom) = room.dots.isNotEmpty()
    override suspend fun undo(room: IRoom) {
        if (isAvailable(room)) {
            val updatedRoom = undoInternal(room)
            roomRepository.save(updatedRoom)
        }
    }

    protected abstract suspend fun undoInternal(room: IRoom): IRoom
}

class UndoMoveBotUseCase @Inject constructor(
    roomRepository: RoomRepository,
) : UndoMoveRealUseCase(roomRepository) {
    override suspend fun undoInternal(room: IRoom): IRoom {
        val r = Room(room)
        r.undo()
        r.undo()
        return r
    }
}

class UndoMoveTwoUseCase @Inject constructor(
    roomRepository: RoomRepository,
) : UndoMoveRealUseCase(roomRepository) {
    override suspend fun undoInternal(room: IRoom): IRoom {
        val r = Room(room)
        r.undo()
        return r
    }
}

class UndoMoveBluetoothUseCase @Inject constructor(
    private val settings: Settings,
    private val repository: BluetoothRoomRepository,
) : UndoMoveUseCase {
    override val isSupported = true
    override suspend fun isAvailable(room: IRoom): Boolean {
        val currentRoom = repository.get().filterNotNull().first()
        return isAvailable(currentRoom)
    }

    override suspend fun undo(room: IRoom) {
        val currentRoom = repository.get().filterNotNull().first()
        if (isAvailable(currentRoom)) {
            val dots = room.dots
            repository.update(currentRoom.copy(dots = dots.subList(0, dots.size - 1)))
        }
    }

    private suspend fun isAvailable(room: NetworkRoom): Boolean {
        val dots = room.dots
        if (dots.isNotEmpty()) {
            val userId = settings.getUserId().first()
            if (room.user1.id == userId) {
                if (dots.size % 2 == 1) return true
            } else {
                if (dots.size % 2 == 0) return true
            }
        }
        return false
    }
}

class UndoMoveNsdUseCase @Inject constructor(
    private val settings: Settings,
    private val repository: NsdRoomRepository,
) : UndoMoveUseCase {
    override val isSupported = true
    override suspend fun isAvailable(room: IRoom): Boolean {
        val currentRoom = repository.get().filterNotNull().first()
        return isAvailable(currentRoom)
    }

    override suspend fun undo(room: IRoom) {
        val currentRoom = repository.get().filterNotNull().first()
        if (isAvailable(currentRoom)) {
            val dots = room.dots
            repository.update(currentRoom.copy(dots = dots.subList(0, dots.size - 1)))
        }
    }

    private suspend fun isAvailable(room: NetworkRoom): Boolean {
        val dots = room.dots
        if (dots.isNotEmpty()) {
            val userId = settings.getUserId().first()
            if (room.user1.id == userId) {
                if (dots.size % 2 == 1) return true
            } else {
                if (dots.size % 2 == 0) return true
            }
        }
        return false
    }
}
