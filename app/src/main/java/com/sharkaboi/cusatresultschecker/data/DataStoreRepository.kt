package com.sharkaboi.cusatresultschecker.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.IOException

internal val Context.dataStore by preferencesDataStore(
    name = "mediahub"
)

private val moshi: Moshi = Moshi.Builder()
    .add(CusatResult.Adapter)
    .build()

private val paramsAdapter: JsonAdapter<Params> =
    moshi.adapter(Params::class.java)

private val cusatResultAdapter: JsonAdapter<CusatResult> =
    moshi.adapter(CusatResult::class.java)

val PARAMS = stringPreferencesKey("params")
val RESULTS = stringPreferencesKey("results")

class DataStoreRepository(private val dataStore: DataStore<Preferences>) {
    val params: Flow<Params?> = dataStore.data.catch { exception ->
        if (exception is IOException) {
            emit(emptyPreferences())
        } else {
            throw exception
        }
    }.map { preferences ->
        val json = preferences[PARAMS] ?: return@map null
        paramsAdapter.fromJson(json)
    }.flowOn(Dispatchers.IO)

    suspend fun setParams(params: Params) = withContext(Dispatchers.IO) {
        val json = paramsAdapter.toJson(params)
        dataStore.edit {
            it[PARAMS] = json
        }
    }

    suspend fun clearParams() = withContext(Dispatchers.IO) {
        dataStore.edit { it.remove(PARAMS) }
    }

    val result: Flow<CusatResult?> = dataStore.data.catch { exception ->
        if (exception is IOException) {
            emit(emptyPreferences())
        } else {
            throw exception
        }
    }.map { preferences ->
        val json = preferences[RESULTS] ?: return@map null
        cusatResultAdapter.fromJson(json)
    }.flowOn(Dispatchers.IO)

    suspend fun setResults(cusatResult: CusatResult) = withContext(Dispatchers.IO) {
        val json = cusatResultAdapter.toJson(cusatResult)
        dataStore.edit {
            it[RESULTS] = json
        }
    }

    suspend fun clearResults() = withContext(Dispatchers.IO) {
        dataStore.edit { it.remove(RESULTS) }
    }
}