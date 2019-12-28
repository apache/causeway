package org.ro.core.aggregator

import org.ro.core.event.LogEntry
import org.ro.core.model.BaseDisplayable
import org.ro.core.model.DisplayObject
import org.ro.layout.Layout
import org.ro.to.HttpError
import org.ro.to.TObject
import org.ro.ui.ErrorAlert
import org.ro.ui.kv.UiManager

class ObjectAggregator(val actionTitle: String) : BaseAggregator() {

    override var dsp: BaseDisplayable = DisplayObject(actionTitle)

    override fun update(logEntry: LogEntry) {
        val obj = logEntry.getTransferObject()

        when (obj) {
            is TObject -> handleObject(obj)
            is Layout -> handleLayout(obj)
            is HttpError -> ErrorAlert(logEntry).open()
            else -> log(logEntry)
        }

        if (dsp.canBeDisplayed()) {
            UiManager.openObjectView(this)
        }
    }

    fun handleObject(obj: TObject) {
        dsp.addData(obj)
        val l = obj.getLayoutLink()
        if (l != null) {
            invoke(l)
        }
    }

    private fun handleLayout(layout: Layout) {
        dsp.layout = layout
        layout.properties.forEach {
            val l = it.link!!
            invoke(l)
        }
    }

    override fun reset(): ObjectAggregator {
        dsp.isRendered = false
        return this
    }

}
