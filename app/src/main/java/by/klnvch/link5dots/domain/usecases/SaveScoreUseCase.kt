/*
 * MIT License
 *
 * Copyright (c) 2023 klnvch
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

import by.klnvch.link5dots.data.firebase.GameScoreRemote
import by.klnvch.link5dots.domain.models.BotGameScore
import by.klnvch.link5dots.domain.repositories.DeviceInfo
import by.klnvch.link5dots.domain.repositories.FirebaseManager
import by.klnvch.link5dots.domain.repositories.GameScoreRemoteSource
import by.klnvch.link5dots.domain.repositories.Settings
import kotlinx.coroutines.flow.first
import javax.inject.Inject

// TODO: add isSupported and login
class SaveScoreUseCase @Inject constructor(
    private val deviceInfo: DeviceInfo,
    private val firebaseManager: FirebaseManager,
    private val gameScoreRemoteSource: GameScoreRemoteSource,
    private val settings: Settings,
) {
    suspend fun save(score: BotGameScore) {
        val deviceId = deviceInfo.getAndroidId()
        val userId = firebaseManager.getUserId()
        val userName = settings.getUserName().first()
        if (userId != null) {
            val scoreRemote = GameScoreRemote(score, userName, userId, deviceId)
            gameScoreRemoteSource.save(scoreRemote)
        }
    }
}
