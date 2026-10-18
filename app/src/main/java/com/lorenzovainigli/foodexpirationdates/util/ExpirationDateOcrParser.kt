package com.lorenzovainigli.foodexpirationdates.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.regex.Pattern

/**
 * Intelligent parser to extract food expiration dates from raw OCR text output.
 * Handles messy camera scans, OCR character misrecognitions (e.g. 'O' for '0'),
 * multiple international & Indonesian date formats, and expiration label prefixes.
 */
object ExpirationDateOcrParser {

    data class OcrDateResult(
        val originalText: String,
        val epochMillis: Long,
        val formattedDate: String,
        val confidence: Float,
        val detectedPrefix: String? = null
    )

    private val EXPIRATION_PREFIXES = listOf(
        // English
        "BEST BEFORE", "BEST BY", "USE BY", "USE BEFORE", "EXPIRY DATE", "EXP DATE", "EXP", "BB",
        // Indonesian / Malay
        "BAIK DIGUNAKAN SEBELUM", "BAIK SEBELUM", "TGL KADALUARSA", "TGL KEDALUWARSA",
        "KADALUARSA", "KEDALUWARSA", "EXPIRED", "EXP."
    )

    private val MONTH_MAP = mapOf(
        // Indonesian
        "jan" to 0, "januari" to 0,
        "feb" to 1, "februari" to 1,
        "mar" to 2, "maret" to 2,
        "apr" to 3, "april" to 3,
        "mei" to 4,
        "jun" to 5, "juni" to 5,
        "jul" to 6, "juli" to 6,
        "agu" to 7, "agustus" to 7, "agt" to 7,
        "sep" to 8, "september" to 8,
        "okt" to 9, "oktober" to 9,
        "nov" to 10, "november" to 10,
        "des" to 11, "desember" to 11,
        // English
        "january" to 0,
        "february" to 1,
        "march" to 2,
        "may" to 4,
        "june" to 5,
        "july" to 6,
        "aug" to 7, "august" to 7,
        "oct" to 9, "october" to 9,
        "dec" to 11, "december" to 11
    )

    // Regex patterns for various date representations
    // 1. Day Month Year with text month: "12 Dec 2026", "25-Mei-2025", "15 Oktober 26"
    private val TEXT_MONTH_PATTERN = Pattern.compile(
        """\b(\d{1,2})[\s\.\-\/]+([a-zA-Z]{3,9})[\s\.\-\/]+(\d{2,4})\b""",
        Pattern.CASE_INSENSITIVE
    )

    // 2. Standard numeric dates: "25/12/2026", "25.12.26", "25-12-2026"
    private val NUMERIC_DMY_PATTERN = Pattern.compile(
        """\b(\d{1,2})[\/\.\-](\d{1,2})[\/\.\-](\d{2,4})\b"""
    )

    // 3. ISO / Reverse numeric format: "2026-12-25", "2026/12/25"
    private val NUMERIC_YMD_PATTERN = Pattern.compile(
        """\b(20\d{2})[\/\.\-](\d{1,2})[\/\.\-](\d{1,2})\b"""
    )

    // 4. Month / Year format: "12/2026", "08/26"
    private val NUMERIC_MY_PATTERN = Pattern.compile(
        """\b(\d{1,2})[\/\.\-](20\d{2}|\d{2})\b"""
    )

    /**
     * Parse raw text string and return the best expiration date candidate if found.
     */
    fun parse(rawText: String): OcrDateResult? {
        val candidates = parseAll(rawText)
        return candidates.maxByOrNull { it.confidence }
    }

