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
package by.klnvch.link5dots.domain.usecases.network

import by.klnvch.link5dots.domain.models.RemoteRoomDescriptor
import by.klnvch.link5dots.domain.repositories.NsdRoomRepository
import by.klnvch.link5dots.domain.repositories.OnlineGameWorkLauncher
import javax.inject.Inject

interface DeleteMultiplayerRoomUseCase {
    fun delete(descriptor: RemoteRoomDescriptor)
    fun finish(descriptor: RemoteRoomDescriptor)
}

class DeleteOnlineRoomUseCase @Inject constructor(
    private val workLauncher: OnlineGameWorkLauncher,
) : DeleteMultiplayerRoomUseCase {
    override fun delete(descriptor: RemoteRoomDescriptor) = workLauncher.delete(descriptor)
    override fun finish(descriptor: RemoteRoomDescriptor) = workLauncher.finish(descriptor)
}

class DeleteNsdRoomUseCase @Inject constructor(
    private val repository: NsdRoomRepository,
) : DeleteMultiplayerRoomUseCase {
    override fun delete(descriptor: RemoteRoomDescriptor) = repository.delete()
    override fun finish(descriptor: RemoteRoomDescriptor) = repository.finish()
}
