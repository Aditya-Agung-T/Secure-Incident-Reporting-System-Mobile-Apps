package com.adit.sirs.presentation.reports

import org.junit.Test
import org.junit.Assert.*

class ReportFormStateTest {

    @Test
    fun `empty form is not valid`() {
        val form = ReportFormState()
        assertFalse(form.isValid)
    }

    @Test
    fun `title too short shows error`() {
        val form = ReportFormState(title = "abc")
        assertNotNull(form.titleError)
    }

    @Test
    fun `valid title shows no error`() {
        val form = ReportFormState(title = "Valid title here")
        assertNull(form.titleError)
    }

    @Test
    fun `description too short shows error`() {
        val form = ReportFormState(description = "short")
        assertNotNull(form.descriptionError)
    }

    @Test
    fun `valid description shows no error`() {
        val form = ReportFormState(description = "This is a valid description that is long enough to pass the minimum requirement.")
        assertNull(form.descriptionError)
    }

    @Test
    fun `location too short shows error`() {
        val form = ReportFormState(location = "ab")
        assertNotNull(form.locationError)
    }

    @Test
    fun `missing category shows error`() {
        val form = ReportFormState(categoryId = "")
        assertNotNull(form.categoryError)
    }

    @Test
    fun `future date shows error`() {
        val form = ReportFormState(incidentDate = System.currentTimeMillis() + 86400000L)
        assertNotNull(form.dateError)
    }

    @Test
    fun `fully valid form is valid`() {
        val form = ReportFormState(
            title = "Valid title here",
            categoryId = "cat123",
            categoryName = "Test",
            description = "This is a valid description that is long enough to pass the minimum requirement.",
            location = "Building A, Floor 2",
            incidentDate = System.currentTimeMillis() - 86400000L,
            severity = "high"
        )
        assertTrue(form.isValid)
    }
}
