package com.example.bookshelf.screens

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.bookshelf.data.AppContainer
import com.example.bookshelf.data.Book
import com.example.bookshelf.data.BooksRepository
import com.example.bookshelf.data.FirestoreRepository
import com.example.bookshelf.network.BookItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import java.io.IOException

sealed interface BooksApiState {
    object Success : BooksApiState
    data class Error(val message: String?) : BooksApiState
    object Loading : BooksApiState
}

class BookShelfViewModel(
    private val booksRepository: BooksRepository,
    private val firestoreRepository: FirestoreRepository
) : ViewModel() {

    private val apiKey = "AIzaSyD7ZpIeYG2YyeRlvelXRW7vrsjWimcRbbM"

    private val _searchResults = MutableStateFlow<List<BookItem>>(emptyList())
    val searchResults: StateFlow<List<BookItem>> = _searchResults.asStateFlow()

    private val _books = MutableStateFlow<List<Book>>(emptyList())
    val books: StateFlow<List<Book>> = _books.asStateFlow()

    private val _selectedBook = MutableStateFlow<BookItem?>(null)
    val selectedBook: StateFlow<BookItem?> = _selectedBook.asStateFlow()

    private val _booksApiState = MutableStateFlow<BooksApiState>(BooksApiState.Success)
    val booksApiState: StateFlow<BooksApiState> = _booksApiState.asStateFlow()

    init {
        loadBooks()
    }

    private fun loadBooks() {
        viewModelScope.launch {
            firestoreRepository.getBooksStream()
                .catch { e -> Log.e("BookShelfViewModel", "Error loading books", e) }
                .collect { _books.value = it }
        }
    }

    fun getBookDetails(bookId: String) {
        viewModelScope.launch {
            _booksApiState.value = BooksApiState.Loading
            try {
                _selectedBook.value = booksRepository.getBookDetails(bookId, apiKey)
                _booksApiState.value = BooksApiState.Success
            } catch (e: Exception) {
                _booksApiState.value = BooksApiState.Error("Failed to fetch book details.")
            }
        }
    }

    fun addBook(book: Book) {
        viewModelScope.launch {
            firestoreRepository.addBook(book)
        }
    }

    fun updateBook(book: Book) {
        viewModelScope.launch {
            firestoreRepository.updateBook(book)
        }
    }

    fun deleteBooks(bookIds: Set<String>) {
        viewModelScope.launch {
            val booksToDelete = _books.value.filter { it.id in bookIds }.toSet()
            booksToDelete.forEach { firestoreRepository.deleteBook(it) }
        }
    }

    fun clearShelf() {
        viewModelScope.launch {
            firestoreRepository.clearShelf()
        }
    }

    fun searchBooks(query: String) {
        viewModelScope.launch {
            _booksApiState.value = BooksApiState.Loading
            try {
                val result = booksRepository.searchBooks(query, apiKey, "my-awesome-app/1.0")
                _searchResults.value = result
                _booksApiState.value = BooksApiState.Success
            } catch (e: IOException) {
                Log.e("BookShelfViewModel", "IOException during book search", e)
                _searchResults.value = emptyList()
                _booksApiState.value = BooksApiState.Error(e.message)
            } catch (e: Exception) {
                Log.e("BookShelfViewModel", "Exception during book search", e)
                _searchResults.value = emptyList()
                _booksApiState.value = BooksApiState.Error(e.message)
            }
        }
    }

    fun createBook(book: BookItem) {
        viewModelScope.launch {
            _booksApiState.value = BooksApiState.Loading
            try {
                booksRepository.createBook(book)
                _booksApiState.value = BooksApiState.Success
            } catch (e: Exception) {
                _booksApiState.value = BooksApiState.Error("Failed to create book.")
            }
        }
    }

    fun submitReview(bookId: String, rating: Int, comment: String) {
        viewModelScope.launch {
            try {
                booksRepository.submitReview(bookId, rating, comment)
            } catch (e: Exception) {
                Log.e("BookShelfViewModel", "Error submitting review", e)
            }
        }
    }

    fun uploadBookCover(bookId: String, coverImage: MultipartBody.Part, description: String) {
        viewModelScope.launch {
            try {
                booksRepository.uploadBookCover(bookId, coverImage, description)
            } catch (e: Exception) {
                Log.e("BookShelfViewModel", "Error uploading cover", e)
            }
        }
    }

    companion object {
        /**
         * Factory for creating [BookShelfViewModel] instances.
         * This factory is used by the `viewModel()` Compose function to properly
         * instantiate the ViewModel with its required dependencies.
         */
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(BookShelfViewModel::class.java)) {
                    return BookShelfViewModel(
                        AppContainer.booksRepository,
                        AppContainer.firestoreRepository
                    ) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}
