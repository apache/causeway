package org.ro.core.event

class ObjectObserver(var baseUrl: String) : ILogEventObserver {
    override fun update(le: LogEntry) {
        //check if all required data has arrived and open tab
        // minimum requirement: - Layout

        //TODO how can be determined, which observer should be assigned to LE?
        // where should this take place? I ResponseHandler? In LE?
    }
}