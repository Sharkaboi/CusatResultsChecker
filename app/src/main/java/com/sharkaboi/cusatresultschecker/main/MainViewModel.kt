package com.sharkaboi.cusatresultschecker.main

import android.content.Context
import androidx.lifecycle.*
import androidx.work.*
import com.sharkaboi.cusatresultschecker.constants.Constants
import com.sharkaboi.cusatresultschecker.data.CusatClient
import com.sharkaboi.cusatresultschecker.data.CusatResult
import com.sharkaboi.cusatresultschecker.data.DataStoreRepository
import com.sharkaboi.cusatresultschecker.data.Params
import com.sharkaboi.cusatresultschecker.worker.ResultCheckWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class MainViewModel
@Inject constructor(
    @ApplicationContext context: Context,
    private val dataStoreRepository: DataStoreRepository,
    private val cusatClient: CusatClient
) : ViewModel() {
    private val workManager by lazy { WorkManager.getInstance(context) }

    val isWorkerRunning: LiveData<Boolean> = Transformations.map(
        workManager.getWorkInfosByTagLiveData(Constants.workerTag)
    ) {
        it?.find { item -> !item.state.isFinished } != null
    }

    val params = dataStoreRepository.params.asLiveData()

    val result = dataStoreRepository.result.asLiveData()

    fun toggleWorkerStatus() = viewModelScope.launch {
        if (isWorkerRunning.value == true) {
            stopWorker()
        } else {
            checkAndStartWorker()
        }
    }

    fun saveNewParams(newParams: Params) = viewModelScope.launch {
        dataStoreRepository.setParams(newParams)
        dataStoreRepository.clearResults()
        checkAndStartWorker()
    }

    private suspend fun checkAndStartWorker() {
        val currentParams = params.value ?: return
        val isResultAvailable = isResultAvailableAlready(currentParams)
        if (isResultAvailable) return

        startWorker(currentParams)
    }

    private fun startWorker(currentParams: Params) {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val resultCheckWorker = PeriodicWorkRequestBuilder<ResultCheckWorker>(
            currentParams.repeatInterval,
            currentParams.repeatTimeUnit
        ).setConstraints(constraints)
            .addTag(Constants.workerTag)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                PeriodicWorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            ).build()
        workManager.enqueueUniquePeriodicWork(
            Constants.workerTag,
            // Force starts with new params
            ExistingPeriodicWorkPolicy.REPLACE,
            resultCheckWorker
        )
    }

    private suspend fun isResultAvailableAlready(currentParams: Params): Boolean {
        if (result.value is CusatResult.PassedResult
            || result.value is CusatResult.FailedResult
        ) {
            return true
        }

        val result = getResult()

        return result !is CusatResult.ResultNotFound
    }

    private suspend fun getResult(): CusatResult {
        val result = cusatClient.getResult()

        dataStoreRepository.setResults(result)
        return result
    }

    private suspend fun stopWorker() {
        dataStoreRepository.clearResults()
        workManager.cancelAllWorkByTag(Constants.workerTag)
    }

}