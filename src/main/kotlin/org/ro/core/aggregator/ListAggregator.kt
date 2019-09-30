package org.ro.core.aggregator

import kotlinx.serialization.Serializable
import org.ro.core.UiManager
import org.ro.core.event.LogEntry
import org.ro.core.model.DisplayList
import org.ro.layout.Layout
import org.ro.to.InvocationResult
import org.ro.to.Link
import org.ro.to.Property
import org.ro.to.TObject

/** sequence of operations:
 * (0) list
 * (1) FR_OBJECT                TObjectHandler -> invoke()
 * (2) FR_OBJECT_LAYOUT         layoutHandler -> invoke(layout.getProperties()[].getLink()) link can be null?
 * (3) FR_OBJECT_PROPERTY       PropertyHandler -> invoke()
 * (4) FR_PROPERTY_DESCRIPTION  PropertyDescriptionHandler
 */
@Serializable
class ListAggregator(val actionTitle: String) : BaseAggregator() {
    var list: DisplayList

    init {
        list = DisplayList(actionTitle)
    }

    override fun update(logEntry: LogEntry) {
        val obj = logEntry.getObj()

        when (obj) {
            is InvocationResult -> handleList(obj)
            is TObject -> handleObject(obj)
            is Layout -> handleLayout(obj)
            is Property -> handleProperty(obj)
            else -> log(logEntry)
        }

        if (list.canBeDisplayed()) {
            UiManager.handleView(list)
        }
    }

    private fun handleList(invocationResult: InvocationResult) {
        val result = invocationResult.result!!
        val links = result.value
        for (l: Link in links) {
            invoke(l)
        }
    }

    private fun handleObject(obj: TObject) {
        list.addData(obj)
        val l = obj.getLayoutLink()
        if (l != null) {
            invoke(l)
        }
    }

    private fun handleLayout(layout: Layout) {
        list.layout = layout
        val pls = layout.properties
        for (pl in pls) {
            val l = pl.link!!
            invoke(l)
        }
    }

    private fun handleProperty(p: Property) {
        if (p.isPropertyDescription()) {
            list.addPropertyLabel(p)
        } else {
            list.addProperty(p)
            val l = p.descriptionLink()!!
            invoke(l)
        }
    }

}
