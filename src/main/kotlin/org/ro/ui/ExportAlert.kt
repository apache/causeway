package org.ro.ui

import org.ro.core.event.EventState
import org.ro.core.event.EventStore
import org.ro.org.ro.core.event.ReplayEvent
import org.ro.ui.kv.RoDialog

class ExportAlert() : Command {

    fun open() {
        val replayEvents = collectReplayEvents()
        val jsonOutput = JSON.stringify(replayEvents)
        console.log("[ExportAlert.open]")
        console.log(jsonOutput)
        val formItems = mutableListOf<FormItem>()
        formItems.add(FormItem("URL", "Text", "ReplayEvents.json"))
        formItems.add(FormItem("Blob", "TextArea", jsonOutput, 20))
        val label = "export"
        RoDialog(caption = label, items = formItems, command = this).open()
    }

    private fun collectReplayEvents(): MutableList<ReplayEvent>{
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
        //TODO write out to FS
    }
}
