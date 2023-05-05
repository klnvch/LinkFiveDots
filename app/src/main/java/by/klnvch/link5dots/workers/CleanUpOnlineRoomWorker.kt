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
package by.klnvch.link5dots.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import by.klnvch.link5dots.domain.usecases.network.UpdateOnlineRoomStateUseCase
import javax.inject.Inject

class CleanUpOnlineRoomWorker @Inject constructor(
        appContext: Context,
        private val params: WorkerParameters,
        private val updateOnlineRoomStateUseCase: UpdateOnlineRoomStateUseCase,
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val key = params.inputData.getString(ROOM_KEY)
        val state = params.inputData.getInt(ROOM_STATE, -1)
        if (key != null && state != -1) {
            updateOnlineRoomStateUseCase.update(key, state)
        }
        return Result.success()
    }

    companion object {
        private const val ROOM_KEY = "ROOM_KEY"
        private const val ROOM_STATE = "ROOM_STATE"
        fun Context.launchCleanUpOnlineRoomWorker(key: String, state: Int) {
            WorkManager.getInstance(this)
                    .enqueue(OneTimeWorkRequestBuilder<CleanUpOnlineRoomWorker>()
                            .setInputData(workDataOf(Pair(ROOM_KEY, key), Pair(ROOM_STATE, state)))
                            .build())
        }
    }
}
