
package com.inventory.book.service

import com.example.demo.book.Book
import com.inventory.book.repository.BookRepository
import com.example.demo.book.GooleBook
import com.inventory.book.kafka.KafkaProducerService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Service
class BookService(@Autowired val bookRepository : BookRepository , @Autowired val kafkaProducerService : KafkaProducerService) {

    fun findAll() : Flux<Book> =
        bookRepository.findAll()

    fun createBooks(book: Book): Mono<Book>{
       return bookRepository.save(book).doOnSuccess { it->kafkaProducerService.messageOnBasisAction(it, Action.ADDED.name) }
    }

    fun deleteBooksById(id : String) : Mono<Book>{
         return bookRepository.findById(id).flatMap { book ->
             bookRepository.deleteById(id)
             .doOnSuccess{kafkaProducerService.messageOnBasisAction(book,Action.DELETED.name)}.subscribe()
             book.toMono()
         }
    }

    fun updateBookById(id: String, book: Book) : Mono<Book> {

       return bookRepository.findById(id)
           .flatMap {
               it.price = book.price
               it.quantity = book.quantity
               bookRepository.save(it)
               .doOnSuccess{kafkaProducerService.messageOnBasisAction(it,Action.UPDATED.name)}
           }
    }

    fun findBySearch(query : String) : Flux<Book>{
        return bookRepository.findByTitleLikeOrAuthors(query)
    }

    fun remove() : Mono<Void> {
         return bookRepository.deleteAll()
    }

    fun getBookfromApi(string : String): Flux<GooleBook> {

        return WebClient.create(buildUrl(string))
            .get()
            .retrieve()
            .bodyToFlux(GooleBook::class.java)
    }

    fun buildUrl(string : String): String {
        return UriComponentsBuilder.fromHttpUrl("https://www.googleapis.com/books/v1/volumes")
            .replaceQueryParam("q",string)
            .replaceQueryParam("key","AIzaSyBEz4HtafYUQVctBqG2PbgI7GzXwr4e1Yc")
            .encode().toUriString()
    }
}

enum class Action{
    ADDED,
    UPDATED,
    DELETED
}