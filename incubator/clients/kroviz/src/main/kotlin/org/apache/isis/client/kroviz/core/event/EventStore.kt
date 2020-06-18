package org.apache.isis.client.kroviz.core.event

import org.apache.isis.client.kroviz.core.aggregator.BaseAggregator
import org.apache.isis.client.kroviz.to.TObject
import org.apache.isis.client.kroviz.to.mb.Menubars
import org.apache.isis.client.kroviz.ui.kv.UiManager
import pl.treksoft.kvision.panel.SimplePanel
import pl.treksoft.kvision.state.observableListOf

/**
 * Keeps a log of remote invocations and the responses.
 * Subsequent invocations are served from this cache.
 * UI events (Dialogs, Windows, etc.) are logged here as well.
 *
 * @see "https://en.wikipedia.org/wiki/Proxy_pattern"
 * @see "https://martinfowler.com/eaaDev/EventSourcing.html"
 */
object EventStore {
    var log = observableListOf<LogEntry>()
    private var logStartTime: Int = 0

    private fun log(logEntry: LogEntry) {
        log.add(logEntry)
        if (log.size == 1) {
            logStartTime = logEntry.createdAt.getMilliseconds()
        }
    }

    fun start(reSpec: ResourceSpecification,
              method: String,
              body: String = "",
              aggregator: BaseAggregator? = null): LogEntry {
        val entry = LogEntry(reSpec.url, method, request = body, subType = reSpec.subType)
        if (aggregator != null) {
            entry.addAggregator(aggregator)
        }
        log(entry)
        updateStatus(entry)
        return entry
    }

    fun add(reSpec: ResourceSpecification) {
        val entry = LogEntry(url = reSpec.url)
        log(entry)
        updateStatus(entry)
    }

    fun addView(title: String, aggregator: BaseAggregator, panel: SimplePanel) {
        val entry = LogEntry(title = title, aggregator = aggregator)
        entry.obj = panel
        log(entry)
        updateStatus(entry)
    }

    fun closeView(title: String) {
        val logEntry = findView(title)
        if (null != logEntry) {
            logEntry.setClose()
            logEntry.getAggregator()!!.reset()
            updateStatus(logEntry)
        }
    }

    fun end(reSpec: ResourceSpecification, response: String): LogEntry? {
        val entry: LogEntry? = find(reSpec)
        if (entry != null) {
            entry.response = response
            entry.setSuccess()
            updateStatus(entry)
        }
        return entry
    }

    fun fault(reSpec: ResourceSpecification, fault: String) {
        val entry: LogEntry? = find(reSpec)
        entry!!.setError(fault)
        updateStatus(entry)
    }

    fun cached(reSpec: ResourceSpecification): LogEntry {
        val entry: LogEntry? = find(reSpec)
        entry!!.setCached()
        return entry
    }

    private fun updateStatus(entry: LogEntry) {
        UiManager.updateStatus(entry)
    }

    /**
     * Answers the first matching entry.
     */
    fun find(reSpec: ResourceSpecification): LogEntry? {
        return if (reSpec.isRedundant()) {
            findEquivalent(reSpec)
        } else {
            findExact(reSpec)
        }
    }

    fun find(tObject: TObject): LogEntry? {
        log.forEach {
            val obj = it.obj
            if (obj is TObject
                    && obj.instanceId == tObject.instanceId)
                return it
        }
        return null
    }

    fun findMenuBars(): LogEntry? {
        this.log.forEach {
           if (it.obj is Menubars)
                return it
        }
        return null
    }

    internal fun findExact(reSpec: ResourceSpecification): LogEntry? {
        return log.firstOrNull { it.matches(reSpec) }
    }

    internal fun findView(title: String): LogEntry? {
        return log.firstOrNull { it.title == title && it.isView() }
    }

    internal fun findEquivalent(reSpec: ResourceSpecification): LogEntry? {
        return log.firstOrNull { reSpec.matches(it) }
    }

    fun isCached(reSpec: ResourceSpecification, method: String): Boolean {
        val le = find(reSpec)
        return when {
            le == null -> false
            le.hasResponse() && le.method == method && le.subType == reSpec.subType -> true
            le.isView() -> true
            else -> false
        }
    }

    fun reset() {
        log.removeAll(log)
    }

}
