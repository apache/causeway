package org.ro.core.aggregator

import org.ro.core.event.LogEntry
import org.ro.core.model.DisplaySystem
import org.ro.to.DomainTypes
import org.ro.to.User
import org.ro.to.Version

class SystemAggregator() : BaseAggregator() {

    init {
        dsp = DisplaySystem("not filled (yet)")
    }

    override fun update(logEntry: LogEntry) {

        when (val obj = logEntry.getTransferObject()) {
            is User -> dsp.addData(obj)
            is Version -> dsp.addData(obj)
            is DomainTypes -> dsp.addData(obj)
            else -> log(logEntry)
        }

        if (dsp.canBeDisplayed()) {
//            UiManager.openObjectView(this)
        }
    }

    override fun reset(): SystemAggregator {
        dsp.isRendered = false
        return this
    }

}
