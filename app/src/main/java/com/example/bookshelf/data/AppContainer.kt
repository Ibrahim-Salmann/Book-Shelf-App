package com.example.bookshelf.data

import com.example.bookshelf.network.BookApiService
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit

/**
 * A simple, manual dependency injection container.
 * 
 * This object acts as a service locator, creating and providing singleton instances of
 * important application components like services and repositories. This pattern is used
 * to decouple classes from the responsibility of creating their own dependencies, making
 * the code more modular, flexible, and easier to test.
 */
object AppContainer {
    private const val BASE_URL = "https://www.googleapis.com/books/v1/"

    private val json = Json { ignoreUnknownKeys = true }

    private val retrofit: Retrofit = Retrofit.Builder()
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .baseUrl(BASE_URL)
        .build()

    /**
     * Provides a lazily-initialized instance of [BookApiService] for making network calls.
     */
    val retrofitService: BookApiService by lazy {
        retrofit.create(BookApiService::class.java)

    }

    /**
     * Provides a lazily-initialized instance of [BooksRepository] for fetching book search results.
     */
    val booksRepository: BooksRepository by lazy {
        BooksRepository(retrofitService)
    }

    /**
     * Provides a lazily-initialized instance of [FirestoreRepository] for all Firestore database operations.
     */
    val firestoreRepository: FirestoreRepository by lazy {
        FirestoreRepository()
    }
}
