package ch.post.strm.poc.dataflows.service.model

data class SourceConnector(override val name: String) : Item {
    override val type = ItemType.SOURCE_CONNECTOR
}