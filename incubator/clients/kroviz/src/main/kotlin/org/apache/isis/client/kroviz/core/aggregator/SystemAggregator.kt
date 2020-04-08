package org.apache.isis.client.kroviz.core.aggregator

import org.apache.isis.client.kroviz.core.event.LogEntry
import org.apache.isis.client.kroviz.core.model.SystemDM
import org.apache.isis.client.kroviz.to.DomainTypes
import org.apache.isis.client.kroviz.to.User
import org.apache.isis.client.kroviz.to.Version

class SystemAggregator() : BaseAggregator() {

    init {
        dsp = SystemDM("not filled (yet)")
    }

    override fun update(logEntry: LogEntry, subType: String) {

        when (val obj = logEntry.getTransferObject()) {
            is User -> dsp.addData(obj)
            is Version -> dsp.addData(obj)
            is DomainTypes -> dsp.addData(obj)
            else -> log(logEntry)
        }

        if (dsp.canBeDisplayed()) {
//  TODO          UiManager.openObjectView(this)
        }
    }

    override fun reset(): SystemAggregator {
        dsp.isRendered = false
        return this
    }

}
