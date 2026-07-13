package com.adit.sirs.domain.model

import org.junit.Test
import org.junit.Assert.*

class UserRoleTest {

    @Test
    fun `fromString returns correct role`() {
        assertEquals(UserRole.USER, UserRole.fromString("user"))
        assertEquals(UserRole.ADMINISTRATOR, UserRole.fromString("administrator"))
    }

    @Test
    fun `fromString returns USER for unknown`() {
        assertEquals(UserRole.USER, UserRole.fromString("admin"))
        assertEquals(UserRole.USER, UserRole.fromString(""))
    }
}
