package com.sharkaboi.cusatresultschecker.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.sharkaboi.cusatresultschecker.R
import com.sharkaboi.cusatresultschecker.constants.Constants
import com.sharkaboi.cusatresultschecker.data.CusatClient
import com.sharkaboi.cusatresultschecker.data.CusatResult
import com.sharkaboi.cusatresultschecker.data.DataStoreRepository
import com.sharkaboi.cusatresultschecker.main.MainActivity
import com.sharkaboi.cusatresultschecker.util.disableSSL
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

@HiltWorker
class ResultCheckWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val dataStoreRepository: DataStoreRepository,
    private val cusatClient: CusatClient
) : CoroutineWorker(context, params) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        return@withContext try {
            val shouldCheckResult = shouldCheckResult()
            if (!shouldCheckResult) Result.success()

            checkResult()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }

    private suspend fun checkResult(): Result {
        disableSSL()
        val result = cusatClient.getResult()
        if (result is CusatResult.ResultNotFound) {
            return Result.retry()
        }

        notifyResultFound(result)
        dataStoreRepository.setResults(result)
        return Result.success()
    }

    private fun notifyResultFound(result: CusatResult) {
        val message = if (result is CusatResult.PassedResult) {
            "Passed - CGPA : ${result.cgpa}"
        } else {
            "Failed"
        }

        createNotificationChannel()
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(
            applicationContext,
            Constants.NOTIFICATION_CHANNEL_ID
        ).setSmallIcon(R.drawable.ic_notify)
            .setContentTitle("Result found!")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(applicationContext)) {
            notify(0, builder.build())
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }

        val name = Constants.NOTIFICATION_CHANNEL_NAME
        val descriptionText = Constants.NOTIFICATION_CHANNEL_DESC
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(
            Constants.NOTIFICATION_CHANNEL_ID,
            name,
            importance
        ).apply {
            description = descriptionText
        }
        notificationManager.createNotificationChannel(channel)
    }


    private suspend fun shouldCheckResult(): Boolean {
        val result = dataStoreRepository.result.firstOrNull()
        return result == null || result is CusatResult.ResultNotFound
    }

    companion object {
        private const val TAG = "ResultCheckWorker"
    }
}
