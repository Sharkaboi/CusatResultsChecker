package com.sharkaboi.cusatresultschecker.data

import com.sharkaboi.cusatresultschecker.constants.Constants
import com.sharkaboi.cusatresultschecker.extensions.getCatching
import kotlinx.coroutines.flow.firstOrNull
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class CusatClient(
    private val dataStoreRepository: DataStoreRepository
) {

    suspend fun getResult(): CusatResult = getCatching {
        val params = getParams()
        val doc = Jsoup.connect(Constants.cusatResultsSite)
            .userAgent(Constants.userAgents).apply {
                data("reg", "R")
                data("degree", "B.Tech")
                params.forEach {
                    data(it.key, it.value)
                }
            }.post()

        parseResult(doc)
    }

    private suspend fun getParams(): Map<String, String> {
        val params = dataStoreRepository.params.firstOrNull() ?: throw Exception("Params not set")
        return mapOf(
            Constants.SEM_KEY to params.sem,
            Constants.EDATE_KEY to params.edate,
            Constants.EXAM_KEY to params.exam,
            Constants.REG_NO_KEY to params.regNo,
            Constants.SCHEME_KEY to params.scheme,
        )
    }

    private suspend fun parseResult(doc: Document): CusatResult {
        val studentName = doc.selectXpath("//table[1]//td[1]/b").firstOrNull()
            ?: return CusatResult.ResultNotFound
        val studentDetails = StudentDetails(studentName.text())

        val resultTable =
            doc.selectXpath("//table[3]").firstOrNull() ?: return CusatResult.ResultNotFound

        val tableRows =
            resultTable.selectXpath("//table[3]//tr[position()>=2 and position() <= last()]")
        if (tableRows.size <= 1) {
            return CusatResult.ResultNotFound
        }

        val subjectMarks = mutableListOf<SubjectMark>()
        for (i in tableRows.indices) {
            if (i == 0) {
                continue
            }
            val tableDatas =
                resultTable.selectXpath("//table[3]//tr[${i + 1}]//td[position() <= last()]")
//            val groupedList = mutableListOf<Element>()
//            var i = 0
//            while (true) {
//                val subCode = tableDatas.getOrNull(i)?.text()
//                val subName = tableDatas.getOrNull(i+1)?.text()
//                val grade = tableDatas.getOrNull(i+2)?.text()
//                val hasPassed = tableDatas.getOrNull(i+3)?.text()
//                if(subCode == null || subName == null || grade == null || hasPassed == null) {
//                    break
//                    continue
//                }
//                groupedList.
//            }
            if (tableDatas.size != 4) {
                continue
            }

            val subCode = tableDatas[0].text()
            val subName = tableDatas[1].text()
            val grade = tableDatas[2].text()
            val hasPassed = tableDatas[3].text()
            val row = SubjectMark(
                subCode = subCode,
                subName = subName,
                grades = grade,
                hasPassed = hasPassed
            )
            subjectMarks.add(row)
        }

        if (subjectMarks.any { it.hasPassed.lowercase().trim() != "pass" }) {
            return CusatResult.FailedResult(
                studentDetails = studentDetails,
                subjectMarks = subjectMarks
            )
        }

        val cgpa =
            doc.selectXpath("//table[3]//tr[position()>1 and position() = last()]/td[position()=2]/b")
                .firstOrNull()?.text() ?: return CusatResult.FailedResult(
                studentDetails = studentDetails,
                subjectMarks = subjectMarks
            )

        return CusatResult.PassedResult(
            studentDetails = studentDetails,
            subjectMarks = subjectMarks,
            cgpa = cgpa
        )
    }
}