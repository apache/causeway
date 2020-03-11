package org.ro.core.aggregator

import org.ro.core.event.LogEntry
import org.ro.to.Action
import org.ro.to.Link
import org.ro.to.Method
import org.ro.ui.Point
import org.ro.ui.kv.ActionPrompt
import org.ro.utils.Utils
import org.w3c.dom.MimeType

class ActionDispatcher(private val at: Point = Point(100, 100)) : BaseAggregator() {

    override fun update(logEntry: LogEntry, mimeType: String) {
        val action = logEntry.getTransferObject() as Action
        action.links.forEach { link ->
            // it.rel should be neither: (self | up | describedBy )
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
            invoke(link)
        }
    }

    private fun processPost(action: Action, link: Link) {
        console.log("[ActionDispatcher.processPost] link.hasArguments: ${link.hasArguments()}")
        console.log(link)
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
