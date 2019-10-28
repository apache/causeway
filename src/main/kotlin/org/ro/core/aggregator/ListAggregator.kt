package org.ro.core.aggregator

import org.ro.core.event.LogEntry
import org.ro.core.model.DisplayList
import org.ro.layout.Layout
import org.ro.to.Property
import org.ro.to.ResultList
import org.ro.to.TObject
import org.ro.ui.kv.UiManager

/** sequence of operations:
 * (0) list
 * (1) FR_OBJECT                TObjectHandler -> invoke()
 * (2) FR_OBJECT_LAYOUT         layoutHandler -> invoke(layout.getProperties()[].getLink()) link can be null?
 * (3) FR_OBJECT_PROPERTY       PropertyHandler -> invoke()
 * (4) FR_PROPERTY_DESCRIPTION  PropertyDescriptionHandler
 */
class ListAggregator(val actionTitle: String) : BaseAggregator() {
    init {
        dsp = DisplayList(actionTitle)
    }

    override fun update(logEntry: LogEntry) {
        val obj = logEntry.getTransferObject()

        when (obj) {
            is ResultList -> handleList(obj)
            is TObject -> handleObject(obj)
            is Layout -> handleLayout(obj)
            is Property -> handleProperty(obj)
            else -> log(logEntry)
        }

        if (dsp!!.canBeDisplayed()) {
            UiManager.openListView(this)
        }
    }

    private fun handleList(resultList: ResultList) {
        val result = resultList.result
        if (result != null) {
            val links = result.value
            links.forEach {
                invoke(it)
            }
        }
    }

    private fun handleObject(obj: TObject) {
        dsp!!.addData(obj)
        val l = obj.getLayoutLink()
        if (l != null) {
            invoke(l)
        }
    }

    private fun handleLayout(layout: Layout) {
        dsp!!.layout = layout
        layout.properties.forEach {
            val l = it.link!!
            invoke(l)
        }
    }

    private fun handleProperty(p: Property) {
        if (p.isPropertyDescription()) {
            (dsp!! as DisplayList).addPropertyLabel(p)
        } else {
            (dsp!! as DisplayList).addProperty(p)
            val l = p.descriptionLink()!!
            invoke(l)
        }
    }

    override fun reset() : ListAggregator {
        dsp!!.reset()
        return this
    }

}
