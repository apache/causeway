package org.ro.core.event

import org.ro.core.aggregator.BaseAggregator
import org.ro.to.TObject
import org.ro.ui.kv.UiManager
import org.ro.utils.Utils
import pl.treksoft.kvision.panel.SimplePanel
import pl.treksoft.kvision.state.observableListOf

/**
 * Keeps a log of remote invocations and the responses.
 * Subsequent invocations are served from this cache.
 * UI events (Dialogs, Windows, etc.) are logged here as well.
 *
 * @see https://en.wikipedia.org/wiki/Proxy_pattern
 * @see https://martinfowler.com/eaaDev/EventSourcing.html
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

    fun add(url: String) {
        val entry = LogEntry(url = url)
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
     * @param url
     * @return
     */
    fun find(reSpec: ResourceSpecification): LogEntry? {
        val url = reSpec.url
        val isRedundant = url.contains("object-layout") || url.contains("/properties/")
        //val isRedundant = false // FIXME
        return if (isRedundant) {
            findEquivalent(reSpec)
        } else {
            findExact(reSpec)
        }
    }

    fun find(tObject: TObject): LogEntry? {
        log.forEach {
            if (it.obj is TObject) {
                if ((it.obj as TObject).instanceId == tObject.instanceId)
                    return it
            }
        }
        return null
    }

    internal fun findExact(reSpec: ResourceSpecification): LogEntry? {
        log.forEach {
            if (it.matches(reSpec)) {
                return it
            }
        }
        return null
    }

    internal fun findView(title: String): LogEntry? {
        return log.firstOrNull { it.title == title && it.isView() }
    }

    internal fun findEquivalent(reSpec: ResourceSpecification): LogEntry? {
        log.forEach {
            if (it.matches(reSpec)
                    && areEquivalent(it.url, reSpec.url)) {
                return it
            }
        }
        return null
    }

    private fun areEquivalent(searchUrl: String, compareUrl: String, allowedDiff: Int = 1): Boolean {
        val sl = Utils.removeHexCode(searchUrl)
        val cl = Utils.removeHexCode(compareUrl)
        val searchList: List<String> = sl.split("/")
        val compareList: List<String> = cl.split("/")
        if (compareList.size != searchList.size) {
            return false
        }

        var diffCnt = 0
        for ((i, s) in searchList.withIndex()) {
            val c = compareList[i]
            if (s != c) {
                diffCnt++
                val n = s.toIntOrNull()
                // if the difference is a String, it is not allowed and counts double
                if (n == null) {
                    diffCnt++
                }
            }
        }
        return diffCnt <= allowedDiff
    }

    fun isCached(reSpec: ResourceSpecification, method: String): Boolean {
        var answer = false
        val le = this.find(reSpec)
        if (le != null) {
            when {
                le.hasResponse() && le.method == method && le.subType == reSpec.subType -> answer = true
                le.isView() -> answer = true
            }
        }
        return answer
    }

    fun reset() {
        log.removeAll(log)
    }

}
