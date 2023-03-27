package by.klnvch.link5dots.utils

import android.content.Intent
import android.os.Build
import by.klnvch.link5dots.models.Room

object IntentExt {
    fun Intent.getRoom(): Room? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getSerializableExtra("room", Room::class.java)
        } else {
            getSerializableExtra("room") as Room?
        }
    }
}
