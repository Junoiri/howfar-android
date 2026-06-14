package com.example.nearworkthesis.settings

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DataStoreSettingsStoreTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val store = DataStoreSettingsStore(context)

    @Test
    fun setExtremeCloseThresholdCm_clampsToCloseDistanceMinusOne() = runTest {
        store.setCloseDistanceThresholdCm(30)
        store.setExtremeCloseThresholdCm(45)

        assertEquals(30, store.observeCloseDistanceThresholdCm().first())
        assertEquals(29, store.observeExtremeCloseThresholdCm().first())
    }

    @Test
    fun setCloseDistanceThresholdCm_clampsExistingExtremeCloseBelowIt() = runTest {
        store.setCloseDistanceThresholdCm(30)
        store.setExtremeCloseThresholdCm(20)

        store.setCloseDistanceThresholdCm(18)

        assertEquals(18, store.observeCloseDistanceThresholdCm().first())
        assertEquals(17, store.observeExtremeCloseThresholdCm().first())
    }
}
