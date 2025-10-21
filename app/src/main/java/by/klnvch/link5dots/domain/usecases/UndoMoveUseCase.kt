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
import by.klnvch.link5dots.domain.models.IUser
import by.klnvch.link5dots.domain.models.NetworkRoom
import by.klnvch.link5dots.domain.models.canUndo
import by.klnvch.link5dots.domain.repositories.NetworkUserProvider
import by.klnvch.link5dots.domain.repositories.RoomGetRepository
import by.klnvch.link5dots.domain.repositories.RoomSaveLocalRepository
import by.klnvch.link5dots.domain.repositories.SocketGetRepository
import by.klnvch.link5dots.domain.repositories.SocketSendRepository
import by.klnvch.link5dots.domain.repositories.networkUserOrThrow
import javax.inject.Inject

class UndoMoveInfoUseCase @Inject constructor() : UndoMoveUseCase {
    override suspend fun undo() = Unit
}

class UndoMoveTwoUseCase @Inject constructor(
    getRepository: RoomGetRepository,
    private val saveRepository: RoomSaveLocalRepository,
) : UndoMoveCommonUseCase<IRoom>(getRepository) {
    override suspend fun undo(room: IRoom) = room.undo()
    override suspend fun save(room: IRoom) = saveRepository.save(room)
}

class UndoMoveSocketUseCase @Inject constructor(
    getRepository: SocketGetRepository,
    private val sendRepository: SocketSendRepository,
    private val networkUserProvider: NetworkUserProvider,
) : UndoMoveCommonUseCase<NetworkRoom>(getRepository) {
    private val user: IUser get() = networkUserProvider.networkUserOrThrow
    override suspend fun undo(room: NetworkRoom) = if (room.canUndo(user)) room.undo() else null
    override suspend fun save(room: NetworkRoom) = sendRepository.send(room)
}
