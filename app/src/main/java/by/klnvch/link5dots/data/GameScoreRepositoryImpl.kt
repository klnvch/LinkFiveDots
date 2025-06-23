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

package by.klnvch.link5dots.data

import by.klnvch.link5dots.BuildConfig
import by.klnvch.link5dots.data.firebase.GameScoreRemote
import by.klnvch.link5dots.domain.models.BotGameScore
import by.klnvch.link5dots.domain.repositories.GameScoreRepository
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class GameScoreRepositoryImpl @Inject constructor() : GameScoreRepository {
    private val path = if (BuildConfig.DEBUG) "high_scores_debug" else "high_scores"
    private val ref = Firebase.database.reference.child(path)

    override suspend fun save(
        score: BotGameScore,
        userName: String,
        userId: String,
        androidId: String,
    ) {
        val scoreRemote = GameScoreRemote(score, userName, userId, androidId)
        val ref = Firebase.database.reference.child(path)
        val key = ref.push().key

        if (key != null) {
            ref.child(key).setValue(scoreRemote).await()
        } else {
            FirebaseCrashlytics.getInstance()
                .recordException(NullPointerException("Couldn't get push key for scores"))
        }
    }

    override suspend fun getHighScore(): List<GameScoreRemote> {
        val dataSnapshot = ref
            .orderByChild("score")
            .limitToLast(500)
            .get()
            .await()

        return dataSnapshot.children
            .mapNotNull { it.getValue(GameScoreRemote::class.java) }
    }
}
