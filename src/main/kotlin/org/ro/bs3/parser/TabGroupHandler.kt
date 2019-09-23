package org.ro.bs3.parser

import org.ro.bs3.parser.BaseXmlHandler
import org.ro.bs3.to.Bs3Object
import org.ro.bs3.to.Tab
import org.ro.bs3.to.TabGroup

class TabGroupHandler : BaseXmlHandler() {
    override fun doHandle() {
//        logEntry.aggregator = NavigationAggregator()
        //      update()
    }

    override fun parse(xmlStr: String): Bs3Object? {
        //TODO dive into sub elements, create objects and use in constructor
        val tab: List<Tab>? = ArrayList<Tab>()
        var metadataError = ""
        var isCollapseIfOne = false
        var isUnreferencedCollections = false
        val cssClass = ""
        return TabGroup(tab, metadataError, isCollapseIfOne, isUnreferencedCollections, cssClass)
    }
}
