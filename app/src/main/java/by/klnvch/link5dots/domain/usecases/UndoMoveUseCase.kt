package by.klnvch.link5dots.domain.usecases

import by.klnvch.link5dots.domain.models.IRoom
import by.klnvch.link5dots.domain.models.Room
import by.klnvch.link5dots.domain.repositories.RoomRepository
import javax.inject.Inject

interface UndoMoveUseCase {
    val isSupported: Boolean
    suspend fun undo(room: IRoom)
}

class UndoMoveInfoUseCase @Inject constructor() : UndoMoveUseCase {
    override val isSupported = false
    override suspend fun undo(room: IRoom) = Unit
}

abstract class UndoMoveRealUseCase constructor(
    private val roomRepository: RoomRepository
) : UndoMoveUseCase {
    override val isSupported = true
    override suspend fun undo(room: IRoom) {
        if (room.dots.isNotEmpty()) {
            val updatedRoom = undoInternal(room)
            roomRepository.save(updatedRoom)
        }
    }

    protected abstract fun undoInternal(room: IRoom): IRoom
}

class UndoMoveBotUseCase @Inject constructor(
    roomRepository: RoomRepository
) : UndoMoveRealUseCase(roomRepository) {
    override fun undoInternal(room: IRoom): IRoom {
        val r = Room(room)
        r.undo()
        r.undo()
        return r
    }
}

class UndoMoveTwoUseCase @Inject constructor(
    roomRepository: RoomRepository
) : UndoMoveRealUseCase(roomRepository) {
    override fun undoInternal(room: IRoom): IRoom {
        val r = Room(room)
        r.undo()
        return r
    }
}
