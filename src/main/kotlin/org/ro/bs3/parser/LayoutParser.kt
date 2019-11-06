package org.ro.bs3.parser

import org.ro.bs3.parser.BaseXmlHandler
import org.ro.bs3.parser.RowHandler
import org.ro.bs3.to.Bs3Object

class LayoutParser : BaseXmlHandler() {
    override fun doHandle() {
//        logEntry.aggregator = NavigationAggregator()
        //      update()
    }

    override fun parse(xmlStr: String): Bs3Object? {
        return RowHandler().parse(xmlStr)
    }
}
