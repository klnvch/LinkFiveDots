/*
 * MIT License
 *
 * Copyright (c) 2023-2025 klnvch
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

import android.util.Log
import by.klnvch.link5dots.domain.models.IRoom
import by.klnvch.link5dots.domain.models.RoomFactory
import by.klnvch.link5dots.domain.models.canMove
import by.klnvch.link5dots.domain.models.isNew
import by.klnvch.link5dots.domain.repositories.KeyForInfoRepository
import by.klnvch.link5dots.domain.repositories.NetworkUserProvider
import by.klnvch.link5dots.domain.repositories.RoomFlowLocalRepository
import by.klnvch.link5dots.domain.repositories.RoomFlowRemoteRepository
import by.klnvch.link5dots.domain.repositories.RoomRepository
import by.klnvch.link5dots.domain.repositories.RoomSaveLocalRepository
import by.klnvch.link5dots.domain.repositories.Settings
import by.klnvch.link5dots.domain.repositories.VibratorService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import javax.inject.Inject

interface GetRoomUseCase {
    val room: Flow<IRoom>
}

class GetRoomCommonUseCase @Inject constructor(
    getRepository: RoomFlowLocalRepository,
    scope: CoroutineScope,
    private val saveRepository: RoomSaveLocalRepository,
    private val roomFactory: RoomFactory,
) : GetRoomUseCase {
    override val room: Flow<IRoom> = getRepository.roomFlow
        .distinctUntilChanged()
        .onEach {
            Log.d("GetRoomCommonUseCase", "once per updated: $it")
            if (it == null) saveRepository.save(roomFactory.generate())
        }
        .filterNotNull()
        .shareIn(scope, SharingStarted.WhileSubscribed(), 1)
}

class GetRoomInfoUseCase @Inject constructor(
    private val repository: RoomRepository,
    keyForInfoRepository: KeyForInfoRepository,
) : GetRoomUseCase {
    @OptIn(ExperimentalCoroutinesApi::class)
    override val room: Flow<IRoom> = keyForInfoRepository.key.flatMapLatest {
        repository.getByKey(it).filterNotNull()
    }
}

class GetRoomNetworkUseCase @Inject constructor(
    repository: RoomFlowRemoteRepository,
    private val saveRepository: RoomSaveLocalRepository,
    private val settings: Settings,
    private val vibratorService: VibratorService,
    private val networkUserProvider: NetworkUserProvider,
) : GetRoomUseCase {
    override val room: Flow<IRoom> = repository.roomFlow
        .onEach {
            val isEnabled = settings.isVibrationEnabled.first()
            val canMove = it.canMove(networkUserProvider.networkUser)
            val isNew = it.isNew()
            if (isEnabled && (canMove || isNew)) vibratorService.vibrate()
        }
        .onEach { saveRepository.save(it) }
}
