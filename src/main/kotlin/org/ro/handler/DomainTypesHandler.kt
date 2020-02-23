package org.ro.handler

import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import org.ro.core.aggregator.DomainTypesAggregator
import org.ro.to.DomainTypes
import org.ro.to.TransferObject

class DomainTypesHandler : BaseHandler() {

    @UnstableDefault
    override fun parse(response: String): TransferObject? {
        return Json.parse(DomainTypes.serializer(), response)
    }

    override fun doHandle() {
        val url = logEntry.url
        logEntry.addAggregator(DomainTypesAggregator(url))
        update()
    }

}
