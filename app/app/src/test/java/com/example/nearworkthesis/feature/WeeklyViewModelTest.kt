package com.example.nearworkthesis.feature

import com.example.nearworkthesis.domain.DuplicateResolutionPolicy
import com.example.nearworkthesis.domain.analysis.AnalysisConfig
import com.example.nearworkthesis.domain.analysis.AnalysisPipelineConfig
import com.example.nearworkthesis.domain.analysis.AnalysisThresholds
import com.example.nearworkthesis.domain.analysis.AnalysisTimeHandling
import com.example.nearworkthesis.domain.analysis.DailySessionInsights
import com.example.nearworkthesis.domain.analysis.DataAnalysisDay
import com.example.nearworkthesis.domain.analysis.NearworkRiskScoreCalculator
import com.example.nearworkthesis.domain.export.ResultsPackCsvs
import com.example.nearworkthesis.domain.model.DailySummary
import com.example.nearworkthesis.domain.model.HistoryDaySummary
import com.example.nearworkthesis.domain.model.Measurement
import com.example.nearworkthesis.domain.model.MonthDaySummary
import com.example.nearworkthesis.domain.model.WeeklyDaySummary
import com.example.nearworkthesis.domain.repository.MeasurementInsertResult
import com.example.nearworkthesis.domain.repository.MeasurementRepository
import com.example.nearworkthesis.settings.ActiveProfileStore
import com.example.nearworkthesis.testutil.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WeeklyViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(UnconfinedTestDispatcher())

    @Test
    fun weeklyHeadlineNrs_sumsPerDayBarsWithoutRecomputingSessions() = runTest {
        val summaries = listOf(
            WeeklyDaySummary(
                day = "2026-06-01",
                sampleCount = 100,
                avgDistanceCm = 30.0,
                avgLux = 200.0,
                diopterHoursTotal = 1.0,
                nrs = 10.0,
                lowLightMinutes = 5,
                firstTimestampIso = null,
                lastTimestampIso = null
            ),
            WeeklyDaySummary(
                day = "2026-06-02",
                sampleCount = 120,
                avgDistanceCm = 32.0,
                avgLux = 220.0,
                diopterHoursTotal = 2.0,
                nrs = 40.0,
                lowLightMinutes = 3,
                firstTimestampIso = null,
                lastTimestampIso = null
            )
        )
        val repository = FakeWeeklyMeasurementRepository(summaries = summaries)
        val viewModel = WeeklyViewModel(
            measurementRepository = repository,
            activeProfileStore = FakeActiveProfileStore(1L),
            nearworkRiskScoreCalculator = NearworkRiskScoreCalculator()
        )

        advanceUntilIdle()

        val state = viewModel.uiState.value as WeeklyUiState.Data
        assertEquals(50.0, state.totalNrs, 1e-9)
    }
}

private class FakeWeeklyMeasurementRepository(
    private val summaries: List<WeeklyDaySummary>
) : MeasurementRepository {

    private val analysisConfig = AnalysisConfig(
        thresholds = AnalysisThresholds(
            lowLightThresholdLux = 55,
            nearworkDistanceThresholdCm = 60,
            breakGapSeconds = 60,
            minSessionDurationSeconds = 60,
            closeDistanceThresholdCm = 30,
            extremeCloseThresholdCm = 20
        ),
        pipeline = AnalysisPipelineConfig(
            smoothingWindowSize = 60,
            dedupeRule = "keep-last",
            distanceRangeMinCm = 10.0,
            distanceRangeMaxCm = 200.0,
            luxRangeMin = 0.0,
            luxRangeMax = 50_000.0,
            gapThresholdSeconds = 10
        ),
        timeHandling = AnalysisTimeHandling(
            timezoneId = "Europe/Warsaw",
            statement = "test"
        )
    )

    override suspend fun getMeasurementsForProfile(profileId: Long): List<Measurement> = emptyList()

    override suspend fun addMeasurements(
        measurements: List<Measurement>,
        duplicateResolutionPolicy: DuplicateResolutionPolicy
    ): MeasurementInsertResult {
        throw UnsupportedOperationException()
    }

    override suspend fun getLatestDay(profileId: Long): String? = summaries.maxOfOrNull { it.day }

    override suspend fun getMeasurementsForLocalDay(profileId: Long, localDay: String): List<Measurement> = emptyList()

    override fun getDailySummary(profileId: Long, day: String, config: AnalysisConfig?): Flow<DailySummary?> {
        throw UnsupportedOperationException()
    }

    override fun observeDailySessionInsights(profileId: Long, day: String, config: AnalysisConfig?): Flow<DailySessionInsights> {
        throw UnsupportedOperationException()
    }

    override suspend fun getDataAnalysisDay(profileId: Long, day: String, config: AnalysisConfig?): DataAnalysisDay {
        error("Weekly headline NRS should reuse per-day summaries and not recompute sessions.")
    }

    override fun getHistoryDays(profileId: Long): Flow<List<HistoryDaySummary>> = flowOf(emptyList())

    override fun observeDaySummariesInRange(profileId: Long, startDay: String, endDay: String, config: AnalysisConfig?): Flow<List<MonthDaySummary>> =
        flowOf(emptyList())

    override fun getLastNDays(profileId: Long, days: Int, config: AnalysisConfig?): Flow<List<WeeklyDaySummary>> =
        flowOf(summaries)

    override fun getDailySummariesInRange(profileId: Long, startDay: String, endDay: String, config: AnalysisConfig?): Flow<List<WeeklyDaySummary>> =
        flowOf(summaries)

    override fun observeAvailableDays(profileId: Long): Flow<List<String>> = flowOf(summaries.map { it.day })

    override fun observeMeasurementCount(profileId: Long): Flow<Int> = flowOf(0)

    override fun observeCurrentAnalysisConfig(profileId: Long): Flow<AnalysisConfig> = flowOf(analysisConfig)

    override suspend fun getCurrentAnalysisConfig(profileId: Long): AnalysisConfig = analysisConfig

    override suspend fun deleteDay(profileId: Long, localDay: String): Int {
        throw UnsupportedOperationException()
    }

    override suspend fun exportRawCsv(profileId: Long, startDay: String?, endDay: String?): String {
        throw UnsupportedOperationException()
    }

    override suspend fun exportProcessedCsv(profileId: Long, startDay: String?, endDay: String?, config: AnalysisConfig?): String {
        throw UnsupportedOperationException()
    }

    override suspend fun exportDailySummaryCsv(profileId: Long, startDay: String?, endDay: String?): String {
        throw UnsupportedOperationException()
    }

    override suspend fun exportAnalysisReportCsv(profileId: Long, startDay: String?, endDay: String?, config: AnalysisConfig?): String {
        throw UnsupportedOperationException()
    }

    override suspend fun exportResultsPackCsvs(profileId: Long, startDay: String?, endDay: String?, config: AnalysisConfig?): ResultsPackCsvs {
        throw UnsupportedOperationException()
    }
}

private class FakeActiveProfileStore(profileId: Long) : ActiveProfileStore {
    private val activeProfileId = MutableStateFlow<Long?>(profileId)

    override fun observeActiveProfileId(): Flow<Long?> = activeProfileId

    override suspend fun setActiveProfileId(id: Long) {
        activeProfileId.value = id
    }
}
