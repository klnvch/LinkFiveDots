package by.klnvch.link5dots.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import by.klnvch.link5dots.domain.usecases.SyncHistoryUseCase
import javax.inject.Inject

class SyncHistoryWorker @Inject constructor(
    appContext: Context,
    params: WorkerParameters,
    private val syncHistoryUseCase: SyncHistoryUseCase
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        syncHistoryUseCase.sync()
        return Result.success()
    }

    companion object {
        const val WORK_NAME = "SyncHistory"
    }
}
