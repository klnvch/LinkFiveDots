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

package by.klnvch.link5dots.di.workers

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import by.klnvch.link5dots.di.ApplicationScope
import javax.inject.Inject
import javax.inject.Provider

@ApplicationScope
class DaggerWorkerFactory @Inject constructor(
    private val workerSubcomponent: WorkerSubcomponent.Builder
) : WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker = workerSubcomponent
        .workerParameters(workerParameters)
        .build().run {
            val workerClass =
                Class.forName(workerClassName).asSubclass(ListenableWorker::class.java)
            createWorker(workerClass, workers())
                ?: createWorker(appContext, workerParameters, workerClass)
        }

    private fun createWorker(
        workerClass: Class<out ListenableWorker>,
        workers: Map<Class<out ListenableWorker>, Provider<ListenableWorker>>
    ): ListenableWorker? {
        var provider = workers[workerClass]
        if (provider == null) {
            for ((key, value) in workers) {
                if (workerClass.isAssignableFrom(key)) {
                    provider = value
                    break
                }
            }
        }
        return provider?.get()
    }

    private fun createWorker(
        context: Context,
        workerParameters: WorkerParameters,
        workerClass: Class<out ListenableWorker>
    ): ListenableWorker {
        val workerConstructor =
            workerClass.getDeclaredConstructor(Context::class.java, WorkerParameters::class.java)
        return workerConstructor.newInstance(context, workerParameters)
    }
}
