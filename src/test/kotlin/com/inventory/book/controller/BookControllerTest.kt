package com.inventory.book.controller

import com.example.demo.book.Book
import com.example.demo.book.GooleBook
import com.example.demo.book.Image
import com.example.demo.book.Item
import com.inventory.book.service.BookService
import io.kotlintest.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.returnResult
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@WebFluxTest(BookController::class)
@AutoConfigureWebTestClient
class BookControllerTest {

    @Autowired
    lateinit var client : WebTestClient

    @Autowired
    lateinit var bookService : BookService

    @Test
    fun `should return list of all the books and to verify that book service is internally called once`() {

        val book1 = Book("1","probability" , Image("https://image.png","https://image.png") , listOf("Michael"), "abcd" , 2,4)
        val book2 = Book("2","complex Algebra" , Image("https://image.png","https://image.png") , listOf("Robert") , "abcd" , 100 , 3)

        val expectedResult = listOf(
            mapOf("id" to "1",
                "title" to "probability",
                "imageLinks" to mapOf("smallThumbnail" to "https://image.png","thumbnail" to "https://image.png"),
                "authors" to listOf("Michael"),
                "description" to "abcd",
                "price" to 2,
                "quantity" to 4),
            mapOf("id" to "2",
                "title" to "complex Algebra",
                "imageLinks" to mapOf("smallThumbnail" to "https://image.png","thumbnail" to "https://image.png"),
                "authors" to listOf("Robert"),
                "description" to "abcd",
                "price" to 100,
                "quantity" to 3),
        )

        every {
            bookService.findAll()
        } returns Flux.just(book1,book2)

        val response = client.get()
            .uri("/api/v1/books/list")
            .accept(MediaType.APPLICATION_JSON)
            .exchange() //invoking the end point
            .expectStatus().is2xxSuccessful
            .returnResult<Any>()
            .responseBody

        response.blockFirst() shouldBe expectedResult[0]
        //response.blockLast() shouldBe expectedResult[1]

        verify(exactly = 1) {
            bookService.findAll()
        }
    }

//    @Test
//    fun `should create book when create api is being called`() {
//
//        val exepectedResponse = mapOf("id" to "1",
//            "title" to "probability",
//            "imageLinks" to mapOf("smallThumbnail" to "https://image.png","thumbnail" to "https://image.png"),
//            "authors" to listOf("Michael"),
//            "description" to "abcd",
//            "price" to 2,
//            "quantity" to 4)
//
//        val book = Book("1","probability" , Image("https://image.png","https://image.png") , listOf("Michael"), "abcd" , 2,4)
//
//        every {
//            bookService.createBooks(book)
//        } returns Mono.just(book)
//
//        val response = client.post()
//            .uri("/api/v1/books/create")
//            .bodyValue(book)
//            .exchange()
//            .expectStatus().is2xxSuccessful
//            .returnResult<Any>().responseBody
//
//        response.blockFirst() shouldBe exepectedResponse
//
//        verify(exactly = 1) {
//            bookService.createBooks(book)
//        }
//    }

    @Test
    fun `should return book on the basis of the book title`() {

        val exepectedResponse = mapOf("id" to "1",
            "title" to "probability",
            "imageLinks" to mapOf("smallThumbnail" to "https://image.png","thumbnail" to "https://image.png"),
            "authors" to listOf("Michael"),
            "description" to "abcd",
            "price" to 2,
            "quantity" to 4)

        val book = Book("1","probability" , Image("https://image.png","https://image.png") , listOf("Michael"), "abcd" , 2,4)

        every {
            bookService.findByTitle("probability")
        } returns Flux.just(book)

        val response = client.get()
            .uri("/api/v1/books/search/title/probability")
            .exchange()
            .expectStatus().is2xxSuccessful
            .returnResult<Any>()
            .responseBody.blockFirst()

        response shouldBe exepectedResponse

        verify(exactly = 1) {
            bookService.findByTitle("probability")
        }
    }

    @Test
    fun `should return book on the basis of the book author`() {

        val expectedResult = listOf(
            mapOf("id" to "1",
                "title" to "probability",
                "imageLinks" to mapOf("smallThumbnail" to "https://image.png","thumbnail" to "https://image.png"),
                "authors" to listOf("Michael"),
                "description" to "abcd",
                "price" to 2,
                "quantity" to 4)
        )

        val book1 = Book("1","probability" , Image("https://image.png","https://image.png") , listOf("Michael"), "abcd" , 2,4)

        every {
           bookService.findByAuthor("Michael")
        } returns Flux.just(
            book1
        )

        val response = client.get()
            .uri("/api/v1/books/search/author/Michael")
            .exchange()
            .expectStatus().is2xxSuccessful
            .returnResult<Any>()
            .responseBody

        response.blockFirst() shouldBe expectedResult[0]

        verify(exactly = 1) {
            bookService.findByAuthor("Michael")
        }
    }

    @Test
    fun `should be able to update the book present in the online book store`() {

        val expectedResult = listOf(
            mapOf("id" to "1",
                "title" to "probability",
                "imageLinks" to mapOf("smallThumbnail" to "https://image.png","thumbnail" to "https://image.png"),
                "authors" to listOf("Michael"),
                "description" to "abcd",
                "price" to 2,
                "quantity" to 4)
        )

        val book1 = Book("1","probability" , Image("https://image.png","https://image.png") , listOf("Michael"), "abcd" , 2,4)

        every {
            bookService.updateBookById("1",book1)
        } returns Mono.just(book1)

        val response = client.put()
            .uri("/api/v1/books/update/1")
            .bodyValue(book1)
            .exchange()
            .expectStatus().is2xxSuccessful

        verify(exactly = 1) {
            bookService.updateBookById("1",book1)
        }
    }

    @TestConfiguration
    class ControllerTestConfig {
        @Bean
        fun bookService() = mockk<BookService>()
    }
}
