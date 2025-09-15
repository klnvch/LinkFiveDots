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
import by.klnvch.link5dots.domain.models.NetworkRoom
import by.klnvch.link5dots.domain.models.isNotEmpty
import by.klnvch.link5dots.domain.repositories.BluetoothRoomRepository
import by.klnvch.link5dots.domain.repositories.NsdRoomRepository
import by.klnvch.link5dots.domain.repositories.RoomGetRepository
import by.klnvch.link5dots.domain.repositories.Settings
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GameActionsInfoUseCase @Inject constructor() : GameActionsUseCase {
    override suspend fun getUndoAvailability() = ActionAvailability.Gone
    override suspend fun getNewAvailability() = ActionAvailability.Gone
    override suspend fun getShareAvailability() = ActionAvailability.Gone
}

class GameActionsTwoUseCase @Inject constructor(
    private val getRepository: RoomGetRepository,
) : GameActionsUseCase {
    override suspend fun getUndoAvailability(): ActionAvailability {
        val room = getRepository.room
        return when {
            room == null -> ActionAvailability.Gone
            room.isNotEmpty() -> ActionAvailability.Available
            else -> ActionAvailability.Disabled
        }
    }

    override suspend fun getNewAvailability() = ActionAvailability.Available
    override suspend fun getShareAvailability() = ActionAvailability.Gone
}

class GameActionsBluetoothUseCase @Inject constructor(
    private val repository: BluetoothRoomRepository,
    private val settings: Settings,
) : GameActionsUseCase {
    override suspend fun getUndoAvailability(): ActionAvailability {
        val currentRoom = repository.getFlow().filterNotNull().first()
        return if (isAvailable(currentRoom)) ActionAvailability.Available else ActionAvailability.Disabled
    }

    override suspend fun getNewAvailability() =
        if (repository.isServer()) ActionAvailability.Available else ActionAvailability.Gone

    override suspend fun getShareAvailability() = ActionAvailability.Gone

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

class GameActionsNsdUseCase @Inject constructor(
    private val repository: NsdRoomRepository,
    private val settings: Settings,
) : GameActionsUseCase {
    override suspend fun getUndoAvailability(): ActionAvailability {
        val currentRoom = repository.getFlow().filterNotNull().first()
        return if (isAvailable(currentRoom)) ActionAvailability.Available else ActionAvailability.Disabled
    }

    override suspend fun getNewAvailability() =
        if (repository.isServer()) ActionAvailability.Available else ActionAvailability.Gone

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

    override suspend fun getShareAvailability() = ActionAvailability.Gone
}
