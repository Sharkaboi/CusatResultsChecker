package com.sharkaboi.cusatresultschecker.extensions

import com.sharkaboi.cusatresultschecker.data.CusatResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

suspend fun getCatching(block: suspend () -> CusatResult): CusatResult {
    return withContext(Dispatchers.IO) {
        try {
            block()
        } catch (e: Exception) {
            e.printStackTrace()
            Timber.d(e.message ?: "")
            return@withContext CusatResult.ResultNotFound
        }
    }
}