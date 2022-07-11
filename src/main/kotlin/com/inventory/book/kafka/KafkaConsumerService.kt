package com.inventory.book.kafka

import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service

@Service
class KafkaConsumerService {

    companion object{
        var audit: ArrayList<String> = ArrayList()
    }

    @KafkaListener(topics = ["audit"], groupId = "group-id")
    fun listen(message: String) {
        audit.add(message)
    }
}