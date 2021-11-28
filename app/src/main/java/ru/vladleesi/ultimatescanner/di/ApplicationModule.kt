package ru.vladleesi.ultimatescanner.di

import android.content.Context
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ru.vladleesi.ultimatescanner.ui.accessibility.VoiceMaker
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApplicationModule {

    @Singleton
    @Provides
    fun bindAnalyticsService(@ApplicationContext context: Context): FirebaseAnalytics =
        FirebaseAnalytics.getInstance(context)

    @Singleton
    @Provides
    fun bindTTS(@ApplicationContext context: Context): VoiceMaker =
        VoiceMaker.getInstance(context)
}
