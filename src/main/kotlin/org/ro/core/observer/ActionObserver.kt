package org.ro.org.ro.core.observer

import org.ro.core.event.LogEntry
import org.ro.core.observer.BaseObserver
import org.ro.to.Action
import org.ro.to.Method
import org.ro.view.ActionPrompt

class ActionObserver : BaseObserver() {

    override fun update(logEntry: LogEntry) {
        val action = logEntry.getObj() as Action
        for (l in action.links) {
            // l.rel should be neither: (self | up | describedBy )
            if (l.isInvokeAction()) {
                when (l.method) {
                    Method.GET.name -> {
                        l.invoke(logEntry.observer)
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
