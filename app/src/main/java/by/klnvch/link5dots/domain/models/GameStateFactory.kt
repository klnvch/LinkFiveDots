package by.klnvch.link5dots.domain.models

import by.klnvch.link5dots.domain.repositories.AnyUserNameResolver
import by.klnvch.link5dots.domain.repositories.GameActionsFactory
import by.klnvch.link5dots.domain.repositories.NetworkUserNameResolver
import javax.inject.Inject

class OfflineGameStateFactory @Inject constructor(
    userNameResolver: AnyUserNameResolver,
    gameActionsFactory: GameActionsFactory,
) : GameStateFactory<IRoom>(userNameResolver, gameActionsFactory)

class NetworkGameStateFactory @Inject constructor(
    userNameResolver: NetworkUserNameResolver,
    gameActionsFactory: GameActionsFactory,
) : GameStateFactory<INetworkRoom>(userNameResolver, gameActionsFactory)
