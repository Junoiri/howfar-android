package com.example.nearworkthesis.domain.device

import org.junit.Assert.assertTrue
import org.junit.Test

class DeviceSettingsValidatorTest {

    @Test
    fun samplingIntervalOutOfRange_isInvalid() {
        val invalid = DeviceSettingsDefaults.defaults.copy(samplingIntervalSeconds = 0)
        val errors = DeviceSettingsValidator.validate(invalid)
        assertTrue(DeviceSettingsField.SamplingIntervalSeconds in errors.keys)
    }
}

