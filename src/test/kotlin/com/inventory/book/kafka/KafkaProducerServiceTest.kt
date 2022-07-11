package com.inventory.book.kafka

import com.example.demo.book.Book
import com.example.demo.book.Image
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.junit.After
import org.junit.Before
import org.junit.ClassRule
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.runner.RunWith
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.listener.ContainerProperties
import org.springframework.kafka.listener.KafkaMessageListenerContainer
import org.springframework.kafka.listener.MessageListener
import org.springframework.kafka.test.rule.EmbeddedKafkaRule
import org.springframework.kafka.test.utils.ContainerTestUtils
import org.springframework.kafka.test.utils.KafkaTestUtils
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit4.SpringRunner
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

@RunWith(SpringRunner::class)
@DirtiesContext
@SpringBootTest
class KafkaProducerServiceTest {

    companion object {
        val logger = LoggerFactory.getLogger(KafkaProducerServiceTest::class.java)
        val topic="audit"

        @ClassRule
        @JvmField
        val embeddedKafkaRule = EmbeddedKafkaRule(1,true, topic)
    }

    @Autowired
    lateinit var kafkaProducerService: KafkaProducerService

    @Autowired
    lateinit var kafkaConsumerService : KafkaConsumerService
    lateinit var container : KafkaMessageListenerContainer<String, String>
    lateinit var records : BlockingQueue<ConsumerRecord<String, String>>


    @Before
    fun setUp() {
        val consumerProperties = KafkaTestUtils.consumerProps("sender",
            "false", embeddedKafkaRule.embeddedKafka)

        val consumerFactory = DefaultKafkaConsumerFactory<String, String>(consumerProperties)
        val containerProperties = ContainerProperties(topic)

        container = KafkaMessageListenerContainer(consumerFactory,containerProperties)
        records = LinkedBlockingQueue()

        container.setupMessageListener(MessageListener<String, String> { record ->
            logger.debug("test-listener received message='{}'", record.toString())
            records.add(record)
        })

        container.start()

        ContainerTestUtils.waitForAssignment(container,
            embeddedKafkaRule.embeddedKafka.partitionsPerTopic)
    }

    @After
    fun tearDown(){
        container.stop()
    }


    @Test
    fun MessageWhenBookIsAdded() {

        val book = Book("1","probability" , Image("https://image.png","https://image.png") , listOf("Michael"), "abcd" , 2,4)

        kafkaProducerService.messageWhenBookIsAdded(book)

        Thread.sleep(10000)
        val audit= KafkaConsumerService.audit.last()
        Assertions.assertTrue(audit.contains("4 number of book of probability having price 2 has been added"))
    }

    @Test
    fun MessageWhenBookIsUpdated() {

        val book = Book("1","probability" , Image("https://image.png","https://image.png") , listOf("Michael"), "abcd" , 2,4)

        kafkaProducerService.messageWhenBookIsUpdated(book)

        Thread.sleep(10000)
        val audit= KafkaConsumerService.audit.last()
        Assertions.assertTrue(audit.contains("4 number of book of probability having price 2 has been updated"))
    }

    @Test
    fun MessageWhenBookIsDeleted() {

        val book = Book("1","probability" , Image("https://image.png","https://image.png") , listOf("Michael"), "abcd" , 2,4)

        kafkaProducerService.messageWhenBookIsDeleted(book)

        Thread.sleep(10000)
        val audit= KafkaConsumerService.audit.last()
        Assertions.assertTrue(audit.contains("4 number of book of probability having price 2 has been deleted"))
    }
}