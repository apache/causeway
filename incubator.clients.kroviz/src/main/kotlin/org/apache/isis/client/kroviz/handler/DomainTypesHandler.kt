package org.apache.isis.client.kroviz.handler

import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import org.apache.isis.client.kroviz.core.aggregator.DomainTypesAggregator
import org.apache.isis.client.kroviz.to.DomainTypes
import org.apache.isis.client.kroviz.to.TransferObject

class DomainTypesHandler : org.apache.isis.client.kroviz.handler.BaseHandler() {

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
