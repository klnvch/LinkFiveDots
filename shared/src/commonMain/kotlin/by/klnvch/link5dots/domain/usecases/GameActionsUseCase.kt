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

import by.klnvch.link5dots.domain.models.ActionAvailability
import by.klnvch.link5dots.domain.models.isNotEmpty
import by.klnvch.link5dots.domain.repositories.GetOnlineRoomRepository
import by.klnvch.link5dots.domain.repositories.RoomGetRepository

interface GameActionsUseCase {
    val undoAction: ActionAvailability
    val newAction: ActionAvailability
    val shareAction: ActionAvailability
}

class GameActionsBotUseCase(private val getRepository: RoomGetRepository) : GameActionsUseCase {
    override val undoAction: ActionAvailability
        get() {
            val room = getRepository.room
            return when {
                room == null -> ActionAvailability.Gone
                room.isNotEmpty() -> ActionAvailability.Available
                else -> ActionAvailability.Disabled
            }
        }

    override val newAction = ActionAvailability.Available
    override val shareAction = ActionAvailability.Available
}

class GameActionsOnlineUseCase(private val repository: GetOnlineRoomRepository) :
    GameActionsUseCase {
    override val undoAction = ActionAvailability.Gone
    override val newAction
        get() = if (repository.room?.isOver() == true) ActionAvailability.Available
        else ActionAvailability.Disabled

    override val shareAction = ActionAvailability.Gone
}
