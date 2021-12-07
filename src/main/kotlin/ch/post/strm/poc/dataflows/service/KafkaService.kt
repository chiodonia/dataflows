package ch.post.strm.poc.dataflows.service

import ch.post.strm.poc.dataflows.service.model.Application
import ch.post.strm.poc.dataflows.service.model.Topic
import org.springframework.stereotype.Service

@Service
class KafkaService(val applicationService: ApplicationService) {

    fun findProducers(topic: Topic): Set<Application?> {
        return when (topic.name) {
            "logistics.Mailpiece-raw" -> setOf(
                applicationService.find()["FISA"]
            )
            else -> setOf()
        }
    }

    fun findConsumers(topic: Topic): Set<Application?> {
        return when (topic.name) {
            "logistics.Mailpiece-event" -> setOf(
                applicationService.find()["TT"], applicationService.find()["EKP"]
            )
            "logistics.Mailpiece-state" -> setOf(
                applicationService.find()["TT"]
            )
            else -> setOf()
        }
    }

    fun find(): Map<String, Topic> {
        return mapOf(
            "logistics.Mailpiece-raw" to Topic("logistics.Mailpiece-raw"),
            "logistics.Mailpiece-event" to Topic("logistics.Mailpiece-event"),
            "logistics.Mailpiece-state" to Topic("logistics.Mailpiece-state")

        )
    }
}