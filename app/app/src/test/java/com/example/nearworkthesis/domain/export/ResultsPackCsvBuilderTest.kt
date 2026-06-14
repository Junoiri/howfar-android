package com.example.nearworkthesis.domain.export

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ResultsPackCsvBuilderTest {

    @Test
    fun dailyHeader_isStable() {
        val csv = ResultsPackCsvBuilder.buildDailyResultsCsv(emptyList())
        val header = csv.lineSequence().first()
        assertEquals(
            // I pin the daily header here so future export changes cannot silently drop the added NRS column.
            "date,sampleCount,diopterHoursTotal,nrsSessionAverage,lowLightMinutes,longestSessionSeconds,riskySessionCount,gapCount,largestGapSeconds",
            header
        )
    }

    @Test
    fun sessionsHeader_isStable() {
        val csv = ResultsPackCsvBuilder.buildSessionsResultsCsv(emptyList())
        val header = csv.lineSequence().first()
        assertEquals(
            // I pin the session header here so future export changes cannot silently drop the added meanLux or NRS columns.
            "date,sessionStartIsoUtc,sessionEndIsoUtc,durationSeconds,avgDistanceCm,minDistanceCm,meanLux,diopterHoursInSession,nrs,lowLightSecondsInSession,flags_closeDistance,flags_lowLight,flags_extremeClose",
            header
        )
    }

    @Test
    fun importQualityHeader_isStable() {
        val csv = ResultsPackCsvBuilder.buildImportQualityCsv(emptyList())
        val header = csv.lineSequence().first()
        assertEquals(
            "importedAtIsoUtc,sourceType,filename,totalRows,insertedRows,rejectedRows,rejectedTimestampCount,rejectedDistanceCount,rejectedLuxCount,duplicatesRemovedCount,gapCount,largestGapSeconds,smoothingWindow,thresholds_lowLightLux,thresholds_nearworkCm,thresholds_breakGapSec,thresholds_minSessionSec,thresholds_closeDistanceCm,thresholds_extremeCloseCm",
            header
        )
    }

    @Test
    fun dailyRow_isWritten() {
        val csv = ResultsPackCsvBuilder.buildDailyResultsCsv(
            listOf(
                DailyResultsRow(
                    date = "2025-12-16",
                    sampleCount = 3,
                    diopterHoursTotal = 1.23,
                    // I include a non-zero daily NRS here so the row assertion proves the new column is emitted in-order.
                    nrsSessionAverage = 4.56,
                    lowLightMinutes = 4,
                    longestSessionSeconds = 60,
                    riskySessionCount = 1,
                    gapCount = 0,
                    largestGapSeconds = null
                )
            )
        )
        // I assert the new NRS slot here so the row-level test guards the daily export payload as well as the header.
        assertTrue(csv.contains("2025-12-16,3,1.23,4.56,4,60,1,0,"))
    }

    @Test
    fun sessionRow_isWritten() {
        val csv = ResultsPackCsvBuilder.buildSessionsResultsCsv(
            listOf(
                SessionResultsRow(
                    date = "2025-12-16",
                    sessionStartIsoUtc = "2025-12-16T07:00:00",
                    sessionEndIsoUtc = "2025-12-16T07:10:00",
                    durationSeconds = 600,
                    avgDistanceCm = 28.5,
                    minDistanceCm = 18.0,
                    // I include a finite session mean lux here so the row assertion proves the new descriptive light column is emitted.
                    meanLux = 512.34,
                    diopterHoursInSession = 1.2345,
                    // I include a non-zero session NRS here so the row assertion proves the new normalized score column is emitted.
                    nrs = 37.89,
                    lowLightSecondsInSession = 120,
                    flagsCloseDistance = 1,
                    flagsLowLight = 0,
                    flagsExtremeClose = 1
                )
            )
        )
        // I assert the ordered payload here so the test fails if meanLux or NRS shifts position in the exported row.
        assertTrue(csv.contains("2025-12-16,2025-12-16T07:00:00,2025-12-16T07:10:00,600,28.50,18.00,512.34,1.2345,37.89,120,1,0,1"))
    }
}
