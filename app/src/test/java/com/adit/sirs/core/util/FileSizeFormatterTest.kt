package com.adit.sirs.core.util

import org.junit.Test
import org.junit.Assert.*

class FileSizeFormatterTest {

    @Test
    fun `formats bytes`() {
        assertEquals("512 B", FileSizeFormatter.format(512))
    }

    @Test
    fun `formats kilobytes`() {
        assertEquals("1.0 KB", FileSizeFormatter.format(1024))
    }

    @Test
    fun `formats megabytes`() {
        assertEquals("2.0 MB", FileSizeFormatter.format(2 * 1024 * 1024))
    }
}
