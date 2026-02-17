package com.example.bookshelf.network

import kotlinx.serialization.Serializable
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface BookApiService {
    @GET("volumes")
    suspend fun searchBooks(
        @Query("q") query: String,
        @Query("key") apiKey: String,
        @Header("X-User-Agent") userAgent: String
    ): BookSearchResponse

    /**
     * Fetches the details for a single book volume.
     *
     * @param bookId The unique ID of the book volume.
     * @return A [BookItem] containing detailed information about the book.
     */
    @GET("volumes/{id}")
    suspend fun getBookDetails(
        @Path("id") bookId: String
    ): BookItem

    /**
     * Example of creating a new book using @Body.
     * The BookItem object will be serialized to JSON and sent in the request body.
     */
    @POST("volumes")
    suspend fun createBook(@Body book: BookItem): BookItem

    /**
     * Example of submitting a review using @FormUrlEncoded and @Field.
     */
    @FormUrlEncoded
    @POST("reviews")
    suspend fun submitReview(
        @Field("book_id") bookId: String,
        @Field("rating") rating: Int,
        @Field("comment") comment: String
    )

    /**
     * Example of uploading a book cover image using @Multipart and @Part.
     */
    @Multipart
    @POST("volumes/{id}/cover")
    suspend fun uploadBookCover(
        @Path("id") bookId: String,
        @Part coverImage: MultipartBody.Part,
        @Part("description") description: String
    )
}

@Serializable
data class BookSearchResponse(
    val items: List<BookItem>
)

@Serializable
data class BookItem(
    val id: String,
    val volumeInfo: VolumeInfo
)

@Serializable
data class VolumeInfo(
    val title: String,
    val authors: List<String>? = null,
    val publisher: String? = null,
    val publishedDate: String? = null,
    val description: String? = null,
    val pageCount: Int? = null,
    val categories: List<String>? = null,
    val imageLinks: ImageLinks? = null,
    val averageRating: Double? = null,
    val ratingsCount: Int? = null
)

@Serializable
data class ImageLinks(
    val thumbnail: String? = null,
    val small: String? = null,
    val medium: String? = null,
    val large: String? = null
)
