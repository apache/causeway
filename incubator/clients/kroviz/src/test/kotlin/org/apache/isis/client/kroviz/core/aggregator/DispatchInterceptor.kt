package org.apache.isis.client.kroviz.core.aggregator

import org.apache.isis.client.kroviz.core.event.LogEntry

/**
 * For Test / debug only
 */
class DispatchInterceptor : BaseAggregator() {

    var logEntry: LogEntry? = null

    override fun update(logEntry: LogEntry, subType: String) {
        this.logEntry = logEntry
        console.log("[DI.update]")
        console.log(logEntry)
    }


}
