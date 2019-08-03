package org.ro.org.ro.core.observer

import org.ro.core.event.LogEntry
import org.ro.core.observer.BaseObserver
import org.ro.to.HttpError
import org.ro.view.ErrorAlert

class ErrorObserver : BaseObserver() {

    override fun update(logEntry: LogEntry) {
        val e = logEntry.getObj() as HttpError
        ErrorAlert(e).open()
    }
}
