package ru.vladleesi.ultimatescanner.repository

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
import ru.vladleesi.ultimatescanner.data.retrofit.RetrofitClient
import ru.vladleesi.ultimatescanner.data.room.AppDatabase
import ru.vladleesi.ultimatescanner.data.room.entity.HistoryEntity
import ru.vladleesi.ultimatescanner.model.AnalyzeState
import ru.vladleesi.ultimatescanner.utils.FileUtils
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.*

class AnalyzeRepo(private val contextWeakReference: WeakReference<Context>) {

    private val service by lazy { RetrofitClient().getAnalyzeService(contextWeakReference) }

    private val appDatabase by lazy { AppDatabase.invoke(contextWeakReference) }

    fun testConnection(): Single<String> {
        val endpoint = PreferenceManager.getDefaultSharedPreferences(contextWeakReference.get())
            .getString("endpoint", "/analyze") ?: "/analyze"

        return service.testConnection(endpoint)
            .map { "${it.code()} ${it.message()}" }
            .onErrorReturn { it.message ?: it.toString() }
    }

    fun analyze(uri: Uri): Single<AnalyzeState?> {

        val file = FileUtils.getFile(contextWeakReference.get(), uri) ?: return Single.just(
            AnalyzeState.Error(IllegalAccessException("Uploaded file not created"))
        )

        val mediaType =
            if (file.endsWith("png")) "image/png".toMediaTypeOrNull() else "image/jpeg".toMediaTypeOrNull()

        val requestBody: RequestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("image", file.name, file.asRequestBody(mediaType))
            .build()

        val endpoint = PreferenceManager.getDefaultSharedPreferences(contextWeakReference.get())
            .getString("endpoint", "/analyze") ?: "/analyze"

        return service.analyze(endpoint, requestBody)
            .doOnError { Log.d(TAG, it.message ?: it.toString()) }
            .doOnSuccess { Log.i(TAG, "Analyze have been complete") }
            .map {
                if (it.contentLength() > 0)
                    AnalyzeState.Success
                else
                    AnalyzeState.Error(null)
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