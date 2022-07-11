package com.inventory.book.integration

import com.example.demo.book.Book
import com.example.demo.book.GooleBook
import com.example.demo.book.Image
import com.inventory.book.repository.BookRepository
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBodyList
import org.springframework.test.web.reactive.server.returnResult

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@ExtendWith(SpringExtension::class)

class IntegrationTests {

    @Autowired
    lateinit var client : WebTestClient

    @Autowired
    lateinit var bookRepository : BookRepository


    @BeforeEach
    fun setUp() {
        bookRepository.deleteAll().subscribe()
    }

    @Test
    fun `should get the lists of book from database`() {

        val exepectedResponse = mapOf("id" to "1",
            "title" to "probability",
            "imageLinks" to mapOf("smallThumbnail" to "https://image.png","thumbnail" to "https://image.png"),
            "authors" to listOf("Michael"),
            "description" to "abcd",
            "price" to 2,
            "quantity" to 4)

        val book = Book("1","probability" , Image("https://image.png","https://image.png") , listOf("Michael"), "abcd" , 2,4)

        bookRepository.save(book).block()

        val response = client.get()
            .uri("/api/v1/books/list")
            .exchange()
            .expectStatus().is2xxSuccessful
            .returnResult<Any>()
            .responseBody.blockFirst()

        response shouldBe exepectedResponse
    }

    @Test
    fun `should get the book from the database on the basis of the title`() {

        val exepectedResponse = mapOf("id" to "1",
            "title" to "probability",
            "imageLinks" to mapOf("smallThumbnail" to "https://image.png","thumbnail" to "https://image.png"),
            "authors" to listOf("Michael"),
            "description" to "abcd",
            "price" to 2,
            "quantity" to 4)

        val book = Book("1","probability" , Image("https://image.png","https://image.png") , listOf("Michael"), "abcd" , 2,4)

        bookRepository.save(book).block()

        val response = client.get()
            .uri("/api/v1/books/search/title/${book.title}")
            .exchange()
            .expectStatus().is2xxSuccessful
            .returnResult<Any>()
            .responseBody.blockFirst()

        response shouldBe exepectedResponse
    }

    @Test
    fun `should get the book from the database on the basis of the author`() {

        val exepectedResponse = mapOf("id" to "1",
            "title" to "probability",
            "imageLinks" to mapOf("smallThumbnail" to "https://image.png","thumbnail" to "https://image.png"),
            "authors" to listOf("Michael"),
            "description" to "abcd",
            "price" to 2,
            "quantity" to 4)

        val book = Book("1","probability" , Image("https://image.png","https://image.png") , listOf("Michael"), "abcd" , 2,4)

        bookRepository.save(book).block()

        val response = client.get()
            .uri("/api/v1/books/search/author/${book.authors?.get(0)}")
            .exchange()
            .expectStatus().is2xxSuccessful
            .returnResult<Any>()
            .responseBody.blockFirst()

        response shouldBe exepectedResponse
    }

    @Test
    fun `should create book in the db and receive audit of addition of book`() {


        val exepectedResponse = mapOf("id" to "1",
            "title" to "probability",
            "imageLinks" to mapOf("smallThumbnail" to "https://image.png","thumbnail" to "https://image.png"),
            "authors" to listOf("Michael"),
            "description" to "abcd",
            "price" to 2,
            "quantity" to 4)

        val book = Book("1","probability" , Image("https://image.png","https://image.png") , listOf("Michael"), "abcd" , 2,4)

        client.post()
            .uri("/api/v1/books/create")
            .bodyValue(book)
            .exchange()
            .expectStatus().is2xxSuccessful


         client.get()
            .uri("/api/v1/books/audit")
            .exchange()
            .expectStatus().isOk
            .expectBodyList(String::class.java)
            .consumeWith<WebTestClient.ListBodySpec<String>>{
                list -> Assertions.assertEquals(1,list.responseBody?.size)
            }

    }

    @Test
    fun `should be able to update the book present in the online book store and receive audit of updation of book`() {

        val exepectedResponse = mapOf("id" to "1",
            "title" to "probability",
            "imageLinks" to mapOf("smallThumbnail" to "https://image.png","thumbnail" to "https://image.png"),
            "authors" to listOf("Michael"),
            "description" to "abcd",
            "price" to 2,
            "quantity" to 6)

        val book1 = Book("1","probability" , Image("https://image.png","https://image.png") , listOf("Michael"), "abcd" , 2,4)
        val book2 = Book("1","probability" , Image("https://image.png","https://image.png") , listOf("Michael"), "abcd" , 2,6)


        bookRepository.save(book1).block()

        val response = client.put()
            .uri("/api/v1/books/update/${book1.id}")
            .bodyValue(book2)
            .exchange()
            .expectStatus().is2xxSuccessful
            .returnResult<Any>()
            .responseBody.blockFirst()

        response shouldBe exepectedResponse

        client.get()
            .uri("/api/v1/books/audit")
            .exchange()
            .expectStatus().isOk
            .expectBodyList(String::class.java)
            .consumeWith<WebTestClient.ListBodySpec<String>>{
                    list -> Assertions.assertEquals(1,list.responseBody?.size)
            }
    }

    @Test
    fun `should return 200 successful when book from the google book api is being searched`(){

          client.get()
            .uri("/api/v1/books/google/list/probability")
            .exchange()
            .expectStatus().is2xxSuccessful
              .expectBodyList(GooleBook::class.java)
              .consumeWith<WebTestClient.ListBodySpec<GooleBook>> {
                  list -> Assertions.assertEquals(1,list.responseBody?.size)
              }
    }
}
