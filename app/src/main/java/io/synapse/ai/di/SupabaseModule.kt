package io.synapse.ai.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.functions.Functions
import io.github.jan.supabase.postgrest.Postgrest
import io.ktor.client.engine.okhttp.OkHttp
import io.synapse.ai.BuildConfig
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import kotlin.time.Duration.Companion.seconds

@Module
@InstallIn(SingletonComponent::class)
object SupabaseModule {

    @Provides
    @Singleton
    fun provideSupabaseClient(): SupabaseClient =
        createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_ANON_KEY,
        ) {
            requestTimeout = 180.seconds
            httpEngine = OkHttp.create {
                config {
                    readTimeout(180, TimeUnit.SECONDS)
                    connectTimeout(180, TimeUnit.SECONDS)
                    writeTimeout(180, TimeUnit.SECONDS)
                }
            }
            install(Auth)
            install(Postgrest)
            install(Functions)
        }
}