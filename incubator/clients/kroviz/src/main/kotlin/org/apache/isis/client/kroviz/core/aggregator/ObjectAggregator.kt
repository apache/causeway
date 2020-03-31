package org.apache.isis.client.kroviz.core.aggregator

import org.apache.isis.client.kroviz.core.event.LogEntry
import org.apache.isis.client.kroviz.core.model.ObjectDM
import org.apache.isis.client.kroviz.layout.Layout
import org.apache.isis.client.kroviz.to.HttpError
import org.apache.isis.client.kroviz.to.Property
import org.apache.isis.client.kroviz.to.ResultObject
import org.apache.isis.client.kroviz.to.TObject
import org.apache.isis.client.kroviz.to.bs3.Grid
import org.apache.isis.client.kroviz.ui.ErrorAlert
import org.apache.isis.client.kroviz.ui.kv.UiManager

class ObjectAggregator(val actionTitle: String) : BaseAggregator() {

    init {
        dsp = ObjectDM(actionTitle)
    }

    override fun update(logEntry: LogEntry, subType: String) {

        when (val obj = logEntry.getTransferObject()) {
            is TObject -> handleObject(obj)
            is ResultObject -> handleResultObject(obj)
            is Property -> handleProperty(obj)
            is Layout -> handleLayout(obj)
            is Grid -> handleGrid(obj)
            is HttpError -> ErrorAlert(logEntry).open()
            else -> log(logEntry)
        }

        if (dsp.canBeDisplayed()) {
            UiManager.openObjectView(this)
        }
    }

    fun handleObject(obj: TObject) {
        dsp.addData(obj)
        val l = obj.getLayoutLink()!!
        // Json.Layout is invoked first
        l.invokeWith(this)
        // then Xml.Layout is to be invoked as well
        l.invokeWith(this, "xml")
    }

    fun handleResultObject(obj: ResultObject) {
        console.log("[OA.handleResultObject]")
        console.log(obj)
        // dsp.addData(obj)
    }

    override fun getObject(): TObject? {
        return dsp.getObject()
    }

    private fun handleProperty(property: Property) {
        console.log("[ObjectAggregator.handleProperty] yet to be implemented")
        console.log(property)
    }

    private fun handleLayout(layout: Layout) {
        val dm = dsp as ObjectDM
        if (dm.layout == null) {
            dm.addLayout(layout)
            dm.propertyLayoutList.forEach { p ->
                val l = p.link!!
                val isDn = l.href.contains("datanucleus")
                if (isDn) {
                    //invoking DN links leads to an error
                    l.invokeWith(this)
                }
            }
        }
    }

    private fun handleGrid(grid: Grid) {
        (dsp as ObjectDM).grid = grid
    }


    override fun reset(): ObjectAggregator {
        dsp.isRendered = false
        return this
    }

}
