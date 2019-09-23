package org.ro.bs3.parser

import org.ro.bs3.parser.BaseXmlHandler
import org.ro.bs3.to.*

class ColHandler : BaseXmlHandler() {
    override fun doHandle() {
//        logEntry.aggregator = NavigationAggregator()
        //      update()
    }

    override fun parse(xmlStr: String): Bs3Object? {
        //TODO dive into sub elements, create objects and use in constructor

        var sizeSpan: List<SizeSpan>? = ArrayList<SizeSpan>()
        var domainObject = DomainObjectHandler().parse(xmlStr) as DomainObject
        var action: List<Action>? = ArrayList()
        var row: List<Row>? = ArrayList<Row>()
        var tabGroup: List<TabGroup>? = ArrayList<TabGroup>()
        var fieldSet: List<FieldSet>? = ArrayList<FieldSet>()
        var collection: List<Collection>? = ArrayList<Collection>()
        var metadataError = ""
        var id = ""
        var span: Int = 0
        var isUnreferencedActions = false
        var isUnreferencedCollections = false

        val size = Size.LG
        val cssClass = ""
        return Col(
                sizeSpan,
                domainObject,
                action,
                row,
                tabGroup,
                fieldSet,
                collection,
                metadataError,
                id,
                span,
                isUnreferencedActions,
                isUnreferencedCollections,
                size,
                cssClass)
    }
}
