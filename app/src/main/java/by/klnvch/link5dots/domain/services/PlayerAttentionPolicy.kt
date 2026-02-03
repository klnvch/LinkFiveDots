package by.klnvch.link5dots.domain.services

import by.klnvch.link5dots.domain.models.INetworkRoom
import by.klnvch.link5dots.domain.models.canMove
import by.klnvch.link5dots.domain.models.isNew
import by.klnvch.link5dots.domain.repositories.NetworkUserProvider
import javax.inject.Inject

class PlayerAttentionPolicy @Inject constructor(
    private val networkUserProvider: NetworkUserProvider,
) {
    fun isAttentionRequired(room: INetworkRoom): Boolean {
        val canMove = room.canMove(networkUserProvider.networkUser)
        val isNew = room.isNew()
        return canMove || isNew
    }
}
