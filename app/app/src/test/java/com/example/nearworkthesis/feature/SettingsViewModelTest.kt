package com.example.nearworkthesis.feature

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SettingsViewModelTest {

    @Test
    fun validateThresholdOrdering_returnsErrorWhenExtremeIsNotStrictlyLess() {
        assertEquals(
            SettingsViewModel.THRESHOLD_ORDERING_ERROR,
            SettingsViewModel.validateThresholdOrdering(
                closeDistanceThresholdCm = 30,
                extremeCloseThresholdCm = 30
            )
        )
    }

    @Test
    fun validateThresholdOrdering_acceptsStrictlyOrderedThresholds() {
        assertNull(
            SettingsViewModel.validateThresholdOrdering(
                closeDistanceThresholdCm = 30,
                extremeCloseThresholdCm = 20
            )
        )
    }
}
