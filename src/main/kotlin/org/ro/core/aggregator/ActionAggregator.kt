package org.ro.core.aggregator

import org.ro.core.event.LogEntry
import org.ro.to.Action
import org.ro.to.Link
import org.ro.to.Method
import org.ro.ui.kv.ActionPrompt

class ActionAggregator : BaseAggregator() {

    override fun update(logEntry: LogEntry) {
        val action = logEntry.getTransferObject() as Action
        action.links.forEach {
            // it.rel should be neither: (self | up | describedBy )
            if (it.isInvokeAction()) {
                when (it.method) {
                    Method.GET.name -> processGet(action, it)
                    Method.POST.name -> processPost(action)
                    Method.PUT.name -> processPut(action)
                    Method.DELETE.name -> processDelete(action)
                }
            }
        }
    }

    private fun processGet(action: Action, link: Link) {
        if (link.hasArguments()) {
            ActionPrompt(action).open()
        } else {
            this.invoke(link)
        }
    }

    private fun processPost(action: Action) {
        ActionPrompt(action).open()
    }

    private fun processPut(action: Action) {
        throw Exception("[ActionAggregator.processPut] notImplementedYet")
    }

    private fun processDelete(action: Action) {
        throw Exception("[ActionAggregator.processDelete] notImplementedYet")
    }

    private fun Link.isInvokeAction(): Boolean {
        if (rel.contains("invokeaction")) {
            return true
        }
        if (rel.contains("invoke;action")) {
            return true
        }
        return false
    }

}
