package com.example.bookshelf.data

import com.example.bookshelf.network.BookApiService
import com.example.bookshelf.network.BookItem
import com.example.bookshelf.network.BookSearchResponse
import com.example.bookshelf.network.VolumeInfo
import kotlinx.coroutines.test.runTest
import okhttp3.MultipartBody
import org.junit.Assert.assertEquals
import org.junit.Test

class BooksRepositoryTest {

    @Test
    fun `searchBooks returns mapped results on success`() = runTest {
        val expected = listOf(BookItem("1", VolumeInfo(title = "Test Book")))
        val apiService = FakeBookApiService(searchResponse = BookSearchResponse(expected))
        val repository = BooksRepository(apiService)

        val result = repository.searchBooks("test", "key", "agent")

        assertEquals(expected, result)
    }

    @Test
    fun `getBookDetails returns correct item`() = runTest {
        val bookId = "1"
        val expected = BookItem(bookId, VolumeInfo(title = "Detailed Book"))
        val apiService = FakeBookApiService(detailsResponse = expected)
        val repository = BooksRepository(apiService)

        val result = repository.getBookDetails(bookId)

        assertEquals(expected, result)
    }
}

// --- Fake API Service for Testing ---

class FakeBookApiService(
    private val searchResponse: BookSearchResponse = BookSearchResponse(emptyList()),
    private val detailsResponse: BookItem? = null
) : BookApiService {

    override suspend fun searchBooks(query: String, apiKey: String, userAgent: String): BookSearchResponse {
        return searchResponse
    }

    override suspend fun getBookDetails(bookId: String): BookItem {
        return detailsResponse ?: throw Exception("No details found")
    }

    override suspend fun createBook(book: BookItem): BookItem {
        TODO("Not yet implemented")
    }

    override suspend fun submitReview(bookId: String, rating: Int, comment: String) {
        TODO("Not yet implemented")
    }

    override suspend fun uploadBookCover(
        bookId: String,
        coverImage: MultipartBody.Part,
        description: String
    ) {
        TODO("Not yet implemented")
    }
}