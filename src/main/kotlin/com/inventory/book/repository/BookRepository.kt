package com.inventory.book.repository

import com.example.demo.book.Book
import org.springframework.data.mongodb.repository.Query
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
interface BookRepository : ReactiveMongoRepository<Book , String> {

    @Query("{\$or :[{authors: {\$regex: ?0, \$options: i}},{title: {\$regex: ?0, \$options: i}}]}")
    fun findByTitleLikeOrAuthors(query : String?): Flux<Book>
}