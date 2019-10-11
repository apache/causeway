package org.ro.core.aggregator

import org.ro.core.UiManager
import org.ro.core.event.LogEntry
import org.ro.core.model.DisplayObject
import org.ro.layout.Layout
import org.ro.to.HttpError
import org.ro.to.TObject
import org.ro.ui.ErrorAlert

class ObjectAggregator(val actionTitle: String) : BaseAggregator() {
    var dsp: DisplayObject

    init {
        dsp = DisplayObject(actionTitle)
    }

    override fun update(logEntry: LogEntry) {
        val obj = logEntry.getObj()

        when (obj) {
            is TObject -> handleObject(obj)
            is Layout -> handleLayout(obj)
            is HttpError -> ErrorAlert(logEntry).open()
            else -> log(logEntry)
        }

        if (dsp.canBeDisplayed()) {
            UiManager.openObjectView(dsp, this)
        }
    }

    private fun handleObject(obj: TObject) {
        console.log("[OA.handleObject]")
        console.log(obj)
        dsp.addData(obj)
        val l = obj.getLayoutLink()
        if (l != null) {
            invoke(l)
        }
    }

    private fun handleLayout(layout: Layout) {
        console.log("[OA.handleLayout]")
        console.log(layout)
        dsp.layout = layout
        val pls = layout.properties
        for (pl in pls) {
            val l = pl.link!!
            invoke(l)
        }
    }

    override fun reset() {
        dsp.isRendered = false
    }

}
