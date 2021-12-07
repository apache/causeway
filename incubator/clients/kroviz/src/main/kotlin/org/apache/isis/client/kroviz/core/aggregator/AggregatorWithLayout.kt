package org.apache.isis.client.kroviz.core.aggregator

import org.apache.isis.client.kroviz.core.event.LogEntry
import org.apache.isis.client.kroviz.core.model.DisplayModelWithLayout
import org.apache.isis.client.kroviz.layout.Layout
import org.apache.isis.client.kroviz.to.Represention
import org.apache.isis.client.kroviz.to.TObject
import org.apache.isis.client.kroviz.ui.core.Constants
import org.apache.isis.client.kroviz.ui.diagram.Tree

abstract class AggregatorWithLayout : BaseAggregator() {
    // parentUrl is to be set in update
    // and to be used in subsequent invocations
    var parentUrl: String? = null
    var tree: Tree? = null

    override fun update(logEntry: LogEntry, subType: String) {
        parentUrl = logEntry.url
    }

    protected fun handleLayout(layout: Layout, dm: DisplayModelWithLayout, referrer: String) {
        if (dm.layout == null) {
            dm.addLayout(layout)
            dm.properties.propertyLayoutList.forEach { p ->
                val l = p.link
                if (l == null) {
                    console.log("[AWL.handleLayout]")
                    console.log(p.id + " link empty")  // ISIS-2846
                    console.log(p)
                } else {
                    val isDn = l.href.contains("datanucleus")
                    if (!isDn) {
                        //invoking DN links leads to an error
                        invoke(l, this, referrer = referrer)
                    }
                }
            }
        }
    }

    protected fun invokeLayoutLink(obj: TObject, aggregator: AggregatorWithLayout, referrer: String) {
        val l = obj.getLayoutLink()
        if (l.representation() == Represention.OBJECT_LAYOUT_BS3) {
            invoke(l, aggregator, Constants.subTypeXml, referrer)
        } else {
            invoke(l, aggregator, referrer = referrer)
        }
    }

    protected fun invokeIconLink(obj: TObject, aggregator: AggregatorWithLayout, referrer: String) {
        val l = obj.getIconLink()!!
        invoke(l, aggregator, referrer = referrer)
    }

}
