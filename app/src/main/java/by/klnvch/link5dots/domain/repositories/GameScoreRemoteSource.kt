package by.klnvch.link5dots.domain.repositories

import by.klnvch.link5dots.data.firebase.GameScoreRemote

interface GameScoreRemoteSource {
    fun getHighScorePath(): String
    fun save(score: GameScoreRemote)
}
