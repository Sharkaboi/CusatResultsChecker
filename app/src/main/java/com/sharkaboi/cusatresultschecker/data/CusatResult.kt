package com.sharkaboi.cusatresultschecker.data

import androidx.annotation.Keep
import com.squareup.moshi.JsonClass
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory

sealed class CusatResult {
    object ResultNotFound : CusatResult()

    @Keep
    @JsonClass(generateAdapter = true)
    data class PassedResult(
        val cgpa: String,
        val studentDetails: StudentDetails,
        val subjectMarks: List<SubjectMark>
    ) : CusatResult()

    @Keep
    @JsonClass(generateAdapter = true)
    data class FailedResult(
        val studentDetails: StudentDetails,
        val subjectMarks: List<SubjectMark>
    ) : CusatResult()

    override fun toString(): String {
        return when (this) {
            ResultNotFound -> "ResultNotFound"
            else -> super.toString()
        }
    }

    companion object {
        val Adapter: PolymorphicJsonAdapterFactory<CusatResult> =
            PolymorphicJsonAdapterFactory.of(CusatResult::class.java, "type")
                .withSubtype(PassedResult::class.java, "PassedResult")
                .withSubtype(FailedResult::class.java, "FailedResult")
                .withDefaultValue(ResultNotFound)
    }
}

@Keep
@JsonClass(generateAdapter = true)
data class SubjectMark(
    val subCode: String,
    val subName: String,
    val grades: String,
    val hasPassed: String
)

fun CusatResult.getFormatted(): String {
    return when (this) {
        is CusatResult.FailedResult -> getFailedFormat(this)
        is CusatResult.PassedResult -> getPassedFormat(this)
        CusatResult.ResultNotFound -> "No results"
    }
}

fun getFailedFormat(failedResult: CusatResult.FailedResult): String {
    return buildString {
        appendSubjectMarks(failedResult.subjectMarks)
    }
}

private fun getPassedFormat(passedResult: CusatResult.PassedResult): String {
    return buildString {
        appendSubjectMarks(passedResult.subjectMarks)
        append("CGPA: ")
        append(passedResult.cgpa)
    }
}

private fun StringBuilder.appendSubjectMarks(list: List<SubjectMark>) {
    list.forEach {
        append(it.subCode)
        append("\n")
        append(it.subName)
        append("\n")
        append(it.grades)
        append("\n")
        append(it.hasPassed)
        append("\n")
        append("\n")
    }
}
