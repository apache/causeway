package org.ro.handler

import org.ro.core.aggregator.UndefinedAggregator

class DefaultHandler : BaseHandler(), IResponseHandler {

    override fun canHandle(response: String): Boolean {
        return true
    }

    override fun doHandle() {
        logEntry.addAggregator(UndefinedAggregator())
        update()
    }

}
