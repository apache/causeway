package org.apache.causeway.client.kroviz.core.aggregator

import kotlinx.serialization.Serializable
import org.apache.causeway.client.kroviz.core.event.LogEntry

/**
 * For Test / debug only
 */
@Serializable
class DispatchInterceptor : BaseAggregator() {

    var logEntry: LogEntry? = null

    override fun update(logEntry: LogEntry, subType: String?) {
        this.logEntry = logEntry
        console.log("[DI.update]")
        console.log(logEntry)
    }
    
}
