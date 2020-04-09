package org.apache.isis.client.kroviz.core.aggregator

import org.apache.isis.client.kroviz.core.event.EventStore
import org.apache.isis.client.kroviz.core.event.LogEntry
import org.apache.isis.client.kroviz.core.event.ResourceSpecification
import org.apache.isis.client.kroviz.to.HttpError
import org.apache.isis.client.kroviz.ui.ErrorDialog

class ErrorDispatcher : BaseAggregator() {

    override fun update(logEntry: LogEntry, subType: String) {
        val error = logEntry.getTransferObject() as HttpError
        val url = logEntry.url
        val message = error.message
        val reSpec = ResourceSpecification(url)
        EventStore.fault(reSpec, message)
        ErrorDialog(logEntry).open()
    }

}
