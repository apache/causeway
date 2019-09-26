package org.ro.core.aggregator

import org.ro.core.event.LogEntry
import org.ro.to.Action
import org.ro.to.Method
import org.ro.ui.ActionPrompt

class ActionAggregator : BaseAggregator() {

    override fun update(logEntry: LogEntry) {
        val action = logEntry.getObj() as Action
        for (l in action.links) {
            // l.rel should be neither: (self | up | describedBy )
            if (l.isInvokeAction()) {
                when (l.method) {
                    Method.GET.name -> {
                        //val obs = logEntry.aggregator!! ? this==obs?
                        this.invoke(l)
                    }
                    Method.POST.name -> {
                        ActionPrompt(action).open()
                    }
                    Method.PUT.name -> {
                    }
                    Method.PUT.name -> {
                    }
                }
            }
        }
    }
}
