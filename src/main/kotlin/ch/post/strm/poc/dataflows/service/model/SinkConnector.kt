package ch.post.strm.poc.dataflows.service.model

data class SinkConnector(override val name: String) : Item {
    override val type = ItemType.SINK_CONNECTOR
}