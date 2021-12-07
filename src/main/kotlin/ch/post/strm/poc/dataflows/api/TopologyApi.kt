package ch.post.strm.poc.dataflows.api

import ch.post.strm.poc.dataflows.service.TopologyService
import ch.post.strm.poc.dataflows.service.model.Item
import ch.post.strm.poc.dataflows.service.model.ItemType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class TopologyApi(val service: TopologyService) {

    @GetMapping("/topology")
    fun topology(): String {
        return service.topology()
    }

    @GetMapping("/topology", params = ["from", "downstream"])
    fun subTopology(
        @RequestParam("downstream") downstream: Boolean,
        @RequestParam("from") from: String
    ): String {
        return service.subTopology(from, downstream)
    }

    @GetMapping("/topology", params = ["from", "to"])
    fun subTopology(
        @RequestParam("from") from: String,
        @RequestParam("to") to: String
    ): String {
        return service.subTopology(from, to)
    }

    @GetMapping("/items")
    fun items(): Set<Item> {
        return service.items(ItemType.APPLICATION).plus(service.items(ItemType.TOPIC))
    }
}