package org.ro.core.aggregator

import kotlinx.serialization.Serializable
import org.ro.core.event.LogEntry
import org.ro.core.model.BaseDisplayable
import org.ro.to.TransferObject
import org.ro.to.mb3.Menubars
import org.ro.ui.kv.UiManager

@Serializable
class XmlNavigationAggregator : BaseAggregator() {
    override var dsp: BaseDisplayable
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}

    @ExperimentalUnsignedTypes
    override fun update(logEntry: LogEntry) {
        console.log("[XmlNavigationHandler.update]")
        console.log(logEntry)
        val obj = logEntry.getTransferObject()

        when (obj) {
            is Menubars -> handleResult(obj)
            else -> log(logEntry)
        }
    }

    private fun handleResult(obj: TransferObject) {
        val result = obj as Menubars
        console.log("[XmlNavigationHandler.handleResult]")
        console.log(obj)
        //val values = result.value
        //FIXME pass MenuEntries as argument
        UiManager.amendMenu()
        isRendered = true
    }

}
