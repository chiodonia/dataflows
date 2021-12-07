package ch.post.strm.poc.dataflows.service

import ch.post.strm.poc.dataflows.service.model.*
import org.jgrapht.Graph
import org.jgrapht.GraphPath
import org.jgrapht.alg.shortestpath.AllDirectedPaths
import org.jgrapht.graph.AsSubgraph
import org.jgrapht.graph.DefaultDirectedGraph
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.graph.EdgeReversedGraph
import org.jgrapht.nio.Attribute
import org.jgrapht.nio.DefaultAttribute
import org.jgrapht.nio.dot.DOTExporter
import org.jgrapht.traverse.BreadthFirstIterator
import org.springframework.stereotype.Service
import java.io.StringWriter
import java.io.Writer
import java.util.*
import java.util.function.Consumer
import java.util.function.UnaryOperator
import java.util.stream.Collectors
import java.util.stream.Stream

@Service
class TopologyService(
    private val kafkaService: KafkaService,
    private val kafkaConnectService: KafkaConnectService,
    private val applicationService: ApplicationService
) {
    private val topology = topology(
        applicationService.find().values.stream().collect(Collectors.toSet()),
        kafkaService.find().values.stream().collect(Collectors.toSet()),
        kafkaConnectService.findSourceConnectors().values.stream().collect(Collectors.toSet()),
        kafkaConnectService.findSinkConnectors().values.stream().collect(Collectors.toSet())
    )

    fun topology(): String {
        return StringWriter().render(topology).toString()
    }

    fun subTopology(from: String, downstream: Boolean): String {
        return if (downstream) {
            StringWriter().render(
                subTopology(
                    topology,
                    traverseDownstream(topology, findByKey(topology, from))
                )
            ).toString()
        } else {
            StringWriter().render(
                subTopology(
                    topology,
                    traverse(topology, findByKey(topology, from))
                )
            ).toString()
        }
    }

    fun items(type: ItemType): Set<Item> {
        return select(topology.vertexSet(), type).stream().collect(Collectors.toSet())
    }

    fun subTopology(fromType: ItemType?, from: String?, downstream: Boolean): String? {
        return subTopology(key(fromType!!, from!!), downstream)
    }

    fun subTopology(from: String, to: String): String {
        return StringWriter().render(
            subTopology(
                topology,
                traverse(
                    topology,
                    findByKey(topology, from),
                    findByKey(topology, to)
                )
            )
        ).toString()
    }

    fun subTopology(fromType: ItemType?, from: String?, toType: ItemType?, to: String?): String? {
        return subTopology(key(fromType!!, from!!), key(toType!!, to!!))
    }

    private fun topology(vararg items: Collection<Item>): Graph<Item, DefaultEdge> {
        val topology: Graph<Item, DefaultEdge> = DefaultDirectedGraph(
            DefaultEdge::class.java
        )
        Arrays.stream(items).forEach { namedItems: Collection<Item> ->
            namedItems.forEach(
                Consumer { v: Item ->
                    topology.addVertex(
                        v
                    )
                })
        }
        select(topology.vertexSet(), ItemType.TOPIC).stream().map { topic: Item -> topic as Topic }
            .forEach { topic: Topic ->
                kafkaService.findProducers(topic)
                    .forEach {
                        if (topology.containsVertex(it) && topology.containsVertex(topic)) {
                            topology.addEdge(it, topic)
                        }
                    }
                kafkaConnectService.findSourceConnectors(topic)
                    .forEach {
                        if (topology.containsVertex(it) && topology.containsVertex(topic)) {
                            topology.addEdge(topic, it)
                        }
                    }
                kafkaService.findConsumers(topic)
                    .forEach {
                        if (topology.containsVertex(it) && topology.containsVertex(topic)) {
                            topology.addEdge(topic, it)
                        }
                    }
                kafkaConnectService.findSinkConnectors(topic)
                    .forEach {
                        if (topology.containsVertex(it) && topology.containsVertex(topic)) {
                            topology.addEdge(it, topic)
                        }
                    }
            }
        select(topology.vertexSet(), ItemType.APPLICATION).stream()
            .map { application: Item -> application as Application }
            .forEach { application: Application ->
                kafkaConnectService.findSinkConnectors(application).forEach {
                    if (topology.containsVertex(application) && topology.containsVertex(it)) {
                        topology.addEdge(application, it)
                    }
                }
            }
        select(topology.vertexSet(), ItemType.APPLICATION).stream()
            .map { application: Item -> application as Application }
            .forEach { application: Application ->
                kafkaConnectService.findSourceConnectors(application).forEach {
                    if (topology.containsVertex(application) && topology.containsVertex(it)) {
                        topology.addEdge(it, application)
                    }
                }
            }

        return topology
    }


}

private fun subTopology(topology: Graph<Item, DefaultEdge>, items: Set<Item?>): Graph<Item, DefaultEdge> {
    return AsSubgraph(topology, items)
}

private fun findByKey(topology: Graph<Item, DefaultEdge>, key: String): Item {
    return topology.vertexSet().stream().filter { item: Item -> item.key == key }
        .findFirst().orElseThrow()
}

private fun traverse(topology: Graph<Item, DefaultEdge>, from: Item, to: Item): Set<Item> {
    val paths = AllDirectedPaths(topology)
    val allPaths = paths.getAllPaths(from, to, false, Int.MAX_VALUE)
    return allPaths.stream().flatMap { path: GraphPath<Item, DefaultEdge> ->
        path.vertexList.stream()
    }.collect(Collectors.toSet())
}

private fun traverse(topology: Graph<Item, DefaultEdge>, from: Item): Set<Item> {
    return Stream.iterate(
        BreadthFirstIterator(topology, from),
        { obj: BreadthFirstIterator<Item, DefaultEdge> -> obj.hasNext() }, UnaryOperator.identity()
    ).map { obj: BreadthFirstIterator<Item, DefaultEdge> -> obj.next() }
        .collect(Collectors.toSet())
}

private fun traverseDownstream(topology: Graph<Item, DefaultEdge>, from: Item): Set<Item> {
    return traverse(EdgeReversedGraph(topology), from)
}


private fun select(items: Set<Item>, type: ItemType): Set<Item> {
    return items.stream().filter { v: Item -> v.type == type }.collect(Collectors.toSet())
}

private fun shape(i: Item): String {
    return when (i.type) {
        ItemType.TOPIC -> "octagon"
        ItemType.SINK_CONNECTOR,
        ItemType.SOURCE_CONNECTOR -> "oval"
        else -> "box"
    }
}

private fun Writer.render(topology: Graph<Item, DefaultEdge>): Writer {
    val exporter = DOTExporter<Item, DefaultEdge>(fun(i: Item): String { return i.key })
    exporter.setGraphIdProvider(fun(): String { return "Dataflows" })
    exporter.setVertexAttributeProvider(fun(i: Item): Map<String, Attribute> {
        return mapOf(
            "label" to DefaultAttribute.createAttribute(i.name),
            "shape" to DefaultAttribute.createAttribute(shape(i))
        )
    })
    exporter.exportGraph(topology, this)
    return this
}

private fun render(topology: Graph<Item, DefaultEdge>): Writer {
    return StringWriter().render(topology)
}

