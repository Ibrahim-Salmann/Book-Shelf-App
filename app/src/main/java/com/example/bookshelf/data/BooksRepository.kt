package com.example.bookshelf.data

import android.util.Log
import com.example.bookshelf.network.BookApiService
import com.example.bookshelf.network.BookItem
import okhttp3.MultipartBody
import retrofit2.HttpException
import java.io.IOException

open class BooksRepository(
    private val apiService: BookApiService
) {
    private val tag = "BooksRepository"

    open suspend fun searchBooks(query: String, apiKey: String, userAgent: String): List<BookItem> {
        return try {
            Log.d(tag, "Searching for books with query: '$query'")
            val response = apiService.searchBooks(query, apiKey, userAgent)
            Log.d(tag, "Successfully found ${response.items?.size ?: 0} books for query: '$query'")
            response.items ?: emptyList()
        } catch (e: IOException) {
            Log.e(tag, "Network error while searching for books: ${e.message}", e)
            emptyList()
        } catch (e: HttpException) {
            Log.e(tag, "HTTP error while searching for books: ${e.code()} ${e.message()}", e)
            emptyList()
        } catch (e: Exception) {
            Log.e(tag, "An unexpected error occurred while searching for books: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Fetches the details for a single book volume from the API.
     *
     * @param bookId The unique ID of the book volume to fetch.
     * @return A [BookItem] containing the book's details, or null if an error occurred.
     */
    open suspend fun getBookDetails(bookId: String): BookItem? {
        return try {
            Log.d(tag, "Fetching details for book ID: $bookId")
            val book = apiService.getBookDetails(bookId)
            Log.d(tag, "Successfully fetched details for book: ${book.volumeInfo.title}")
            book
        } catch (e: IOException) {
            // This indicates a network-level error (e.g., no connectivity).
            Log.e(tag, "Network error fetching book details for ID '$bookId': ${e.message}", e)
            null
        } catch (e: HttpException) {
            // This indicates a non-successful HTTP response (e.g., 404 Not Found, 500 Server Error).
            Log.e(tag, "HTTP error fetching book details for ID '$bookId': ${e.code()} ${e.message()}", e)
            null
        } catch (e: Exception) {
            // Catch any other unexpected exceptions.
            Log.e(tag, "An unexpected error occurred fetching book details for ID '$bookId': ${e.message}", e)
            null
        }
    }

    open suspend fun createBook(book: BookItem): BookItem? {
        return try {
            apiService.createBook(book)
        } catch (e: Exception) {
            Log.e(tag, "Error creating book: ${e.message}", e)
            null
        }
    }

    open suspend fun submitReview(bookId: String, rating: Int, comment: String) {
        try {
            apiService.submitReview(bookId, rating, comment)
        } catch (e: Exception) {
            Log.e(tag, "Error submitting review: ${e.message}", e)
        }
    }

    open suspend fun uploadBookCover(bookId: String, coverImage: MultipartBody.Part, description: String) {
        try {
            apiService.uploadBookCover(bookId, coverImage, description)
        } catch (e: Exception) {
            Log.e(tag, "Error uploading book cover: ${e.message}", e)
        }
    }
}
