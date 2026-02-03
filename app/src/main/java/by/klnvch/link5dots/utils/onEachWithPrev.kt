package by.klnvch.link5dots.utils

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.runningFold

fun <T : Any> Flow<T>.onEachWithPrev(
    action: suspend (prev: T?, next: T) -> Unit,
): Flow<T> = runningFold<T, Pair<T?, T>?>(null) { acc, new -> Pair(acc?.second, new) }
    .filterNotNull()
    .onEach { action(it.first, it.second) }
    .map { it.second }
