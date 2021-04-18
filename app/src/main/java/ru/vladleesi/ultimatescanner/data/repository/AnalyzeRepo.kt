package ru.vladleesi.ultimatescanner.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.preference.PreferenceManager
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
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

    fun testConnection(): Single<String> {
        return service.testConnection(endpointFromPrefs)
            .map { "${it.code()} ${it.message()}" }
            .onErrorReturn { it.message ?: it.toString() }
    }

    fun analyze(uri: Uri, discovered: Array<String>?): Single<AnalyzeState?> {

        val file = FileUtils.getFile(contextWeakReference.get(), uri) ?: return Single.just(
            AnalyzeState.Error(IllegalAccessException("Uploaded file not created"))
        )

        val mediaType =
            if (file.endsWith("png")) "image/png".toMediaTypeOrNull() else "image/jpeg".toMediaTypeOrNull()

        val requestBody: RequestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("image", file.name, file.asRequestBody(mediaType))
            .addFormDataPart("discovered", discovered.toJson())
            .build()

        return service.analyze(endpointFromPrefs, requestBody)
            .doOnError { Log.d(TAG, it.message ?: it.toString()) }
            .doOnSuccess { Log.i(TAG, "Analyze have been complete") }
            .map { response ->
                if (response.code() == 200) {
                    AnalyzeState.Success(response.body()?.data)
                } else {
                    AnalyzeState.Error(null)
                }
            }
    }

    fun saveToHistory(type: String, value: String): Completable {
        val simpleDateFormatTo = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())
        val date = simpleDateFormatTo.format(Date())
        return Completable.fromRunnable {
            appDatabase?.historyDao()?.insert(HistoryEntity(type, value, date))
        }.subscribeOn(Schedulers.io())
    }

    fun getHistory(): Observable<List<HistoryEntity>?> {
        return Observable.fromCallable {
            appDatabase?.historyDao()?.getAll()
        }.subscribeOn(Schedulers.io())
    }

    companion object {
        const val TAG = "AnalyzeRepo"
    }
}