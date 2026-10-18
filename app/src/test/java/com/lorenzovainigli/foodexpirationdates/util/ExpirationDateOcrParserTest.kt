package com.lorenzovainigli.foodexpirationdates.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class ExpirationDateOcrParserTest {

    @Test
    fun parse_standardDmySlash_parsesCorrectly() {
        val input = "Product Milk EXP 25/12/2026"
        val result = ExpirationDateOcrParser.parse(input)

        assertNotNull(result)
        assertEquals("2026-12-25", result?.formattedDate)
        assertEquals("EXP", result?.detectedPrefix)
        assertTrue((result?.confidence ?: 0f) >= 0.90f)
    }

    @Test
    fun parse_dottedShortYear_parsesCorrectly() {
        val input = "BEST BEFORE 15.08.26"
        val result = ExpirationDateOcrParser.parse(input)

        assertNotNull(result)
        assertEquals("2026-08-15", result?.formattedDate)
        assertEquals("BEST BEFORE", result?.detectedPrefix)
    }

    @Test
    fun parse_isoFormat_parsesCorrectly() {
        val input = "Batch #98124 USE BY 2027-05-10"
        val result = ExpirationDateOcrParser.parse(input)

        assertNotNull(result)
        assertEquals("2027-05-10", result?.formattedDate)
        assertEquals("USE BY", result?.detectedPrefix)
    }

    @Test
    fun parse_indonesianMonthName_parsesCorrectly() {
        val input = "BAIK DIGUNAKAN SEBELUM: 12 Okt 2026"
        val result = ExpirationDateOcrParser.parse(input)

        assertNotNull(result)
        assertEquals("2026-10-12", result?.formattedDate)
        assertEquals("BAIK DIGUNAKAN SEBELUM", result?.detectedPrefix)
    }

    @Test
    fun parse_indonesianFullNameMonth_parsesCorrectly() {
        val input = "TGL KADALUARSA 28 DESEMBER 2025"
        val result = ExpirationDateOcrParser.parse(input)

        assertNotNull(result)
        assertEquals("2025-12-28", result?.formattedDate)
        assertEquals("TGL KADALUARSA", result?.detectedPrefix)
    }

    @Test
    fun parse_multiLinePackaging_detectsPrefixFromPrecedingLine() {
        val input = """
            SARI ROTI TAWAR
            PROD: 10/10/2026
            EXP:
            18/10/2026
            L249821
        """.trimIndent()

        val result = ExpirationDateOcrParser.parse(input)
        assertNotNull(result)
        assertEquals("2026-10-18", result?.formattedDate)
        assertEquals("EXP", result?.detectedPrefix)
    }

    @Test
    fun parse_ocrTyposFixed_parsesSuccessfully() {
        // 'O' instead of '0', 'S' instead of '5'
        val input = "EXP 2S/12/2O26"
        val result = ExpirationDateOcrParser.parse(input)

        assertNotNull(result)
        assertEquals("2026-12-25", result?.formattedDate)
    }

    @Test
    fun parse_monthYearFormat_parsesToEndOfMonth() {
        val input = "BB 02/2028"
        val result = ExpirationDateOcrParser.parse(input)

        assertNotNull(result)
        // 2028 is a leap year, so February has 29 days
        assertEquals("2028-02-29", result?.formattedDate)
    }

    @Test
    fun parse_noDateInText_returnsNull() {
        val input = "Indomie Mi Instan Rasa Ayam Bawang Netto 75g PT Indofood CBP Sukses Makmur"
        val result = ExpirationDateOcrParser.parse(input)

        assertNull(result)
    }

    @Test
    fun parse_invalidDateValues_ignored() {
        val input = "Serial 32/13/2026"
        val result = ExpirationDateOcrParser.parse(input)

        assertNull(result)
    }

    @Test
    fun parse_epochMillis_matchesCalculatedCalendar() {
        val input = "EXP 01/01/2027"
        val result = ExpirationDateOcrParser.parse(input)

        assertNotNull(result)
        val cal = Calendar.getInstance().apply {
            set(2027, Calendar.JANUARY, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        assertEquals(cal.timeInMillis, result?.epochMillis)
    }
}
