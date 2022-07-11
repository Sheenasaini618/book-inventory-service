package com.inventory.book.repository

import com.example.demo.book.Book
import com.example.demo.book.Image
import io.kotlintest.shouldBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.dao.DuplicateKeyException
import org.springframework.test.context.ActiveProfiles

@DataMongoTest
@ActiveProfiles("test")
class BookRepositoryTest {

    @Autowired
    lateinit var bookRepository : BookRepository

    @AfterEach
    fun tearDown() {
        bookRepository.deleteAll().block()
    }

    @Test
    fun `should find book for given author `() {

        val book = Book("1","probability" , Image("https://image.png","https://image.png") , listOf("Michael"), "abcd" , 2,4)

        bookRepository.save(book).block()

        val actualSaveApplication = bookRepository.findByAuthors(
            "Michael"
        ).blockFirst()

        actualSaveApplication shouldBe book
    }

    @Test
    fun `should find book for given title`() {

        val book = Book("1","probability" , Image("https://image.png","https://image.png") , listOf("Michael"), "abcd" , 2,4)

        bookRepository.save(book).block()

        val actualSaveApplication = bookRepository.findByTitle(
            "probability"
        ).blockFirst()

        actualSaveApplication shouldBe book
    }

    @Test
    fun `should not store duplicate application with same title`() {

        val book1 = Book("1","probability" , Image("https://image.png","https://image.png") , listOf("Michael"), "abcd" , 2,4)
        val book2 = Book("1","complex Algebra" , Image("https://image.png","https://image.png") , listOf("Robert") , "abcd" , 100 , 3)

        bookRepository.insert(book1).block()

        assertThrows<DuplicateKeyException> { bookRepository.insert(book2).block() }

    }

}