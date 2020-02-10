package org.ro.ui

import org.ro.core.Utils
import org.ro.core.event.EventState
import org.ro.core.event.EventStore
import org.ro.org.ro.core.event.ReplayEvent
import org.ro.ui.kv.RoDialog

class ExportAlert() : Command {

    private var jsonOutput: String = ""

    fun open() {
        val replayEvents = collectReplayEvents()
        jsonOutput = JSON.stringify(replayEvents)
        jsonOutput = Utils.format(jsonOutput)
        val formItems = mutableListOf<FormItem>()
        formItems.add(FormItem("JSON", "TextArea", jsonOutput, 15))
        val label = "Download Replay Events"
        RoDialog(caption = label, items = formItems, command = this).open()
    }

    private fun collectReplayEvents(): MutableList<ReplayEvent> {
        val replayEvents = mutableListOf<ReplayEvent>()
        EventStore.log.forEach { it ->
            val re = ReplayEvent(
                    url = it.url,
                    method = it.method!!,
                    request = it.request,
                    state = it.state.toString(),
                    offset = 0L,   // can timimg be relevant ?
                    response = it.response
            )
            when (it.state) {
                EventState.SUCCESS -> replayEvents.add(re)
                EventState.ERROR -> replayEvents.add(re)
                else -> {
                }
            }
        }
        return replayEvents
    }

    override fun execute() {
        Utils.download("ReplayEvents.json", jsonOutput)
    }

}
