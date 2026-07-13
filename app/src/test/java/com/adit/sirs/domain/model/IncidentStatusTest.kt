package com.adit.sirs.domain.model

import org.junit.Test
import org.junit.Assert.*

class IncidentStatusTest {

    @Test
    fun `fromString returns correct status`() {
        assertEquals(IncidentStatus.PENDING, IncidentStatus.fromString("pending"))
        assertEquals(IncidentStatus.INVESTIGATING, IncidentStatus.fromString("investigating"))
        assertEquals(IncidentStatus.RESOLVED, IncidentStatus.fromString("resolved"))
        assertEquals(IncidentStatus.REJECTED, IncidentStatus.fromString("rejected"))
    }

    @Test
    fun `fromString returns PENDING for unknown value`() {
        assertEquals(IncidentStatus.PENDING, IncidentStatus.fromString("unknown"))
    }
}
