package com.inventory.book.controller

import com.example.demo.book.Book
import com.inventory.book.service.BookService
import com.example.demo.book.GooleBook
import com.inventory.book.kafka.KafkaConsumerService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

//We wrap a single Book.kt resource in a Mono because we return at most one book.
//For the collection resource, we use a Flux of type Book.kt since that's the publisher for 0..n elements.

@RestController
@RequestMapping("/api/v1")
@CrossOrigin
class BookController(@Autowired val bookService : BookService) {

    // endpoint in our controller that publishes multiple Book.kt resource

    @GetMapping("/books/list")
    fun getAllBooks(): Flux<Book> {
        return bookService.findAll()
    }

    @GetMapping("books/search/title/{title}")
    fun getBookBasisTitle(@PathVariable title : String): Flux<Book>{
        return bookService.findByTitle(title)
    }

    @GetMapping("books/search/author/{author}")
    fun getBookBasisAuthor(@PathVariable author : String): Flux<Book>{
        return bookService.findByAuthor(author)
    }

    @PostMapping("/books/create")
    fun createBook(@RequestBody book: Book) : Mono<Book> {
        return bookService.createBooks(book)
    }

    @PutMapping("/books/update/{id}")
    fun updateBookById(@PathVariable id : String, @RequestBody book : Book): Mono<Book>{
        return bookService.updateBookById(id, book)
    }

    @DeleteMapping("/books/delete/{id}")
    fun deleteBookById(@PathVariable id : String): Mono<Book> {
         return bookService.deleteBooksById(id)
    }

    @DeleteMapping("books/deleteAll")
    fun deleteAllBooks(): Mono<Void> {
        return bookService.remove()
    }

    @GetMapping("/books/google/list/{title}")
    fun getAllBooksUsingGoogleBookApi(@PathVariable title : String): Flux<GooleBook> {
        return bookService.getBookfromApi(title)
    }

    @GetMapping("/books/audit")
    fun retrievingAudit() = KafkaConsumerService.audit

}