package ch.post.strm.poc.dataflows.service.model

data class Topic(override val name: String) : Item {
    override val type = ItemType.TOPIC
}