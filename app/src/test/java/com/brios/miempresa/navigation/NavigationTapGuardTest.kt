package com.brios.miempresa.navigation

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NavigationTapGuardTest {
    @Test
    fun acceptsFirstNavigationTap() {
        var now = 1_000L
        val guard = NavigationTapGuard(cooldownMillis = 500L, clock = { now })

        assertTrue(guard.canNavigateNow())
    }

    @Test
    fun rejectsSecondTapInsideCooldownWindow() {
        var now = 1_000L
        val guard = NavigationTapGuard(cooldownMillis = 500L, clock = { now })

        assertTrue(guard.canNavigateNow())
        now = 1_300L

        assertFalse(guard.canNavigateNow())
    }

    @Test
    fun acceptsTapAfterCooldownElapsed() {
        var now = 1_000L
        val guard = NavigationTapGuard(cooldownMillis = 500L, clock = { now })

        assertTrue(guard.canNavigateNow())
        now = 1_550L

        assertTrue(guard.canNavigateNow())
    }
}
