package com.rentsplit.util

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object OcrScanner {
    private const val TAG = "OcrScanner"

    data class OcrResult(
        val amount: Double?,
        val title: String?
    )

    /**
     * Scans an image URI using Google ML Kit Text Recognition and extracts
     * receipt title (merchant) and total amount using custom heuristics.
     */
    suspend fun scanReceipt(context: Context, uri: Uri): OcrResult = suspendCancellableCoroutine { continuation ->
        try {
            val image = InputImage.fromFilePath(context, uri)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    try {
                        val result = parseReceiptText(visionText)
                        Log.d(TAG, "OCR parsed successfully. Title: ${result.title}, Amount: ${result.amount}")
                        continuation.resume(result)
                    } catch (ex: Exception) {
                        Log.e(TAG, "Error parsing OCR results", ex)
                        continuation.resume(OcrResult(null, null))
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "ML Kit OCR processing failed", e)
                    continuation.resumeWithException(e)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing ML Kit input image", e)
            continuation.resumeWithException(e)
        }
    }

    /**
     * Parses the vision text blocks using heuristic rules to extract:
     * 1. Store Name (Title)
     * 2. Total Amount
     */
    fun parseReceiptText(visionText: Text): OcrResult {
        val blocks = visionText.textBlocks
        if (blocks.isEmpty()) {
            return OcrResult(null, null)
        }

        // Collect all lines in order of top-to-bottom, left-to-right visual placement
        val lines = blocks.flatMap { it.lines }
            .sortedWith(compareBy<Text.Line> { it.boundingBox?.top ?: 0 }
                .thenBy { it.boundingBox?.left ?: 0 })

        val rawLinesText = lines.map { it.text.trim() }
        Log.d(TAG, "All Extracted OCR Lines:\n${rawLinesText.joinToString("\n")}")

        val title = extractTitle(rawLinesText)
        val amount = extractAmount(rawLinesText)

        return OcrResult(amount, title)
    }

    /**
     * Extracts merchant/store name from receipt text.
     * Looks at top lines and filters out address, phone, date, receipt metadata.
     */
    private fun extractTitle(lines: List<String>): String? {
        val ignoreKeywords = listOf(
            "tel", "phone", "ph:", "fax", "address", "street", " road", " st ", " ave", " blvd", " lane", " drive",
            "welcome", "receipt", "invoice", "order", "cashier", "date", "time", "http", "www", ".com", ".org",
            "terminal", "merchant", "customer", "sale", "transaction", "tax ", "gst", "vat", "store #", "store id",
            "chk ", "check #", "server:", "table:", "guest", "duplicate"
        )

        val dateRegex = Regex("""\b\d{1,4}[-/.]\d{1,2}[-/.]\d{1,4}\b""")
        val phoneRegex = Regex("""\b(\+?\d{1,3}[-.\s]?)?\(?\d{3}\)?[-.\s]?\d{3}[-.\s]?\d{4}\b""")
        val timeRegex = Regex("""\b\d{1,2}:\d{2}\s*(?i)(am|pm)?\b""")
        val numberOnlyRegex = Regex("""^[-\d\s\.,:#/*()]+$""")

        for (line in lines) {
            val lowerLine = line.lowercase(Locale.ROOT)
            
            // Skip short lines
            if (line.length < 3) continue

            // Skip lines with digits only or mostly digits
            if (numberOnlyRegex.matches(line)) continue

            // Skip dates, phone numbers, times
            if (dateRegex.containsMatchIn(line) || phoneRegex.containsMatchIn(line) || timeRegex.containsMatchIn(line)) continue

            // Skip if contains common receipt metadata / address keywords
            if (ignoreKeywords.any { lowerLine.contains(it) }) continue

            // Clean the title: remove any leading/trailing special symbols
            val cleaned = line.replace(Regex("""^[^a-zA-Z0-9]+|[^a-zA-Z0-9\s&'!.-]+$"""), "").trim()
            if (cleaned.length >= 3) {
                // Return capitalized title
                return cleaned.split(" ").joinToString(" ") { word ->
                    word.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
                }
            }
        }
        return null
    }

    /**
     * Extracts receipt total amount.
     * Looks for decimal values associated with total keywords, falling back to largest decimal value.
     */
    private fun extractAmount(lines: List<String>): Double? {
        val priceRegex = Regex("""[$€£¥]?\s*(\d{1,3}(?:[,\s]?\d{3})*[\.,]\d{2})\b""")
        
        val totalKeywords = listOf("total", "gtotal", "grand total", "total due", "amount due", "balance", "net to pay", "sum")
        val excludeKeywords = listOf("subtotal", "sub total", "tax", "vat", "discount", "change", "cash", "tendered", "card", "visa", "mc")

        val candidates = mutableListOf<Pair<Double, Int>>() // Value and confidence score

        for (i in lines.indices) {
            val line = lines[i]
            val lowerLine = line.lowercase(Locale.ROOT)

            // Find all price patterns in this line
            val matchResults = priceRegex.findAll(line)
            for (match in matchResults) {
                val priceStr = match.groupValues[1]
                    .replace(",", "") // Remove thousands separators
                    .replace(" ", "")
                val priceVal = priceStr.toDoubleOrNull() ?: continue

                if (priceVal <= 0.0 || priceVal > 100000.0) continue // Exclude extreme outliers

                var score = 1 // Default baseline score for any price

                // Check if this line contains total keywords
                val hasTotalKeyword = totalKeywords.any { lowerLine.contains(it) }
                val hasExcludeKeyword = excludeKeywords.any { lowerLine.contains(it) }

                if (hasTotalKeyword && !hasExcludeKeyword) {
                    score += 10
                    // Extra boost if it explicitly has "grand" or "due"
                    if (lowerLine.contains("grand") || lowerLine.contains("due")) {
                        score += 5
                    }
                } else if (hasExcludeKeyword) {
                    // Reduce score if it's subtotal or tax
                    score -= 5
                }

                // Check preceding line for keywords
                if (i > 0) {
                    val prevLine = lines[i - 1].lowercase(Locale.ROOT)
                    if (totalKeywords.any { prevLine.contains(it) } && !excludeKeywords.any { prevLine.contains(it) }) {
                        score += 8
                    }
                }

                candidates.add(Pair(priceVal, score))
            }
        }

        if (candidates.isNotEmpty()) {
            // Find candidate with highest score
            val bestCandidate = candidates.maxWithOrNull(compareBy<Pair<Double, Int>> { it.second }.thenBy { it.first })
            Log.d(TAG, "Amount candidates: $candidates. Selected: $bestCandidate")
            return bestCandidate?.first
        }

        // Fallback: If no keywords match, try to find any decimal values and return the largest one
        val fallbackPrices = lines.flatMap { line ->
            priceRegex.findAll(line).mapNotNull { match ->
                match.groupValues[1].replace(",", "").toDoubleOrNull()
            }
        }.filter { it in 0.01..10000.0 }

        if (fallbackPrices.isNotEmpty()) {
            val maxPrice = fallbackPrices.maxOrNull()
            Log.d(TAG, "Fallback amount selection: $maxPrice from candidates: $fallbackPrices")
            return maxPrice
        }

        return null
    }
}
