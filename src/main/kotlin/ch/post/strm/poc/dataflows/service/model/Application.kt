package ch.post.strm.poc.dataflows.service.model

data class Application(override val name: String) : Item {
    override val type = ItemType.APPLICATION
}