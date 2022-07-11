package com.inventory.book.repository

import com.example.demo.book.Book
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
interface BookRepository : ReactiveMongoRepository<Book , String> {

    fun findByTitle(title: String?) : Flux<Book>
    fun findByAuthors(authors: String?) : Flux<Book>

}