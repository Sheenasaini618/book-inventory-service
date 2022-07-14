package com.inventory.book.service

import com.example.demo.book.Book
import com.example.demo.book.Image
import com.inventory.book.kafka.KafkaProducerService
import com.inventory.book.repository.BookRepository
import io.kotlintest.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier


class BookServiceTest{

    // mocking the repository layer response

    val book1 = Book("1","probability" , Image("https://image.png","https://image.png") , listOf("Michael"), "abcd" , 2,4)
    val book2 = Book("2","complex Algebra" , Image("https://image.png","https://image.png") , listOf("Robert") , "abcd" , 100 , 3)


    private val bookRepository = mockk<BookRepository>(){

        every {
            findAll()
        } returns Flux.just(book1,book2)

        every {
            findByTitleLikeOrAuthors("probability")
        } returns Flux.just(book1)

        every {
            findByTitleLikeOrAuthors("Michael")
        } returns Flux.just(book1)

    }

    private val kafkaProducerService = mockk<KafkaProducerService>()

    private val bookService = BookService(bookRepository,kafkaProducerService)


    @Test
    fun `should return books when find all method is called`() {

        val firstBook =  bookService.findAll().blockFirst()
        val secondBook = bookService.findAll().blockLast()

        firstBook shouldBe book1
        secondBook shouldBe book2
    }

    @Test
    fun `should expect on complete call post all the books are retrieved`() {

        //StepVerifier takes care of subscribing

        StepVerifier.create( bookService.findAll()).expectSubscription().expectNext(book1).expectNext(book2).verifyComplete()
        StepVerifier.create( bookService.findAll()).expectNextCount(2).verifyComplete()
    }

    @Test
    fun `should find the list of books on the basis of the title`() {

        val result = bookService.findBySearch("probability").blockFirst()

        result shouldBe book1
    }

    @Test
    fun `should find the list of books on the basis of the author`() {

        val bookResult = bookService.findBySearch("Michael").blockFirst()

        bookResult shouldBe book1
    }
}