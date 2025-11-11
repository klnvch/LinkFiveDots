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

import by.klnvch.link5dots.domain.models.BotGameScore
import by.klnvch.link5dots.domain.models.GameResult
import by.klnvch.link5dots.domain.models.UnauthorizedException
import by.klnvch.link5dots.domain.repositories.DeviceInfo
import by.klnvch.link5dots.domain.repositories.FirebaseManager
import by.klnvch.link5dots.domain.repositories.GameScoreRepository
import by.klnvch.link5dots.domain.repositories.RoomGetRepository
import by.klnvch.link5dots.domain.repositories.Settings
import by.klnvch.link5dots.domain.repositories.StringRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

interface SaveScoreUseCase {
    suspend fun save()
}

class SaveScoreEmptyUseCase @Inject constructor() : SaveScoreUseCase {
    override suspend fun save() = Unit
}

// TODO: add isSupported and login
class SaveScoreBotUseCase @Inject constructor(
    private val deviceInfo: DeviceInfo,
    private val firebaseManager: FirebaseManager,
    private val gameScoreRepository: GameScoreRepository,
    private val settings: Settings,
    private val stringRepository: StringRepository,
    private val getRepository: RoomGetRepository,
) : SaveScoreUseCase {
    override suspend fun save() {
        getRepository.room?.let {
            if (it.isOver()) {
                val score = BotGameScore(
                    it.dots.size,
                    it.getDuration(),
                    it.getEndTime().toLong(),
                    if (it.dots.size % 2 == 1) GameResult.WON else GameResult.LOST
                )
                val deviceId = deviceInfo.getAndroidId()
                val userId = firebaseManager.userId.first() ?: throw UnauthorizedException()
                val userName = settings.getUserName() ?: stringRepository.unknownName
                gameScoreRepository.save(score, userName, userId, deviceId)
            }
        }
    }
}
