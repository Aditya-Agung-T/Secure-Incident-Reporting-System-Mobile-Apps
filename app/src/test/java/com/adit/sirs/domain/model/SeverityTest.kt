package com.adit.sirs.domain.model

import org.junit.Test
import org.junit.Assert.*

class SeverityTest {

    @Test
    fun `fromString returns correct severity`() {
        assertEquals(Severity.LOW, Severity.fromString("low"))
        assertEquals(Severity.MEDIUM, Severity.fromString("medium"))
        assertEquals(Severity.HIGH, Severity.fromString("high"))
        assertEquals(Severity.CRITICAL, Severity.fromString("critical"))
    }

    @Test
    fun `fromString returns LOW for unknown value`() {
        assertEquals(Severity.LOW, Severity.fromString("unknown"))
        assertEquals(Severity.LOW, Severity.fromString(""))
    }

    @Test
    fun `displayName is correct`() {
        assertEquals("Low", Severity.LOW.displayName)
        assertEquals("Medium", Severity.MEDIUM.displayName)
        assertEquals("High", Severity.HIGH.displayName)
        assertEquals("Critical", Severity.CRITICAL.displayName)
    }
}
