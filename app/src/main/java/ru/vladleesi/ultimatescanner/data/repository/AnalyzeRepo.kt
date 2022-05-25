package ru.vladleesi.ultimatescanner.data.repository

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import ru.vladleesi.ultimatescanner.data.local.AppDatabase
import ru.vladleesi.ultimatescanner.data.local.entity.HistoryEntity
import ru.vladleesi.ultimatescanner.data.remote.RetrofitClient
import ru.vladleesi.ultimatescanner.data.remote.adapter.toJson
import ru.vladleesi.ultimatescanner.data.remote.model.AnalyzeResultApi
import ru.vladleesi.ultimatescanner.ui.fragments.tabs.CameraModeHolder
import ru.vladleesi.ultimatescanner.ui.model.state.ResultState
import ru.vladleesi.ultimatescanner.utils.FileUtils
import ru.vladleesi.ultimatescanner.utils.PreferencesHelper
import java.io.File
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.*

class AnalyzeRepo(private val contextWeakReference: WeakReference<Context>) {

    private val preferencesHelper = PreferencesHelper(contextWeakReference)

    private val appDatabase by lazy { AppDatabase.invoke(contextWeakReference) }

    suspend fun testConnection(): String {
        val response = service().testConnection(preferencesHelper.endpoint())
        return buildTestConnectionMessage(response)
    }

    private fun service() =
        RetrofitClient(preferencesHelper).getAnalyzeService(DEFAULT_BASE_URL)

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

        val response = service().analyze(preferencesHelper.endpoint(), requestBody)

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

    companion object {
        const val DEFAULT_BASE_URL = "http://test.ru"
        const val DEFAULT_UPLOAD_ENDPOINT = "analyze"

        private const val ANALYZE_QUERY_IMAGE = "image"
        private const val ANALYZE_QUERY_DISCOVERED = "discovered"
        private const val ANALYZE_QUERY_CAMERA_MODE = "cameraMode"

        private const val DATE_PATTERN_HISTORY = "dd.MM.yyyy HH:mm:ss"
    }
}
