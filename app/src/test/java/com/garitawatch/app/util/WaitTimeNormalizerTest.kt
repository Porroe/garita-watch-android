package com.garitawatch.app.util

import com.garitawatch.app.domain.util.WaitTimeNormalizer
import org.junit.Assert.assertEquals
import org.junit.Test

class WaitTimeNormalizerTest {

    @Test
    fun `parseWaitTime should handle minutes correctly`() {
        assertEquals(30, WaitTimeNormalizer.parseWaitTime("30 min"))
        assertEquals(5, WaitTimeNormalizer.parseWaitTime("5 min"))
    }

    @Test
    fun `parseWaitTime should handle hours and minutes correctly`() {
        assertEquals(75, WaitTimeNormalizer.parseWaitTime("1 hr 15 min"))
        assertEquals(135, WaitTimeNormalizer.parseWaitTime("2 hrs 15 min"))
        assertEquals(120, WaitTimeNormalizer.parseWaitTime("2 hrs"))
    }

    @Test
    fun `parseWaitTime should handle NA correctly`() {
        assertEquals(0, WaitTimeNormalizer.parseWaitTime("N/A"))
        assertEquals(0, WaitTimeNormalizer.parseWaitTime(null))
        assertEquals(0, WaitTimeNormalizer.parseWaitTime(""))
    }

    @Test
    fun `parseWaitTime should handle raw numbers correctly`() {
        assertEquals(45, WaitTimeNormalizer.parseWaitTime("45"))
    }
    
    @Test
    fun `parseWaitTime should handle plural and singular hrs correctly`() {
        assertEquals(60, WaitTimeNormalizer.parseWaitTime("1 hr"))
        assertEquals(120, WaitTimeNormalizer.parseWaitTime("2 hrs"))
    }
}
