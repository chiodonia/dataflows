package ch.post.strm.poc.dataflows.service.model

interface Item {

    val name: String
    val type: ItemType
    val key: String
        get() = key(type, name)

}

fun key(type: ItemType, name: String): String {
    return type.name + "_" + name.replace('-', '_').replace('.', '_')
}

enum class ItemType {
    APPLICATION, TOPIC, SINK_CONNECTOR, SOURCE_CONNECTOR
}
