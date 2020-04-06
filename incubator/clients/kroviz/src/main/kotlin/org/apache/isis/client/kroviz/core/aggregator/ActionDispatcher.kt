package org.apache.isis.client.kroviz.core.aggregator

import org.apache.isis.client.kroviz.core.event.LogEntry
import org.apache.isis.client.kroviz.to.Action
import org.apache.isis.client.kroviz.to.Link
import org.apache.isis.client.kroviz.to.Method
import org.apache.isis.client.kroviz.ui.Point
import org.apache.isis.client.kroviz.ui.kv.ActionPrompt
import org.apache.isis.client.kroviz.utils.Utils

class ActionDispatcher(private val at: Point = Point(100, 100)) : BaseAggregator() {

    override fun update(logEntry: LogEntry, subType: String) {
        val action = logEntry.getTransferObject() as Action
        action.links.forEach { link ->
            if (link.isInvokeAction()) {
                when (link.method) {
                    Method.GET.name -> processGet(action, link)
                    Method.POST.name -> processPost(action, link)
                    Method.PUT.name -> invoke(link)
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
            link.invokeWith(this)
        }
    }

    private fun processPost(action: Action, link: Link) {
        val title = Utils.deCamel(action.id)
        if (link.hasArguments()) {
            ActionPrompt(action).open(at)
        } else {
            link.invokeWith(ObjectAggregator(title))
        }
    }

    fun invoke(link: Link) {
        link.invokeWith(this)
    }

}
