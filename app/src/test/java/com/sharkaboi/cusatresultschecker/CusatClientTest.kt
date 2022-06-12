package com.sharkaboi.cusatresultschecker

import android.util.Log
import com.sharkaboi.cusatresultschecker.data.CusatClient
import com.sharkaboi.cusatresultschecker.data.CusatResult
import com.sharkaboi.cusatresultschecker.data.DataStoreRepository
import com.sharkaboi.cusatresultschecker.data.Params
import com.sharkaboi.cusatresultschecker.util.disableSSL
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.util.concurrent.TimeUnit

class CusatClientTest {

    private var dataStoreRepository: DataStoreRepository = mockk(relaxed = true)
    private val client = CusatClient(dataStoreRepository)

    init {
        mockkStatic(Log::class)
        every { Log.v(any(), any()) } returns 0
        every { Log.d(any(), any()) } returns 0
        every { Log.i(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
    }

    @Test
    fun sampleScrapTest() = runBlocking {
        disableSSL()
        val params = Params(
            sem = "VIII",
            exam = "Regular",
            regNo = "20418061",
            scheme = "2015",
            edate = "APRIL 2022",
            repeatTimeUnit = TimeUnit.HOURS,
            repeatInterval = 1L
        )
        every { dataStoreRepository.params } returns flow { emit(params) }
        val result = client.getResult()
        println(result)
        assert(result is CusatResult.PassedResult)
    }
}