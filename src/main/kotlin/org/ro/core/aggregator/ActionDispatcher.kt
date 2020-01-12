package org.ro.core.aggregator

import org.ro.core.event.LogEntry
import org.ro.to.Action
import org.ro.to.Link
import org.ro.to.Method
import org.ro.ui.Point
import org.ro.ui.kv.ActionPrompt

class ActionDispatcher(private val at: Point = Point(100,100)) : BaseAggregator() {

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

    /**
     *  link.rel should neither be: (self | up | describedBy )
     */
    private fun Link.isInvokeAction(): Boolean {
        return rel.contains("invoke") && rel.contains("action")
    }

    private fun processGet(action: Action, link: Link) {
        if (link.hasArguments()) {
            ActionPrompt(action).open(at)
        } else {
            this.invoke(link)
        }
    }

    private fun processPost(action: Action) {
        ActionPrompt(action).open(at)
    }

    private fun processPut(action: Action) {
        // sample:
        // url: http://localhost:8080/restful/objects/simple.SimpleObject/0/properties/notes
        // body:   {"value": "FoolOnTheHill"}
        throw Exception("[ActionAggregator.processPut] notImplementedYet $action")
    }

    private fun processDelete(action: Action) {
        throw Exception("[ActionAggregator.processDelete] notImplementedYet $action")
    }


}
