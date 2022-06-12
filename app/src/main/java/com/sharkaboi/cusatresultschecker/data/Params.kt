package com.sharkaboi.cusatresultschecker.data

import androidx.annotation.Keep
import com.squareup.moshi.JsonClass
import java.util.concurrent.TimeUnit

@Keep
@JsonClass(generateAdapter = true)
data class Params(
    val sem: String,
    val edate: String,
    val exam: String,
    val regNo: String,
    val scheme: String,
    val repeatInterval: Long,
    val repeatTimeUnit: TimeUnit
)
