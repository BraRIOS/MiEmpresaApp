package com.brios.miempresa.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Stable
class ScreenActionGuard internal constructor(
    private val canHandleAction: () -> Boolean,
    private val markNavigatingAway: () -> Unit,
    val isScreenInteractive: Boolean,
) {
    fun runIfActive(action: () -> Unit) {
        if (canHandleAction()) {
            action()
        }
    }

    fun runAndNavigate(action: () -> Unit) {
        if (canHandleAction()) {
            markNavigatingAway()
            action()
        }
    }

    fun beginNavigation() {
        markNavigatingAway()
    }
}

@Composable
fun rememberScreenActionGuard(
    minLifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
): ScreenActionGuard {
    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow.collectAsStateWithLifecycle()
    var isNavigatingAway by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(lifecycleState) {
        if (lifecycleState == Lifecycle.State.RESUMED) {
            isNavigatingAway = false
        }
    }

    val canHandleAction: () -> Boolean = {
        lifecycleState.isAtLeast(minLifecycleState) && !isNavigatingAway
    }

    return remember(lifecycleState, isNavigatingAway, minLifecycleState) {
        ScreenActionGuard(
            canHandleAction = canHandleAction,
            markNavigatingAway = { isNavigatingAway = true },
            isScreenInteractive = !isNavigatingAway,
        )
    }
}
