package com.brios.miempresa.core.sync

import com.brios.miempresa.categories.data.shouldRewriteCategoriesPrivateSheet
import com.brios.miempresa.products.data.shouldRewriteProductsPrivateSheet
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PrivateSheetRewriteGuardTest {
    @Test
    fun `products guard skips destructive rewrite when local snapshot and dirty set are empty`() {
        assertFalse(
            shouldRewriteProductsPrivateSheet(
                hasActiveRows = false,
                hasDirtyRows = false,
            ),
        )
    }

    @Test
    fun `products guard allows rewrite for legitimate delete all when dirty tombstones exist`() {
        assertTrue(
            shouldRewriteProductsPrivateSheet(
                hasActiveRows = false,
                hasDirtyRows = true,
            ),
        )
    }

    @Test
    fun `categories guard skips destructive rewrite when local snapshot and dirty set are empty`() {
        assertFalse(
            shouldRewriteCategoriesPrivateSheet(
                hasActiveRows = false,
                hasDirtyRows = false,
            ),
        )
    }

    @Test
    fun `categories guard allows rewrite for legitimate delete all when dirty tombstones exist`() {
        assertTrue(
            shouldRewriteCategoriesPrivateSheet(
                hasActiveRows = false,
                hasDirtyRows = true,
            ),
        )
    }
}
