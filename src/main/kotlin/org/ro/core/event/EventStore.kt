package org.ro.core.event

import org.ro.core.Utils
import org.ro.core.aggregator.IAggregator
import org.ro.to.TransferObject
import org.ro.ui.kv.UiManager
import pl.treksoft.kvision.panel.SimplePanel
import pl.treksoft.kvision.state.observableListOf

/**
 * Keeps a log of remote invocations and the responses.
 * Subsequent invocations are served from this cache.
 * UI events (Dialogs, Windows, etc.) are logged here as well.
 *
 * @See https://en.wikipedia.org/wiki/Proxy_pattern
 * @See https://martinfowler.com/eaaDev/EventSourcing.html
 */
object EventStore {
    var log = observableListOf<LogEntry>()
    var logStartTime: Int = 0

    private fun log(logEntry: LogEntry) {
        log.add(logEntry)
        if (log.size == 1) {
            logStartTime = logEntry.createdAt.getMilliseconds()
        }
    }

    fun start(url: String, method: String, body: String = "", aggregator: IAggregator? = null): LogEntry {
        val entry = LogEntry(url, method, body)
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

    fun addView(title: String, aggregator: IAggregator, panel: SimplePanel) {
        val entry = LogEntry(title = title, aggregator = aggregator)
        entry.obj = panel
        log(entry)
        updateStatus(entry)
    }

    fun closeView(title: String) {
        val logEntry = findView(title)
        if (null != logEntry) {
            logEntry.setClose()
            //FIXME is the first agg the right one to reset?
            logEntry.getAggregator()!!.reset()
            updateStatus(logEntry)
        }
    }

    fun end(url: String, response: String): LogEntry? {
        val entry: LogEntry? = find(url)
        if (entry != null) {
            entry.response = response
            entry.setSuccess()
            updateStatus(entry)
        }
        return entry
    }

    fun fault(url: String, fault: String) {
        val entry: LogEntry? = find(url)
        entry!!.setError(fault)
        updateStatus(entry)
    }

    fun cached(url: String): LogEntry {
        val entry: LogEntry? = find(url)
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
    fun find(url: String): LogEntry? {
        val isRedundant = urlContains(url, "object-layout") || urlContains(url, "/properties/")
        if (isRedundant) {
//            cached(url)
            return findEquivalent(url)
        } else {
            return findExact(url)
        }
    }

    fun find(to: TransferObject): LogEntry? {
        return log.firstOrNull { it.obj == to }
    }

    private fun urlContains(url: String, search: String): Boolean {
        val index = url.indexOf(search)
        val answer = index >= 0
        return answer
    }

    internal fun findExact(url: String): LogEntry? {
        return log.firstOrNull { it.url == url }
    }

    internal fun findView(title: String): LogEntry? {
        return log.firstOrNull { it.title == title && it.isView() }
    }

    internal fun findEquivalent(url: String): LogEntry? {
        return log.firstOrNull { areEquivalent(it.url, url) }
    }

    private fun areEquivalent(searchUrl: String, compareUrl: String, allowedDiff: Int = 1): Boolean {
        val sl = Utils.removeHexCode(searchUrl)
        val cl = Utils.removeHexCode(compareUrl)
        val searchList: List<String> = sl.split("/")
        val compareList: List<String> = cl.split("/")
        if (searchList.size != compareList.size) {
            return false
        }

        var diffCnt = 0
        for ((i, s) in searchList.withIndex()) {
            val c = compareList[i];
            if (!s.equals(c)) {
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

    fun isCached(url: String): Boolean {
        var answer = false
        val le = this.find(url)
        if (le != null) {
            when {
                le.hasResponse() -> answer = true
                le.isView() -> answer = true
            }
        }
        return answer
    }

    fun reset() {
        log = observableListOf<LogEntry>()
    }

}
