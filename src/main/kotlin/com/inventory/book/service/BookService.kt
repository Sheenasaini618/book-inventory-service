
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

@Service
class BookService(@Autowired val bookRepository : BookRepository , @Autowired val kafkaProducerService : KafkaProducerService) {

    fun findAll() : Flux<Book> =
        bookRepository.findAll()

    fun createBooks(book: Book){
       bookRepository.save(book).subscribe(kafkaProducerService::messageWhenBookIsAdded)
    }

    fun deleteBooksById(id : String): Mono<Void>{
        bookRepository.findById(id).subscribe(kafkaProducerService::messageWhenBookIsDeleted)
        return bookRepository.deleteById(id)
    }

    fun updateBookById(id: String, book: Book) : Mono<Book> {

       var bookResult = bookRepository.findById(id)
           .flatMap {
               it.price = book.price
               it.quantity = book.quantity
               bookRepository.save(it)
           }
        bookResult.subscribe(kafkaProducerService::messageWhenBookIsUpdated)
        return bookResult
    }

    fun findByTitle(title : String) : Flux<Book>{
        return bookRepository.findByTitle(title)
    }

    fun findByAuthor(author : String) : Flux<Book>{
        return bookRepository.findByAuthors(author)
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