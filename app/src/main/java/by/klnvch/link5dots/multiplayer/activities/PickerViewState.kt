package by.klnvch.link5dots.multiplayer.activities

import android.view.View
import by.klnvch.link5dots.multiplayer.utils.GameState

data class PickerViewState(private val state: GameState) {
    val isCreateButtonEnabled = when {
        state.connectState != GameState.ConnectState.NONE -> false
        state.targetState == GameState.TargetState.DELETED -> true
        state.targetState == GameState.TargetState.CREATED -> true
        else -> false
    }

    val isCreateButtonChecked = when (state.targetState) {
        GameState.TargetState.CREATED -> true
        GameState.TargetState.DELETING -> true
        else -> false
    }

    val progressCreateVisibility = when (state.targetState) {
        GameState.TargetState.CREATING -> View.VISIBLE
        GameState.TargetState.DELETING -> View.VISIBLE
        else -> View.INVISIBLE
    }

    val statusVisibility = when (state.targetState) {
        GameState.TargetState.CREATING -> View.INVISIBLE
        GameState.TargetState.DELETING -> View.INVISIBLE
        else -> View.VISIBLE
    }

    val isScanButtonEnabled = when {
        state.connectState != GameState.ConnectState.NONE -> false
        state.scanState == GameState.ScanState.ON -> true
        state.scanState == GameState.ScanState.OFF -> true
        else -> false
    }

    val isScanButtonChecked = when (state.scanState) {
        GameState.ScanState.ON -> true
        else -> false
    }

    val scanProgressVisibility = when (state.scanState) {
        GameState.ScanState.ON -> View.VISIBLE
        else -> View.INVISIBLE
    }
}
