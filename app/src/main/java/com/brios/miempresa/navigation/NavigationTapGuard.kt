package com.brios.miempresa.navigation

class NavigationTapGuard(
    private val cooldownMillis: Long = DEFAULT_COOLDOWN_MILLIS,
    private val clock: () -> Long = { System.currentTimeMillis() },
) {
    private var lastAcceptedTapMillis: Long? = null

    fun canNavigateNow(): Boolean {
        val now = clock()
        val lastTap = lastAcceptedTapMillis
        if (lastTap != null && now - lastTap < cooldownMillis) {
            return false
        }
        lastAcceptedTapMillis = now
        return true
    }

    companion object {
        private const val DEFAULT_COOLDOWN_MILLIS = 500L
    }
}
