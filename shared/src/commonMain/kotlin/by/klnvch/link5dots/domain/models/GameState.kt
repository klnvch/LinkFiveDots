package by.klnvch.link5dots.domain.models

fun List<Dot>?.getDuration(d: Int) = this
    ?.asSequence()
    ?.drop(d)
    ?.chunked(2)
    ?.filter { it.size > 1 }
    ?.filter { it[0].dt > 0 && it[1].dt > 0 }
    ?.sumOf { it[1].dt - it[0].dt }
    ?: 0

data class UserState(
    val name: String?,
    val canMove: Boolean,
    val isWon: Boolean,
    val duration: Int,
)

data class GameState(
    val room: IRoom,
    val gameActions: GameActions,
    private val userNames: ResolvedUserNames,
    private val isActive: Boolean,
) {
    val lastDotTime: Int? = room.let {
        val dt = it.dots.lastOrNull()?.dt ?: 0
        if (dt > 0) room.time + dt else null
    }
    val isOver: Boolean = room.isOver() || !isActive
    val isNew: Boolean = room.isNew()
    val lastPoint: Point? = room.lastPoint()
    val dots: List<Dot> = room.dots
    val winningLine: WinningLine? = room.getWinningLine()
    val size: Int = room.dots.size
    val user1 = UserState(
        userNames.user1Name,
        isActive && room.canMove(0),
        room.isWon(1),
        room.dots.getDuration(1),
    )
    val user2 = UserState(
        userNames.user2Name,
        isActive && room.canMove(1),
        room.isWon(0),
        room.dots.getDuration(0),
    )
}
