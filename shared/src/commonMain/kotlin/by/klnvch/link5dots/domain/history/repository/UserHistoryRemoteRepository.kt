package by.klnvch.link5dots.domain.history.repository

interface UserHistorySaveRemoteRepository {
    suspend fun addItemToUserHistory(userId: String, roomId: String, value: String)
}

interface UserHistoryReadRemoteRepository {
    suspend fun getUserHistory(userId: String): List<String>
}

interface UserHistoryRemoteRepository :
    UserHistorySaveRemoteRepository,
    UserHistoryReadRemoteRepository
