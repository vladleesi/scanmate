package ru.vladleesi.ultimatescanner.data.repository

import android.content.Context
import android.net.Uri
import androidx.preference.PreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import ru.vladleesi.ultimatescanner.data.local.AppDatabase
import ru.vladleesi.ultimatescanner.data.local.entity.HistoryEntity
import ru.vladleesi.ultimatescanner.data.remote.RetrofitClient
import ru.vladleesi.ultimatescanner.data.remote.adapter.toJson
import ru.vladleesi.ultimatescanner.ui.model.AnalyzeState
import ru.vladleesi.ultimatescanner.utils.FileUtils
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.*

class AnalyzeRepo(private val contextWeakReference: WeakReference<Context>) {

    private val service by lazy { RetrofitClient().getAnalyzeService(contextWeakReference) }

    private val appDatabase by lazy { AppDatabase.invoke(contextWeakReference) }

    private val endpointFromPrefs by lazy {
        PreferenceManager.getDefaultSharedPreferences(contextWeakReference.get())
            .getString("endpoint", "/analyze") ?: "/analyze"
    }

    suspend fun testConnectionAsync(): String {
        val response = service
            .testConnection(endpointFromPrefs)
        return "${response.code()} ${response.message()}"
    }

    suspend fun analyze(uri: Uri, discovered: Array<String>?): AnalyzeState {

        val file = FileUtils.getFile(contextWeakReference.get(), uri) ?: return AnalyzeState.Error(
            IllegalAccessException("Uploaded file not created")
        )

        val mediaType =
            if (file.endsWith("png")) "image/png".toMediaTypeOrNull() else "image/jpeg".toMediaTypeOrNull()

        val requestBody: RequestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("image", file.name, file.asRequestBody(mediaType))
            .addFormDataPart("discovered", discovered.toJson())
            .build()

        val response = service.analyze(endpointFromPrefs, requestBody)
        return if (response.code() == 200) {
            AnalyzeState.Success(response.body()?.data)
        } else {
            AnalyzeState.Error(null)
        }
    }

    suspend fun saveToHistory(type: String, value: String) {
        return withContext(Dispatchers.IO) {
            val simpleDateFormatTo = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())
            val date = simpleDateFormatTo.format(Date())
            appDatabase?.historyDao()?.insert(HistoryEntity(type, value, date))
        }
    }

    suspend fun getHistory(): List<HistoryEntity>? {
        return withContext(Dispatchers.IO) {
            appDatabase?.historyDao()?.getAll()
        }
    }

    companion object {
        const val TAG = "AnalyzeRepo"
    }
}