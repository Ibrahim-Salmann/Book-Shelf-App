package com.example.bookshelf.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

open class FirestoreRepository {

    private val db = FirebaseFirestore.getInstance()
    private val booksCollection = db.collection("books")

    open fun getBooksStream(): Flow<List<Book>> = callbackFlow {
        val subscription = booksCollection.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w("FirestoreRepository", "Listen failed.", e)
                close(e)
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val books = snapshot.toObjects(Book::class.java)
                trySend(books)
            } else {
                Log.d("FirestoreRepository", "Current data: null")
            }
        }
        awaitClose { subscription.remove() }
    }

    open suspend fun addBook(book: Book) {
        try {
            booksCollection.add(book).await()
        } catch (e: Exception) {
            Log.e("FirestoreRepository", "Error adding book", e)
        }
    }

    open suspend fun updateBook(book: Book) {
        if (book.id.isEmpty()) {
            Log.e("FirestoreRepository", "Book ID is empty, cannot update")
            return
        }
        try {
            booksCollection.document(book.id).set(book).await()
        } catch (e: Exception) {
            Log.e("FirestoreRepository", "Error updating book", e)
        }
    }

    open suspend fun deleteBook(book: Book) {
        if (book.id.isEmpty()) {
            Log.e("FirestoreRepository", "Book ID is empty, cannot delete")
            return
        }
        try {
            booksCollection.document(book.id).delete().await()
        } catch (e: Exception)
        {
            Log.e("FirestoreRepository", "Error deleting book", e)
        }
    }

    open suspend fun clearShelf() {
        try {
            val snapshot = booksCollection.get().await()
            val batch = db.batch()
            for (document in snapshot.documents) {
                batch.delete(document.reference)
            }
            batch.commit().await()
        } catch (e: Exception) {
            Log.e("FirestoreRepository", "Error clearing shelf", e)
        }
    }
}
