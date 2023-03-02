package by.klnvch.link5dots.utils

import android.content.Intent
import android.os.Build
import by.klnvch.link5dots.models.HighScore
import by.klnvch.link5dots.models.Room

object IntentExt {
    fun Intent.getRoom(): Room? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getSerializableExtra("room", Room::class.java)
        } else {
            getSerializableExtra("room") as Room?
        }
    }

    fun Intent.getHighScore(): HighScore? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getSerializableExtra(HighScore.TAG, HighScore::class.java)
        } else {
            getSerializableExtra(HighScore.TAG) as HighScore?
        }
    }
}
