package org.ro.core.aggregator

import kotlinx.serialization.Serializable
import org.ro.ui.Menu
import org.ro.core.event.LogEntry
import org.ro.core.model.BaseDisplayable
import org.ro.to.ResultListResult
import org.ro.to.Service
import org.ro.to.TransferObject
import org.ro.ui.kv.UiManager

@Serializable
class NavigationAggregator : BaseAggregator() {
    override var dsp: BaseDisplayable
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    private var serviceTotal = 0
    private var serviceCount = 0

    @ExperimentalUnsignedTypes
    override fun update(logEntry: LogEntry) {
        val obj = logEntry.getTransferObject()

        when (obj) {
            is ResultListResult -> handleResult(obj)
            is Service -> handleService(obj)
            else -> log(logEntry)
        }

        if (serviceCount >= serviceTotal && !isRendered) {
            UiManager.amendMenu()
            isRendered = true
        }
    }

    private fun handleService(obj: Service) {
        Menu.add(obj)
        serviceCount++
    }

    private fun handleResult(obj: TransferObject) {
        val result = obj as ResultListResult
        val values = result.value
        serviceTotal = values.size
        for (l in values) {
            invoke(l)
        }
    }

}
