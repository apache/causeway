package org.ro.core.aggregator

import org.ro.core.event.LogEntry
import org.ro.core.model.DisplayList
import org.ro.layout.Layout
import org.ro.layout.PropertyLt
import org.ro.to.Property
import org.ro.to.ResultList
import org.ro.to.TObject
import org.ro.to.bs3.Grid
import org.ro.ui.kv.UiManager

/** sequence of operations:
 * (0) list
 * (1) FR_OBJECT                TObjectHandler -> invoke()
 * (2) FR_OBJECT_LAYOUT         layoutHandler -> invoke(layout.getProperties()[].getLink()) link can be null?
 * (3) FR_OBJECT_PROPERTY       PropertyHandler -> invoke()
 * (4) FR_PROPERTY_DESCRIPTION  <PropertyDescriptionHandler>
 */
class ListAggregator(actionTitle: String) : BaseAggregator() {

    init {
        dsp = DisplayList(actionTitle)
    }

    override fun update(logEntry: LogEntry, subType: String) {

        when (val obj = logEntry.getTransferObject()) {
            null -> log(logEntry)
            is ResultList -> handleList(obj)
            is TObject -> handleObject(obj)
            is Layout -> handleLayout(obj)
            is Grid -> handleGrid(obj)
            is Property -> handleProperty(obj)
            else -> log(logEntry)
        }

        if (dsp.canBeDisplayed()) {
            UiManager.openListView(this)
        }
    }

    private fun handleList(resultList: ResultList) {
        val result = resultList.result
        if (result != null) {
            val links = result.value
            links.forEach {
                it.invokeWith(this)
            }
        }
    }

    private fun handleObject(obj: TObject) {
        dsp.addData(obj)
        val l = obj.getLayoutLink()!!
        // Json.Layout is invoked first
        l.invokeWith(this)
        // then Xml.Layout is to be called as well
        l.invokeWith(this, "xml")
    }

    private fun handleLayout(layout: Layout) {
        val dspl = dsp as DisplayList
        dspl.addLayout(layout)
        dspl.propertyList.forEach { p ->
            val l = p.links.firstOrNull{it.rel == "describedby"}!!
            if (!l.href.contains("datanucleus")) {
                l.invokeWith(this)
            }
        }
    }

    private fun handleGrid(grid: Grid) {
        (dsp as DisplayList).grid = grid
    }

    private fun handleProperty(p: Property) {
        val dspl = dsp as DisplayList
        if (p.isPropertyDescription()) {
            dspl.addPropertyDescription(p)
        } else {
            dspl.addProperty(p)
            p.descriptionLink()?.invokeWith(this)
        }
    }

    override fun reset(): ListAggregator {
        dsp.reset()
        return this
    }

}