    /**
     * Parse all potential date candidates found in the text, ranked by confidence.
     */
    fun parseAll(rawText: String): List<OcrDateResult> {
        val results = mutableListOf<OcrDateResult>()
        val lines = rawText.lines()

        for (lineIndex in lines.indices) {
            val line = lines[lineIndex].trim()
            if (line.isBlank()) continue

            // Check if there is an explicit prefix in this line or previous line
            var matchedPrefix: String? = null
            for (prefix in EXPIRATION_PREFIXES) {
                if (line.contains(prefix, ignoreCase = true)) {
                    matchedPrefix = prefix
                    break
                }
            }

            // Also check preceding line for prefix if current line only contains date
            if (matchedPrefix == null && lineIndex > 0) {
                val prevLine = lines[lineIndex - 1].trim()
                for (prefix in EXPIRATION_PREFIXES) {
                    if (prevLine.contains(prefix, ignoreCase = true)) {
                        matchedPrefix = prefix
                        break
                    }
                }
            }

            // Sanitize common OCR typos for date parsing
            val sanitized = sanitizeOcrText(line)

            // Try Matcher 1: Day Month Year with text month (e.g. 15 Dec 2026)
            val textMonthMatcher = TEXT_MONTH_PATTERN.matcher(sanitized)
            while (textMonthMatcher.find()) {
                val dayStr = textMonthMatcher.group(1) ?: continue
                val monthStr = textMonthMatcher.group(2)?.lowercase(Locale.ROOT) ?: continue
                val yearStr = textMonthMatcher.group(3) ?: continue

                val monthVal = MONTH_MAP[monthStr]
                if (monthVal != null) {
                    val day = dayStr.toIntOrNull() ?: continue
                    val year = normalizeYear(yearStr.toIntOrNull() ?: continue)
                    if (isValidDate(day, monthVal, year)) {
                        val cal = Calendar.getInstance().apply {
                            set(Calendar.YEAR, year)
                            set(Calendar.MONTH, monthVal)
                            set(Calendar.DAY_OF_MONTH, day)
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        val conf = if (matchedPrefix != null) 0.95f else 0.85f
                        results.add(
                            OcrDateResult(
                                originalText = textMonthMatcher.group(0) ?: line,
                                epochMillis = cal.timeInMillis,
                                formattedDate = formatDate(cal),
                                confidence = conf,
                                detectedPrefix = matchedPrefix
                            )
                        )
                    }
                }
            }

            // Try Matcher 2: Numeric DMY (e.g. 25/12/2026 or 25.12.26)
            val dmyMatcher = NUMERIC_DMY_PATTERN.matcher(sanitized)
            while (dmyMatcher.find()) {
                val first = dmyMatcher.group(1)?.toIntOrNull() ?: continue
                val second = dmyMatcher.group(2)?.toIntOrNull() ?: continue
                val year = normalizeYear(dmyMatcher.group(3)?.toIntOrNull() ?: continue)

                // Interpret day and month: standard dd/MM/yyyy
                var day = first
                var month = second - 1 // 0-indexed month

                if (second > 12 && first in 1..12) {
                    // MM/dd/yyyy format detected
                    day = second
                    month = first - 1
                }

                if (isValidDate(day, month, year)) {
                    val cal = Calendar.getInstance().apply {
                        set(Calendar.YEAR, year)
                        set(Calendar.MONTH, month)
                        set(Calendar.DAY_OF_MONTH, day)
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    val conf = if (matchedPrefix != null) 0.90f else 0.75f
                    results.add(
                        OcrDateResult(
                            originalText = dmyMatcher.group(0) ?: line,
                            epochMillis = cal.timeInMillis,
                            formattedDate = formatDate(cal),
                            confidence = conf,
                            detectedPrefix = matchedPrefix
                        )
                    )
                }
            }

            // Try Matcher 3: Numeric YMD (e.g. 2026-12-25)
            val ymdMatcher = NUMERIC_YMD_PATTERN.matcher(sanitized)
            while (ymdMatcher.find()) {
                val year = ymdMatcher.group(1)?.toIntOrNull() ?: continue
                val month = (ymdMatcher.group(2)?.toIntOrNull() ?: continue) - 1
                val day = ymdMatcher.group(3)?.toIntOrNull() ?: continue

                if (isValidDate(day, month, year)) {
                    val cal = Calendar.getInstance().apply {
                        set(Calendar.YEAR, year)
                        set(Calendar.MONTH, month)
                        set(Calendar.DAY_OF_MONTH, day)
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    val conf = if (matchedPrefix != null) 0.92f else 0.80f
                    results.add(
                        OcrDateResult(
                            originalText = ymdMatcher.group(0) ?: line,
                            epochMillis = cal.timeInMillis,
                            formattedDate = formatDate(cal),
                            confidence = conf,
                            detectedPrefix = matchedPrefix
                        )
                    )
                }
            }

            // Try Matcher 4: Month/Year (e.g. 12/2026)
            if (results.isEmpty() || matchedPrefix != null) {
                val myMatcher = NUMERIC_MY_PATTERN.matcher(sanitized)
                while (myMatcher.find()) {
                    val month = (myMatcher.group(1)?.toIntOrNull() ?: continue) - 1
                    val year = normalizeYear(myMatcher.group(2)?.toIntOrNull() ?: continue)

                    if (month in 0..11 && year in 2020..2050) {
                        val cal = Calendar.getInstance().apply {
                            set(Calendar.YEAR, year)
                            set(Calendar.MONTH, month)
                            // Default to end of month for expiry dates that specify only month/year
                            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        val conf = if (matchedPrefix != null) 0.80f else 0.50f
                        results.add(
                            OcrDateResult(
                                originalText = myMatcher.group(0) ?: line,
                                epochMillis = cal.timeInMillis,
                                formattedDate = formatDate(cal),
                                confidence = conf,
                                detectedPrefix = matchedPrefix
                            )
                        )
                    }
                }
            }
        }

        // De-duplicate results with same epochMillis, keeping the highest confidence
        return results
            .groupBy { it.epochMillis }
            .map { (_, group) -> group.maxByOrNull { it.confidence }!! }
            .sortedByDescending { it.confidence }
    }

    private fun normalizeYear(year: Int): Int {
        return when {
            year < 100 -> 2000 + year
            else -> year
        }
    }

    private fun isValidDate(day: Int, month: Int, year: Int): Boolean {
        if (month !in 0..11) return false
        if (year !in 2020..2050) return false
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        return day in 1..maxDays
    }

    private fun formatDate(cal: Calendar): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT)
        return sdf.format(cal.time)
    }

    /**
     * Fix common OCR letter-to-digit substitutions within numeric tokens.
     */
    private fun sanitizeOcrText(text: String): String {
        val words = text.split(" ")
        val sanitizedWords = words.map { word ->
            // If the word looks like a date fragment with mixed characters (e.g. "2S/12/2O26" or "O5-11-2025")
            if (word.any { it.isDigit() } && (word.contains('/') || word.contains('.') || word.contains('-'))) {
                word.replace('O', '0')
                    .replace('o', '0')
                    .replace('l', '1')
                    .replace('I', '1')
                    .replace('S', '5')
                    .replace('s', '5')
                    .replace('B', '8')
            } else {
                word
            }
        }
        return sanitizedWords.joinToString(" ")
    }
}
