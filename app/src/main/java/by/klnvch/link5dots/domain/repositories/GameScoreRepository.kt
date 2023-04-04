package by.klnvch.link5dots.domain.repositories

import by.klnvch.link5dots.domain.models.BotGameScore

interface GameScoreRepository {
    fun getHighScorePath(): String
    fun save(score: BotGameScore, userName: String, userId: String, androidId: String)
}
