package com.example.bookshelf.data

import com.google.firebase.firestore.DocumentId
import kotlinx.serialization.Serializable

@Serializable
data class Book(
    @DocumentId var id: String = "",
    var name: String = "",
    var author: String = "",
    var category: String = "",
    var genre: String = "",
    var coverUri: String? = null,
    var status: String? = null,
    var rating: Int = 0
)
