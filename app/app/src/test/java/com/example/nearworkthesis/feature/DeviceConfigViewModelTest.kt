// Tests the device config ViewModel flow with a fake repository.
package com.example.nearworkthesis.feature

import com.example.nearworkthesis.domain.device.DeviceConfigRepository
import com.example.nearworkthesis.domain.device.DeviceConnectionState
import com.example.nearworkthesis.domain.device.DeviceSettings
import com.example.nearworkthesis.domain.device.DeviceSettingsDefaults
import com.example.nearworkthesis.testutil.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DeviceConfigViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(UnconfinedTestDispatcher())

    @Test
    fun applySettings_success_emitsReadyState() = runTest {
        val repo = FakeDeviceConfigRepository()
        val vm = DeviceConfigViewModel(repository = repo, ioDispatcher = UnconfinedTestDispatcher(testScheduler))

        // init triggers refreshFromDevice() immediately; state should become Ready quickly.
        advanceUntilIdle()
        val ready = vm.uiState.value as DeviceConfigUiState.Ready
        assertTrue(!ready.isBusy)

        vm.applySettings()
        advanceUntilIdle()

        val after = vm.uiState.value as DeviceConfigUiState.Ready
        assertTrue(!after.isBusy)
        assertTrue(repo.lastWritten != null)
    }

    @Test
    fun clearDeviceData_success_callsRepository_andReturnsReadyState() = runTest {
        val repo = FakeDeviceConfigRepository()
        val vm = DeviceConfigViewModel(repository = repo, ioDispatcher = UnconfinedTestDispatcher(testScheduler))

        advanceUntilIdle()
        vm.clearDeviceData()
        advanceUntilIdle()

        val after = vm.uiState.value as DeviceConfigUiState.Ready
        assertTrue(!after.isBusy)
        assertTrue(repo.lastCleared != null)
    }
}

private class FakeDeviceConfigRepository : DeviceConfigRepository {
    var lastWritten: DeviceSettings? = null
    var lastCleared: DeviceSettings? = null

    override fun observeConnectionState(): Flow<DeviceConnectionState> = flowOf(DeviceConnectionState.Connected)
    override fun refreshConnection() = Unit

    override suspend fun readSettings(): Result<DeviceSettings> = Result.success(DeviceSettingsDefaults.defaults)

    override suspend fun writeSettings(settings: DeviceSettings): Result<Unit> {
        lastWritten = settings
        return Result.success(Unit)
    }

    override suspend fun clearDeviceData(settings: DeviceSettings): Result<Unit> {
        lastCleared = settings
        return Result.success(Unit)
    }

    override suspend fun buildConfigUf2(settings: DeviceSettings): Result<ByteArray> = Result.success(ByteArray(4))

    override suspend fun resetToDefaults(): Result<DeviceSettings> = Result.success(DeviceSettingsDefaults.defaults)
}





