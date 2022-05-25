package ru.vladleesi.ultimatescanner.di

import android.content.Context
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ru.vladleesi.ultimatescanner.data.repository.AnalyzeRepo
import ru.vladleesi.ultimatescanner.ui.accessibility.SoundMaker
import ru.vladleesi.ultimatescanner.ui.accessibility.VoiceMaker
import ru.vladleesi.ultimatescanner.utils.Notifier
import java.lang.ref.WeakReference
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApplicationModule {

    // TODO: Separate into some scopes

    @Singleton
    @Provides
    fun bindAnalyticsService(@ApplicationContext context: Context): FirebaseAnalytics =
        FirebaseAnalytics.getInstance(context)

    @Singleton
    @Provides
    fun bindSoundMaker(@ApplicationContext context: Context): SoundMaker =
        SoundMaker(context)

    @Singleton
    @Provides
    fun bindTTS(@ApplicationContext context: Context): VoiceMaker =
        VoiceMaker.getInstance(context)

    @Singleton
    @Provides
    fun bindNotifier(@ApplicationContext context: Context): Notifier =
        Notifier(WeakReference(context))

    @Singleton
    @Provides
    fun bindAnalyzeRepo(@ApplicationContext context: Context): AnalyzeRepo =
        AnalyzeRepo(WeakReference(context))
}
