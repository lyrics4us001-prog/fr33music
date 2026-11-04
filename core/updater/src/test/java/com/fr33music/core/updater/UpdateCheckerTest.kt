package com.fr33music.core.updater

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateCheckerTest {

    @Test
    fun testIsVersionGreater_newerVersions() {
        assertTrue(UpdateChecker.isVersionGreater("v1.0.1", "1.0.0"))
        assertTrue(UpdateChecker.isVersionGreater("1.0.1", "1.0.0"))
        assertTrue(UpdateChecker.isVersionGreater("v1.1.0", "1.0.9"))
        assertTrue(UpdateChecker.isVersionGreater("v2.0.0", "1.9.9"))
        assertTrue(UpdateChecker.isVersionGreater("v1.0.0.1", "1.0.0"))
        assertTrue(UpdateChecker.isVersionGreater("v1.0.1-rc1", "1.0.0"))
        assertTrue(UpdateChecker.isVersionGreater("v1.2", "1.1.5"))
    }

    @Test
    fun testIsVersionGreater_sameOrOlderVersions() {
        assertFalse(UpdateChecker.isVersionGreater("v1.0.0", "1.0.0"))
        assertFalse(UpdateChecker.isVersionGreater("1.0.0", "1.0.0"))
        assertFalse(UpdateChecker.isVersionGreater("v1.0.0-rc1", "1.0.0"))
        assertFalse(UpdateChecker.isVersionGreater("v0.9.9", "1.0.0"))
        assertFalse(UpdateChecker.isVersionGreater("v1.0.0", "1.0.1"))
        assertFalse(UpdateChecker.isVersionGreater("1.0", "1.0.0"))
    }
}
