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

import by.klnvch.link5dots.domain.models.BotUser
import by.klnvch.link5dots.domain.models.IRoom
import by.klnvch.link5dots.domain.models.canUndo
import by.klnvch.link5dots.domain.models.isNotEmpty
import by.klnvch.link5dots.domain.repositories.GetRoomRepository
import by.klnvch.link5dots.domain.repositories.RoomGetRepository
import by.klnvch.link5dots.domain.repositories.RoomSaveLocalRepository

interface UndoMoveUseCase {
    suspend fun undo()
}

abstract class UndoMoveCommonUseCase<Room : IRoom>(
    private val getRepository: GetRoomRepository<Room>,
) : UndoMoveUseCase {
    override suspend fun undo() {
        getRepository.room?.let { room ->
            if (room.isNotEmpty()) undo(room)?.let { save(it) }
        }
    }

    protected abstract suspend fun undo(room: Room): Room?
    protected abstract suspend fun save(room: Room)
}

class UndoMoveBotUseCase(
    getRepository: RoomGetRepository,
    private val saveRepository: RoomSaveLocalRepository,
) : UndoMoveCommonUseCase<IRoom>(getRepository) {
    override suspend fun undo(room: IRoom) =
        if (room.canUndo(BotUser)) room.undo().undo() else room.undo()

    override suspend fun save(room: IRoom) = saveRepository.save(room)
}
