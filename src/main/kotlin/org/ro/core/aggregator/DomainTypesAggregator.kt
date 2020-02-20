package org.ro.core.aggregator

import org.ro.to.DomainTypes
import org.ro.to.Link

class DomainTypesAggregator(val url: String, val model: DomainTypes) : BaseAggregator() {

    lateinit var diagramCode: String
    var modelLinks = mutableListOf<Link>()

    init {
        model.values.forEach { link ->
            if (link.href.contains("/demo")) {
                modelLinks.add(link)
            }
        }
        console.log("[DomainTypesManager.init]")
        console.log(modelLinks)
    }
}
