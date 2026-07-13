package com.adit.sirs.core.security

import org.junit.Test
import org.junit.Assert.*

class MimeTypeValidatorTest {

    @Test
    fun `JPEG is allowed`() {
        assertTrue(MimeTypeValidator.isAllowed("image/jpeg"))
    }

    @Test
    fun `PNG is allowed`() {
        assertTrue(MimeTypeValidator.isAllowed("image/png"))
    }

    @Test
    fun `PDF is allowed`() {
        assertTrue(MimeTypeValidator.isAllowed("application/pdf"))
    }

    @Test
    fun `video is not allowed`() {
        assertFalse(MimeTypeValidator.isAllowed("video/mp4"))
    }

    @Test
    fun `null is not allowed`() {
        assertFalse(MimeTypeValidator.isAllowed(null))
    }

    @Test
    fun `getExtensionFromMime works`() {
        assertEquals("jpg", MimeTypeValidator.getExtensionFromMime("image/jpeg"))
        assertEquals("png", MimeTypeValidator.getExtensionFromMime("image/png"))
        assertEquals("pdf", MimeTypeValidator.getExtensionFromMime("application/pdf"))
        assertNull(MimeTypeValidator.getExtensionFromMime("video/mp4"))
    }
}
