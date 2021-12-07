package ch.post.strm.poc.dataflows.service

import ch.post.strm.poc.dataflows.service.model.Application
import ch.post.strm.poc.dataflows.service.model.SinkConnector
import ch.post.strm.poc.dataflows.service.model.SourceConnector
import ch.post.strm.poc.dataflows.service.model.Topic
import org.springframework.stereotype.Service

@Service
class KafkaConnectService {

    fun findSourceConnectors(): Map<String, SourceConnector> {
        return mapOf("padasa-jdbc-source" to SourceConnector("padasa-jdbc-source"))
    }

    fun findSinkConnectors(): Map<String, SinkConnector> {
        return mapOf(
            "padasa-state-jdbc-sink" to SinkConnector("padasa-state-jdbc-sink"),
            "padasa-event-jdbc-sink" to SinkConnector("padasa-event-jdbc-sink")
        )
    }

    fun findSinkConnectors(topic: Topic): Set<SinkConnector?> {
        return when (topic.name) {
            "logistics.Mailpiece-event" -> setOf(
                findSinkConnectors()["padasa-event-jdbc-sink"]
            )
            "logistics.Mailpiece-state" -> setOf(
                findSinkConnectors()["padasa-state-jdbc-sink"]
            )
            else -> setOf()
        }
    }

    fun findSourceConnectors(topic: Topic): Set<SourceConnector?> {
        return when (topic.name) {
            "logistics.Mailpiece-raw" -> setOf(
                findSourceConnectors()["padasa-jdbc-source"]
            )
            else -> setOf()
        }
    }

    fun findSinkConnectors(application: Application): Set<SinkConnector?> {
        return when (application.name) {
            "PADASA" -> setOf(
                findSinkConnectors()["padasa-event-jdbc-sink"],
                findSinkConnectors()["padasa-state-jdbc-sink"]
            )
            else -> setOf()
        }
    }

    fun findSourceConnectors(application: Application): Set<SourceConnector?> {
        return when (application.name) {
            "PADASA" -> setOf(
                findSourceConnectors()["padasa-jdbc-source"]
            )
            else -> setOf()
        }
    }
}