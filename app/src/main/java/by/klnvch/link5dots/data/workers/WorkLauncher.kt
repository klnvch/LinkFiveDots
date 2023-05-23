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
package by.klnvch.link5dots.data.workers

import android.content.Context
import by.klnvch.link5dots.data.OnlineRoomDescriptor
import by.klnvch.link5dots.data.workers.CleanUpOnlineRoomWorker.Companion.launchCleanUpOnlineRoomWorker
import by.klnvch.link5dots.domain.models.RemoteRoomDescriptor
import by.klnvch.link5dots.domain.models.RoomState
import by.klnvch.link5dots.domain.repositories.OnlineGameWorkLauncher
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkLauncher @Inject constructor(
    private val context: Context
) : OnlineGameWorkLauncher {
    override fun delete(descriptor: RemoteRoomDescriptor) {
        val key = (descriptor as OnlineRoomDescriptor).key
        context.launchCleanUpOnlineRoomWorker(key, RoomState.DELETED)
    }

    override fun finish(descriptor: RemoteRoomDescriptor) {
        val key = (descriptor as OnlineRoomDescriptor).key
        context.launchCleanUpOnlineRoomWorker(key, RoomState.FINISHED)
    }
}
