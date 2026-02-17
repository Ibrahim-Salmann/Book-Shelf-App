package com.example.bookshelf.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.SubcomposeAsyncImage
import com.example.bookshelf.data.Book
import com.example.bookshelf.network.BookItem
import com.example.bookshelf.network.ImageLinks
import com.example.bookshelf.network.VolumeInfo
import com.example.bookshelf.ui.theme.BookShelfTheme
import com.example.bookshelf.ui.theme.ScanLines

@Composable
fun BookShelfScreen(
    modifier: Modifier = Modifier,
    viewModel: BookShelfViewModel = viewModel(factory = BookShelfViewModel.Factory)
) {
    val searchResults by viewModel.searchResults.collectAsState()
    val books by viewModel.books.collectAsState()
    val selectedBook by viewModel.selectedBook.collectAsState()
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var isSearchMode by remember { mutableStateOf(false) }
    var showDetailsDialog by remember { mutableStateOf(false) }
    var selectedBooks by remember { mutableStateOf(emptySet<Book>()) }
    val isInSelectionMode = selectedBooks.isNotEmpty()
    val booksApiState by viewModel.booksApiState.collectAsState()
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var showClearShelfConfirmation by remember { mutableStateOf(false) }

    fun onSearchQueryChange(query: String) {
        searchQuery = query
    }

    fun onBookClick(bookId: String) {
        viewModel.getBookDetails(bookId)
        showDetailsDialog = true
    }

    fun onDismissDetailsDialog() {
        showDetailsDialog = false
    }

    fun onToggleSearchMode() {
        isSearchMode = !isSearchMode
        selectedBooks = emptySet() // Clear selection when toggling search mode
    }

    fun onAddBook(book: Book) {
        viewModel.addBook(book)
        showDetailsDialog = false
        isSearchMode = false
    }

    fun showDeleteConfirmationDialog() {
        showDeleteConfirmation = true
    }

    fun showClearShelfConfirmationDialog() {
        showClearShelfConfirmation = true
    }

    fun onDismissDeleteConfirmation() {
        showDeleteConfirmation = false
    }

    fun onDismissClearShelfConfirmation() {
        showClearShelfConfirmation = false
    }

    fun onDeleteSelectedBooks() {
        viewModel.deleteBooks(selectedBooks.map { it.id }.toSet())
        selectedBooks = emptySet()
        onDismissDeleteConfirmation()
    }

    fun onToggleBookSelection(book: Book) {
        selectedBooks = if (book in selectedBooks) {
            selectedBooks - book
        } else {
            selectedBooks + book
        }
    }

    fun onClearSelection() {
        selectedBooks = emptySet()
    }

    fun onClearShelf() {
        viewModel.clearShelf()
        onDismissClearShelfConfirmation()
    }

    if (showDetailsDialog) {
        selectedBook?.let {
            BookDetailsDialog(
                bookItem = it,
                onDismiss = ::onDismissDetailsDialog,
                onAddBook = ::onAddBook
            )
        }
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = ::onDismissDeleteConfirmation,
            title = { Text("Delete Books") },
            text = { Text("Are you sure you want to delete the selected books?") },
            confirmButton = {
                Button(onClick = ::onDeleteSelectedBooks) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = ::onDismissDeleteConfirmation) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showClearShelfConfirmation) {
        AlertDialog(
            onDismissRequest = ::onDismissClearShelfConfirmation,
            title = { Text("Clear Shelf") },
            text = { Text("Are you sure you want to clear your entire shelf? This action cannot be undone.") },
            confirmButton = {
                Button(onClick = ::onClearShelf) {
                    Text("Clear")
                }
            },
            dismissButton = {
                TextButton(onClick = ::onDismissClearShelfConfirmation) {
                    Text("Cancel")
                }
            }
        )
    }

    BookShelfContent(
        modifier = modifier,
        searchQuery = searchQuery,
        onSearchQueryChange = ::onSearchQueryChange,
        onSearchClick = { viewModel.searchBooks(searchQuery) },
        searchResults = searchResults,
        onBookClick = { onBookClick(it.id) },
        isSearchMode = isSearchMode,
        onToggleSearchMode = ::onToggleSearchMode,
        books = books,
        onDeleteSelectedBooks = ::showDeleteConfirmationDialog,
        selectedBooks = selectedBooks,
        onToggleBookSelection = ::onToggleBookSelection,
        isInSelectionMode = isInSelectionMode,
        onClearSelection = ::onClearSelection,
        onBackFromSearch = ::onToggleSearchMode,
        booksApiState = booksApiState,
        onClearShelf = ::showClearShelfConfirmationDialog
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookShelfContent(
    modifier: Modifier = Modifier,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearchClick: () -> Unit,
    searchResults: List<BookItem>,
    onBookClick: (BookItem) -> Unit,
    isSearchMode: Boolean,
    onToggleSearchMode: () -> Unit,
    onBackFromSearch: () -> Unit,
    books: List<Book>,
    onDeleteSelectedBooks: () -> Unit,
    selectedBooks: Set<Book>,
    onToggleBookSelection: (Book) -> Unit,
    isInSelectionMode: Boolean,
    onClearSelection: () -> Unit,
    booksApiState: BooksApiState,
    onClearShelf: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        ScanLines()
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = {
                    if (isInSelectionMode) {
                        Text("${selectedBooks.size} selected")
                    } else if (isSearchMode) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = onSearchQueryChange,
                            label = { Text("Search for books") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text("My Bookshelf")
                    }
                },
                navigationIcon = {
                    if (isInSelectionMode) {
                        IconButton(onClick = onClearSelection) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Clear selection"
                            )
                        }
                    } else if (isSearchMode) {
                        IconButton(onClick = onBackFromSearch) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                },
                actions = {
                    if (isInSelectionMode) {
                        IconButton(onClick = onDeleteSelectedBooks) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete selected books")
                        }
                    } else if (isSearchMode) {
                        IconButton(onClick = onSearchClick) {
                            Icon(Icons.Filled.Search, contentDescription = "Search")
                        }
                    } else {
                        IconButton(onClick = onToggleSearchMode) {
                            Icon(Icons.Filled.Search, contentDescription = "Search")
                        }
                        IconButton(onClick = { showMenu = !showMenu }) {
                            Icon(Icons.Filled.MoreVert, contentDescription = "More")
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Clear Shelf") },
                                onClick = {
                                    onClearShelf()
                                    showMenu = false
                                }
                            )
                        }
                    }
                }
            )
            if (isSearchMode) {
                when (booksApiState) {
                    is BooksApiState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }

                    is BooksApiState.Success -> {
                        LazyColumn(modifier = Modifier.weight(1f)) {
                            items(searchResults) { bookItem ->
                                BookSearchResultItem(bookItem = bookItem) {
                                    onBookClick(bookItem)
                                }
                            }
                        }
                    }

                    is BooksApiState.Error -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "Error: ${booksApiState.message}")
                        }
                    }
                }
            } else {
                if (books.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Your bookshelf is empty. Add books by searching.",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                } else {
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(books) { book ->
                            ShelfBookItem(
                                book = book,
                                isSelected = book in selectedBooks,
                                onClick = {
                                    if (isInSelectionMode) {
                                        onToggleBookSelection(book)
                                    }
                                },
                                onLongClick = { onToggleBookSelection(book) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ShelfBookItem(
    book: Book,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.errorContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SubcomposeAsyncImage(
                model = book.coverUri,
                contentDescription = book.name,
                modifier = Modifier.size(80.dp, 120.dp),
                loading = { CircularProgressIndicator() },
                error = {
                    Box(
                        modifier = Modifier
                            .size(80.dp, 120.dp)
                            .background(MaterialTheme.colorScheme.secondaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No Image")
                    }
                }
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = book.name, style = MaterialTheme.typography.titleMedium)
                Text(text = book.author, style = MaterialTheme.typography.bodyMedium)
                Text(text = "Status: ${book.status}", style = MaterialTheme.typography.bodySmall)
                Text(text = "Rating: ${book.rating}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun BookSearchResultItem(bookItem: BookItem, onBookClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onBookClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SubcomposeAsyncImage(
            model = bookItem.volumeInfo.imageLinks?.thumbnail?.replace("http://", "https://"),
            contentDescription = bookItem.volumeInfo.title,
            modifier = Modifier.size(60.dp, 90.dp),
            loading = { CircularProgressIndicator() },
            error = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No Image")
                }
            }
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = bookItem.volumeInfo.title, style = MaterialTheme.typography.titleMedium)
            Text(text = bookItem.volumeInfo.authors?.joinToString(", ") ?: "Unknown Author")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailsDialog(
    bookItem: BookItem,
    onDismiss: () -> Unit,
    onAddBook: (Book) -> Unit
) {
    var status by remember { mutableStateOf("Plan to Read") }
    var rating by remember { mutableIntStateOf(0) }
    val statuses = listOf("Currently Reading", "Completed", "Paused", "Dropped", "Plan to Read")
    var statusExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(bookItem.volumeInfo.title) },
        text = {
            Column {
                Text("by ${bookItem.volumeInfo.authors?.joinToString(", ") ?: "N/A"}")
                Spacer(modifier = Modifier.height(16.dp))
                ExposedDropdownMenuBox(
                    expanded = statusExpanded,
                    onExpandedChange = { statusExpanded = !statusExpanded }) {
                    TextField(
                        value = status,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Status") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded)
                        },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = statusExpanded,
                        onDismissRequest = { statusExpanded = false }) {
                        statuses.forEach { selectionOption ->
                            DropdownMenuItem(
                                text = { Text(selectionOption) },
                                onClick = {
                                    status = selectionOption
                                    statusExpanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Rating: $rating")
                Slider(
                    value = rating.toFloat(),
                    onValueChange = { rating = it.toInt() },
                    valueRange = 0f..5f,
                    steps = 4
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val book = Book(
                    name = bookItem.volumeInfo.title,
                    author = bookItem.volumeInfo.authors?.joinToString(", ") ?: "N/A",
                    coverUri = bookItem.volumeInfo.imageLinks?.thumbnail?.replace(
                        "http://",
                        "https://"
                    ),
                    status = status,
                    rating = rating
                )
                onAddBook(book)
            }) {
                Text("Add")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun BookSearchResultItemPreview() {
    BookShelfTheme {
        BookSearchResultItem(
            bookItem = BookItem(
                id = "1",
                volumeInfo = VolumeInfo(
                    title = "The Great Gatsby",
                    authors = listOf("F. Scott Fitzgerald"),
                    description = "The Great Gatsby is a 1925 novel by American writer F. Scott Fitzgerald. Set in the Jazz Age on Long Island, the novel depicts narrator Nick Caraway'''s interactions with mysterious millionaire Jay Gatsby and Gatsby'''s obsession to reunite with his former love, Daisy Buchanan.",
                    imageLinks = ImageLinks(thumbnail = "http://books.google.com/books/content?id=zaRoPwAACAAJ&printsec=frontcover&img=1&zoom=1&source=gbs_api")
                )
            ),
            onBookClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ShelfBookItemPreview() {
    BookShelfTheme {
        ShelfBookItem(
            book = Book(
                name = "Dune",
                author = "Frank Herbert",
                status = "Completed",
                coverUri = "",
                rating = 4
            ),
            isSelected = false,
            onClick = {},
            onLongClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun BookShelfScreenContentPreview() {
    BookShelfTheme {
        BookShelfContent(
            searchQuery = "Query",
            onSearchQueryChange = {},
            onSearchClick = {},
            searchResults = emptyList(),
            onBookClick = { },
            isSearchMode = false,
            onToggleSearchMode = { },
            books = emptyList(),
            onDeleteSelectedBooks = {},
            selectedBooks = emptySet(),
            onToggleBookSelection = { },
            isInSelectionMode = false,
            onClearSelection = {},
            onBackFromSearch = {},
            booksApiState = BooksApiState.Success,
            onClearShelf = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun BookShelfScreenSearchPreview() {
    BookShelfTheme {
        val searchResults = listOf(
            BookItem(
                id = "1",
                volumeInfo = VolumeInfo(
                    title = "The Great Gatsby",
                    authors = listOf("F. Scott Fitzgerald"),
                    description = "A novel about the American dream.",
                    imageLinks = ImageLinks(thumbnail = "http://books.google.com/books/content?id=zaRoPwAACAAJ&printsec=frontcover&img=1&zoom=1&source=gbs_api")
                )
            ),
            BookItem(
                id = "2",
                volumeInfo = VolumeInfo(
                    title = "To Kill a Mockingbird",
                    authors = listOf("Harper Lee"),
                    description = "A novel about justice and race.",
                    imageLinks = ImageLinks(thumbnail = "http://books.google.com/books/content?id=zaRoPwAACAAJ&printsec=frontcover&img=1&zoom=1&source=gbs_api")
                )
            )
        )

        BookShelfContent(
            searchQuery = "Gatsby",
            onSearchQueryChange = {},
            onSearchClick = {},
            searchResults = searchResults,
            onBookClick = { },
            isSearchMode = true,
            onToggleSearchMode = { },
            books = emptyList(),
            onDeleteSelectedBooks = {},
            selectedBooks = emptySet(),
            onToggleBookSelection = { },
            isInSelectionMode = false,
            onClearSelection = {},
            onBackFromSearch = {},
            booksApiState = BooksApiState.Success,
            onClearShelf = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun BookDetailsDialogPreview() {
    BookShelfTheme {
        BookDetailsDialog(
            bookItem = BookItem(
                id = "1",
                volumeInfo = VolumeInfo(
                    title = "The Lord of the Rings",
                    authors = listOf("J.R.R. Tolkien"),
                    description = "The Lord of the Rings is an epic high-fantasy novel written by English author and scholar J. R. R. Tolkien.",
                    imageLinks = ImageLinks(thumbnail = "http://books.google.com/books/content?id=zaRoPwAACAAJ&printsec=frontcover&img=1&zoom=1&source=gbs_api")
                )
            ),
            onDismiss = {},
            onAddBook = {}
        )
    }
}
