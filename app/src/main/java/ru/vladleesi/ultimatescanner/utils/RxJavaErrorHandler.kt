package ru.vladleesi.ultimatescanner.utils

import android.util.Log
import io.reactivex.rxjava3.exceptions.UndeliverableException
import io.reactivex.rxjava3.functions.Consumer
import java.io.IOException
import java.net.SocketException

class RxJavaErrorHandler : Consumer<Throwable> {

    override fun accept(throwable: Throwable?) {
        when (throwable) {
            is UndeliverableException -> {
                // TODO: Set all sources with UndeliverableException to default value onErrorReturn
                Log.w(
                    TAG,
                    "Set default value for onErrorReturn to source",
                    throwable
                )
            }
            is IOException, is SocketException -> {
                // fine, irrelevant network problem or API that throws on cancellation
            }
            is InterruptedException -> {
                // fine, irrelevant network problem or API that throws on cancellation
            }
            is NullPointerException, is IllegalArgumentException -> {
                // that's likely a bug in the application
                Thread.currentThread().uncaughtExceptionHandler?.uncaughtException(
                    Thread.currentThread(),
                    throwable
                )
            }
            is IllegalStateException -> {
                // that's a bug in RxJava or in a custom operator
                Thread.currentThread().uncaughtExceptionHandler?.uncaughtException(
                    Thread.currentThread(),
                    throwable
                )
            }
        }
    }

    companion object {
        const val TAG = "RxJavaErrorHandler"
    }
}