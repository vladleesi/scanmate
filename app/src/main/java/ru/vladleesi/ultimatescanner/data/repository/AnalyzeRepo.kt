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
import okhttp3.ResponseBody
import retrofit2.Response
import ru.vladleesi.ultimatescanner.R
import ru.vladleesi.ultimatescanner.data.local.AppDatabase
import ru.vladleesi.ultimatescanner.data.local.entity.HistoryEntity
import ru.vladleesi.ultimatescanner.data.remote.RetrofitClient
import ru.vladleesi.ultimatescanner.data.remote.adapter.toJson
import ru.vladleesi.ultimatescanner.data.remote.model.AnalyzeResultApi
import ru.vladleesi.ultimatescanner.extensions.get
import ru.vladleesi.ultimatescanner.ui.fragments.tabs.CameraModeHolder
import ru.vladleesi.ultimatescanner.ui.model.state.ResultState
import ru.vladleesi.ultimatescanner.utils.FileUtils
import java.io.File
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.*

class AnalyzeRepo(private val contextWeakReference: WeakReference<Context>) {

    private val defaultPreferences =
        PreferenceManager.getDefaultSharedPreferences(contextWeakReference.get())

    private val baseUrlFromPrefs by lazy {
        defaultPreferences.get(
            contextWeakReference.get()?.getString(R.string.settings_url),
            DEFAULT_BASE_URL
        ) ?: DEFAULT_BASE_URL
    }

    private val service by lazy { RetrofitClient().getAnalyzeService(baseUrlFromPrefs) }

    private val appDatabase by lazy { AppDatabase.invoke(contextWeakReference) }

    private val endpointFromPrefs by lazy {
        var endpoint = defaultPreferences.get(
            contextWeakReference.get()?.getString(R.string.settings_endpoint),
            DEFAULT_UPLOAD_ENDPOINT
        ) ?: DEFAULT_BASE_URL

        if (!baseUrlFromPrefs.endsWith("/") && !endpoint.startsWith("/")) {
            endpoint = "/$endpoint"
        }

        if (baseUrlFromPrefs.endsWith("/") && endpoint.startsWith("/")) {
            endpoint = endpoint.removePrefix("/")
        }

        return@lazy endpoint
    }

    suspend fun testConnection(): String {
        val response = service.testConnection(endpointFromPrefs)
        return buildTestConnectionMessage(response)
    }

    private fun buildTestConnectionMessage(response: Response<ResponseBody>) =
        "${response.code()} ${response.message()} ${response.body()?.string().orEmpty()}"

    suspend fun analyze(
        uri: Uri,
        discovered: Map<String, String>?
    ): ResultState<AnalyzeResultApi> {

        val filePath = FileUtils.getPathFrom(contextWeakReference.get(), uri)
            ?: return ResultState.Error(null)

        val file = File(filePath)

        val mediaType = FileUtils.getMimeType(contextWeakReference, uri)?.toMediaTypeOrNull()

        val requestBody: RequestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(ANALYZE_QUERY_IMAGE, file.name, file.asRequestBody(mediaType))
            .addFormDataPart(ANALYZE_QUERY_DISCOVERED, discovered.toJson())
            .addFormDataPart(ANALYZE_QUERY_CAMERA_MODE, CameraModeHolder.cameraMode.name)
            .build()

        val response = service.analyze(endpointFromPrefs, requestBody)

        return if (response.isSuccessful) {
            ResultState.Success(response.body())
        } else {
            ResultState.Error(null)
        }
    }

    suspend fun saveToHistory(result: Map<String, String>) {
        return withContext(Dispatchers.IO) {
            val simpleDateFormatTo = SimpleDateFormat(DATE_PATTERN_HISTORY, Locale.getDefault())
            val date = simpleDateFormatTo.format(Date())
            result.forEach { resultItem ->
                val entity = HistoryEntity(resultItem.key, resultItem.value, date)
                appDatabase?.historyDao()?.insert(entity)
            }
        }
    }

    suspend fun getHistory(): List<HistoryEntity>? {
        return withContext(Dispatchers.IO) {
            appDatabase?.historyDao()?.getAll()
        }
    }

    suspend fun clearHistory() {
        return withContext(Dispatchers.IO) {
            appDatabase?.historyDao()?.nukeTable()
        }
    }

    private companion object {
        private const val DEFAULT_BASE_URL = "http://test.ru"
        private const val DEFAULT_UPLOAD_ENDPOINT = "analyze"

        private const val ANALYZE_QUERY_IMAGE = "image"
        private const val ANALYZE_QUERY_DISCOVERED = "discovered"
        private const val ANALYZE_QUERY_CAMERA_MODE = "cameraMode"

        private const val DATE_PATTERN_HISTORY = "dd.MM.yyyy HH:mm:ss"
    }
}
