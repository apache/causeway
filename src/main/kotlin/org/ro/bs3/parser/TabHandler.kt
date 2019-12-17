package org.ro.bs3.parser

import org.ro.to.bs3.Bs3Object
import org.ro.to.bs3.Row
import org.ro.to.bs3.Tab

class TabHandler : BaseXmlHandler() {
    override fun doHandle() {
//        logEntry.aggregator = NavigationAggregator()
        //      update()
    }

    override fun parse(xmlStr: String): Bs3Object? {
        //TODO dive into sub elements, create objects and use in constructor
        val rows = mutableListOf<Row>()
        val name = ""
        val cssClass = ""
        return Tab(rows, name, cssClass)
    }
}
