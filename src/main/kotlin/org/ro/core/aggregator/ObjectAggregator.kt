package org.ro.org.ro.core.aggregator

import org.ro.core.UiManager
import org.ro.core.aggregator.BaseAggregator
import org.ro.core.event.LogEntry
import org.ro.core.model.DisplayObject
import org.ro.layout.Layout
import org.ro.to.TObject

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
            else -> log(logEntry)
        }

        if (dsp.canBeDisplayed()) {
            UiManager.createObjectView(dsp)
        }
    }

    private fun handleObject(obj: TObject) {
        dsp.addData(obj)
        val l = obj.getLayoutLink()
        if (l != null) {
            invoke(l)
        }
    }

    private fun handleLayout(layout: Layout) {
        dsp.layout = layout
        val pls = layout.properties
        for (pl in pls) {
            val l = pl.link!!
            invoke(l)
        }
    }
}
