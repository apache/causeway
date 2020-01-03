package org.ro.core.aggregator

import org.ro.core.event.LogEntry
import org.ro.core.event.RoXmlHttpRequest
import org.ro.core.model.BaseDisplayable
import org.ro.to.Restful

/**
 * Rather a Dispatcher than an aggregator ...
 */
class RestfulAggregator() : BaseAggregator() {

    override lateinit var dsp: BaseDisplayable

    override fun update(logEntry: LogEntry) {
        val restful = logEntry.getTransferObject() as Restful
        restful.links.forEach {
            when  {
                it.rel.endsWith("/menuBars") -> {
                    console.log("[RestfulAggregator.update]")
                    console.log(it.rel)
                    RoXmlHttpRequest().invoke(it, XmlNavigationAggregator())
                }
                it.rel.equals("self") ->  {}
//FIXME                it.rel.endsWith("/services") -> {}
                else -> invoke(it)
            }
        }
    }

}
