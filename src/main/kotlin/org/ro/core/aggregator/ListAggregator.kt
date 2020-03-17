package org.ro.core.aggregator

import org.ro.core.event.EventState
import org.ro.core.event.LogEntry
import org.ro.core.model.ListDM
import org.ro.layout.Layout
import org.ro.to.*
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
        dsp = ListDM(actionTitle)
    }

    override fun update(logEntry: LogEntry, subType: String) {

        //TODO duplicates should be caught earlier IMPROVE
        if (logEntry.state != EventState.DUPLICATE) {
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
    }

    private fun handleList(resultList: ResultList) {
        val result = resultList.result!!
        result.value.forEach {
            it.invokeWith(this)
        }
    }

    private fun handleObject(obj: TObject) {
        console.log("[LA.handleObject]")
        dsp.addData(obj)
        val l = obj.getLayoutLink()!!
        // Json.Layout is invoked first
        l.invokeWith(this)
        // then Xml.Layout is to be called as well
        l.invokeWith(this, "xml")
    }

    private fun handleLayout(layout: Layout) {
        val dspl = dsp as ListDM
        // TODO layout is passed in at least twice.
        //  Eventually due to parallel invocations  - only once required -> IMPROVE
        if (dspl.layout == null) {
            dspl.addLayout(layout)
        }
        dspl.propertyLayoutList.forEach { p ->
            val l = p.link!!
            if (!l.href.contains("datanucleus")) { //invoking DN links lead to an error
                l.invokeWith(this)
            }
        }
    }

    private fun handleGrid(grid: Grid) {
        (dsp as ListDM).grid = grid
    }

    private fun handleProperty(p: Property) {
        val dspl = dsp as ListDM
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

    private fun Property.descriptionLink(): Link? {
        return links.find {
            it.rel == RelType.DESCRIBEDBY.type
        }
    }

    /**
     * property-description's have extensions.friendlyName whereas
     * plain properties don't have them  cf.:
     * FR_PROPERTY_DESCRIPTION
     * FR_OBJECT_PROPERTY_
     */
    private fun Property.isPropertyDescription(): Boolean {
        val hasExtensions = extensions != null
        if (!hasExtensions) {
            return false
        }
        return extensions!!.friendlyName.isNotEmpty()
    }

}
