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

package by.klnvch.link5dots.domain.repositories

import by.klnvch.link5dots.domain.models.ActionAvailability
import by.klnvch.link5dots.domain.models.GameActions
import by.klnvch.link5dots.domain.models.IRoom
import by.klnvch.link5dots.domain.models.canUndo
import by.klnvch.link5dots.domain.models.isNotEmpty
import by.klnvch.link5dots.domain.models.isOwner
import javax.inject.Inject

class GameActionsInfoFactory @Inject constructor() : GameActionsFactory {
    override fun get(room: IRoom) = GameActions(
        ActionAvailability.Gone,
        ActionAvailability.Gone,
        ActionAvailability.Gone,
    )
}

class GameActionsTwoFactory @Inject constructor() : GameActionsFactory {
    override fun get(room: IRoom) = GameActions(
        when {
            room.isNotEmpty() -> ActionAvailability.Available
            else -> ActionAvailability.Disabled
        },
        ActionAvailability.Available,
        ActionAvailability.Gone,
    )
}

class GameActionsSocketFactory @Inject constructor(
    private val networkUserProvider: NetworkUserProvider,
) : GameActionsFactory {
    override fun get(room: IRoom): GameActions {
        val user = networkUserProvider.networkUser
        return GameActions(
            when {
                user == null -> ActionAvailability.Gone
                room.isNotEmpty() && room.canUndo(user) -> ActionAvailability.Available
                else -> ActionAvailability.Disabled
            },
            when {
                user == null -> ActionAvailability.Gone
                room.isOwner(user) -> ActionAvailability.Available
                else -> ActionAvailability.Gone
            },
            ActionAvailability.Gone,
        )
    }
}
