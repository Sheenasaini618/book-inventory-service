package com.example.demo.book

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class KafkaProducerService {

    @Autowired
    private val kafkaTemplate : KafkaTemplate<String, String>? = null

    fun messageWhenBookIsAdded(book: Book){
        kafkaTemplate?.send("audit",message(book,"added"))
    }

    fun messageWhenBookIsUpdated(book : Book){
        kafkaTemplate?.send("audit",message(book,"updated"))
    }

    fun messageWhenBookIsDeleted(book : Book){
        kafkaTemplate?.send("audit",message(book,"deleted"))
    }

    fun message(book : Book, action: String): String{
        return "${book.quantity} number of book of ${book.title} having price ${book.price} has been ${action}"
    }

}