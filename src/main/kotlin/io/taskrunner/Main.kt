package io.taskrunner

import com.rabbitmq.client.AMQP
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.DefaultConsumer
import com.rabbitmq.client.Envelope
import java.io.IOException
import kotlinx.serialization.json.Json
import io.taskmodels.TaskMessage
import kotlinx.serialization.encodeToString
import io.tasks.FiveSecondTask
import io.tasks.TenSecondTask
import io.tasks.FailingTask

class Worker {
    companion object {
        const val QUEUE_NAME = "tasks"
        const val MAX_TRIES = 3

        fun doWork(taskMessage: TaskMessage) {
            when(taskMessage.task.fileName) {
                "FiveSecondTask.kt" -> FiveSecondTask().run()
                "TenSecondTask.kt" -> TenSecondTask().run()
                "FailingTask.kt" -> FailingTask().run()
                else -> throw RuntimeException("Unknown task fileName ${taskMessage.task.fileName}")
            }
            print(taskMessage)
        }
    }
}

fun main() {
    val factory = ConnectionFactory()
    factory.host = "localhost"
    val connection = factory.newConnection()
    val channel = connection.createChannel()

    channel.queueDeclare(Worker.QUEUE_NAME, true, false, false, null)
    println(" [*] Waiting for messages. To exit press CTRL+C")

    channel.basicQos(1)

    val consumer = object : DefaultConsumer(channel) {
        @Throws(IOException::class)
        override fun handleDelivery(consumerTag: String, envelope: Envelope, properties: AMQP.BasicProperties, body: ByteArray) {
            val message = String(body, charset("UTF-8"))
            val taskMessage = Json.decodeFromString<TaskMessage>(message)

            println(" [x] Received '$message'")
            try {
                Worker.doWork(taskMessage)
            } catch (e: Exception) {
                taskMessage.timesRun += 1
                if (taskMessage.timesRun < Worker.MAX_TRIES) {
                    val rawMessage = Json.encodeToString(taskMessage).toByteArray()
                    channel.basicPublish("", Worker.QUEUE_NAME, null, rawMessage)
                    println(" [x] Re-publish '$taskMessage'")
                } else {
                    println(" [x] Final failing try for '$taskMessage'")
                }
                print(e.message)
            } finally {
                println(" [x] Done")
                channel.basicAck(envelope.deliveryTag, false)
            }
        }
    }
    channel.basicConsume(Worker.QUEUE_NAME, false, consumer)
}