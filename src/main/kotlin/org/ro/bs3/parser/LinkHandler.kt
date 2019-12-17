package org.ro.bs3.parser

import org.ro.bs3.parser.BaseXmlHandler
import org.ro.to.bs3.Bs3Object
import org.ro.to.bs3.Link

class LinkHandler : BaseXmlHandler() {
    override fun doHandle() {
//        logEntry.aggregator = NavigationAggregator()
        //      update()
    }

    override fun parse(xmlStr: String): Bs3Object? {
        //TODO dive into sub elements, create objects and use in constructor
        val rel = ""
        val method = ""
        val href = ""
        val type = ""
        return Link(
                rel,
                method,
                href,
                type)
    }
}
