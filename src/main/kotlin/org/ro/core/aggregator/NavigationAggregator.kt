package org.ro.core.aggregator

import kotlinx.serialization.Serializable
import org.ro.core.Menu
import org.ro.core.UiManager
import org.ro.core.event.LogEntry
import org.ro.to.Result
import org.ro.to.Service
import org.ro.to.TransferObject

@Serializable
class NavigationAggregator : BaseAggregator() {
    private var isRendered = false;
    private var serviceTotal = 0;
    private var serviceCount = 0;

    @ExperimentalUnsignedTypes
    override fun update(logEntry: LogEntry) {
        val obj = logEntry.getObj()

        when (obj) {
            is Result -> handleResult(obj)
            is Service -> handleService(obj)
            else -> log(logEntry)
        }

        if (serviceCount >= serviceTotal) {
            if (!isRendered) {
                UiManager.amendMenu()
                isRendered = true
            }
        }
    }

    private fun handleService(obj: Service) {
        Menu.add(obj)
        serviceCount++
    }

    private fun handleResult(obj: TransferObject) {
        val result = obj as Result
        val values = result.value
        serviceTotal = values.size
        for (l in values) {
            invoke(l)
        }
    }

}
