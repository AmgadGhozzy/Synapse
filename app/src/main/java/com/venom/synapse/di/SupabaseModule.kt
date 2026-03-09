package com.venom.synapse.di
import com.venom.data.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SupabaseModule {

    @Provides
    @Singleton
    fun provideSupabaseClient(): SupabaseClient =
        createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,       // injected from local.properties
            supabaseKey = BuildConfig.SUPABASE_ANON_KEY,  // injected from local.properties
        ) {
            install(Postgrest)
            // install(Auth)   // for auth
            // install(Storage) // for storage
        }
}