package com.example.bookshelf.screens

import com.example.bookshelf.data.Book
import com.example.bookshelf.data.BooksRepository
import com.example.bookshelf.data.FirestoreRepository
import com.example.bookshelf.network.BookItem
import com.example.bookshelf.network.VolumeInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.MultipartBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BookShelfViewModelTest {

    private lateinit var viewModel: BookShelfViewModel
    private lateinit var booksRepository: FakeBooksRepository
    private lateinit var firestoreRepository: FakeFirestoreRepository
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        booksRepository = FakeBooksRepository()
        firestoreRepository = FakeFirestoreRepository()
        viewModel = BookShelfViewModel(booksRepository, firestoreRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `searchBooks updates searchResults on success`() = runTest {
        val query = "test"
        val expected = listOf(BookItem("1", VolumeInfo(title = "Test Book")))
        booksRepository.setSearchResults(expected)

        viewModel.searchBooks(query)
        testDispatcher.scheduler.advanceUntilIdle() // Wait for coroutines

        assertEquals(expected, viewModel.searchResults.value)
        assertEquals(BooksApiState.Success, viewModel.booksApiState.value)
    }

    @Test
    fun `getBookDetails updates selectedBook`() = runTest {
        val bookId = "1"
        val expected = BookItem(bookId, VolumeInfo(title = "Detailed Book"))
        booksRepository.setBookDetails(expected)

        viewModel.getBookDetails(bookId)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(expected, viewModel.selectedBook.value)
    }

    @Test
    fun `addBook calls repository`() = runTest {
        val book = Book(name = "New Book")
        viewModel.addBook(book)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(book.name, firestoreRepository.addedBook?.name)
    }

    @Test
    fun `deleteBooks calls repository`() = runTest {
        val initialBooks = listOf(Book(id = "1"), Book(id = "2"))
        firestoreRepository.setBooks(initialBooks)
        viewModel = BookShelfViewModel(booksRepository, firestoreRepository) // Re-init to collect books
        testDispatcher.scheduler.advanceUntilIdle()

        val bookIdsToDelete = setOf("1")
        viewModel.deleteBooks(bookIdsToDelete)
        testDispatcher.scheduler.advanceUntilIdle()

        // Create a set of the IDs of the books that were passed to deleteBook
        val deletedIds = firestoreRepository.deletedBooks.map { it.id }.toSet()
        assertEquals(bookIdsToDelete, deletedIds)
    }

    @Test
    fun `clearShelf calls repository`() = runTest {
        viewModel.clearShelf()
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(firestoreRepository.isShelfCleared)
    }
}

// --- Fake Repositories for Testing ---

class FakeBooksRepository : BooksRepository(FakeBookApiService()) {
    private var searchResults: List<BookItem> = emptyList()
    private var bookDetails: BookItem? = null

    fun setSearchResults(results: List<BookItem>) {
        searchResults = results
    }

    fun setBookDetails(details: BookItem?) {
        bookDetails = details
    }

    override suspend fun searchBooks(query: String, apiKey: String, userAgent: String): List<BookItem> {
        return searchResults
    }

    override suspend fun getBookDetails(bookId: String): BookItem? {
        return bookDetails
    }
}

class FakeFirestoreRepository : FirestoreRepository() {
    var addedBook: Book? = null
    val deletedBooks = mutableListOf<Book>()
    var isShelfCleared = false
    private val booksFlow = MutableStateFlow<List<Book>>(emptyList())

    fun setBooks(books: List<Book>) {
        booksFlow.value = books
    }

    override fun getBooksStream(): Flow<List<Book>> = booksFlow.asStateFlow()

    override suspend fun addBook(book: Book) {
        addedBook = book
    }

    override suspend fun deleteBook(book: Book) {
        deletedBooks.add(book)
    }

    override suspend fun clearShelf() {
        isShelfCleared = true
    }
}

class FakeBookApiService : com.example.bookshelf.network.BookApiService {
    override suspend fun searchBooks(query: String, apiKey: String, userAgent: String) = com.example.bookshelf.network.BookSearchResponse(emptyList())
    override suspend fun getBookDetails(bookId: String) = BookItem("", VolumeInfo(title = ""))
    override suspend fun createBook(book: BookItem): BookItem {
        return book
    }

    override suspend fun submitReview(bookId: String, rating: Int, comment: String) {
        // Not implemented for fake
    }

    override suspend fun uploadBookCover(
        bookId: String,
        coverImage: MultipartBody.Part,
        description: String
    ) {
        // Not implemented for fake
    }
}
