package com.inventory.book.kafka

import com.example.demo.book.Book
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class KafkaProducerService (private val kafkaTemplate : KafkaTemplate<String, String>){

    fun messageOnBasisAction(book : Book, action : String){
        when(action){
            "ADDED"-> kafkaTemplate.send("audit",message(book,action))
            "UPDATED" -> kafkaTemplate.send("audit",message(book,action))
            "DELETED" -> kafkaTemplate.send("audit",message(book,action))
        }
    }

    fun message(book : Book, action: String): String{
        return "${book.quantity} number of book of ${book.title} having price ${book.price} has been ${action.lowercase()}"
    }

}