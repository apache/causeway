package org.apache.isis.client.kroviz.ui.diagram

import org.apache.isis.client.kroviz.core.aggregator.BaseAggregator
import org.apache.isis.client.kroviz.core.event.EventStore
import org.apache.isis.client.kroviz.core.event.LogEntry
import org.apache.isis.client.kroviz.core.event.LogEntryDecorator
import org.apache.isis.client.kroviz.core.event.ResourceSpecification
import org.apache.isis.client.kroviz.to.HasLinks
import org.apache.isis.client.kroviz.ui.core.UiManager
import org.apache.isis.client.kroviz.utils.StringUtils

object LinkTreeDiagram {

    private const val NL = "\n"
    private const val prolog = "@startmindmap$NL"
    private const val epilog = "@endmindmap$NL"
    private val protocolHostPort = UiManager.getUrl()

    fun build(aggregator: BaseAggregator): String {
        var code = prolog
        val entryList: List<LogEntry> = EventStore.findAllBy(aggregator)
        val root = findRoot(entryList)
        if (root == null) {
            code += "* /$NL"
            code += createNodes(entryList, 2)
        } else {
            code += root.asPumlNode(1)
            val led = LogEntryDecorator(root)
            val childList = led.findChildrenIn(entryList)
            console.log("[LTD.build]")
            console.log(aggregator)
            console.log(entryList)
            code += createChildNodes(childList, 2)
        }
        code += epilog
        return code
    }

    private fun createChildNodes(childList: List<LogEntry>, level: Int): String {
        var code = ""
        childList.forEach {
            code += createNode(it, level)
            val led = LogEntryDecorator(it)
            val kidSet = led.findChildrenIn(childList)
            code += createChildNodes(kidSet, level +1)
        }
        return code
    }

    private fun createNode(le: LogEntry, level: Int): String {
        var code = ""
        if (isInEventStore(le.url)) {
            code += le.asPumlNode(level)
        }
        return code
    }

    private fun createNodes(entryList: List<LogEntry>, level: Int): String {
        var code = ""
        entryList.forEach {
            code += createNode(it, level)
        }
        return code
    }

    private fun findRoot(entryList: List<LogEntry>): LogEntry? {
        entryList.forEach {
            val led = LogEntryDecorator(it)
            val parent = led.findParent()
            if (parent != null && !entryList.contains(parent)) {
                return parent
            }
        }
        return null
    }

    private fun isInEventStore(url: String): Boolean {
        val rs = ResourceSpecification(url)
        val le = EventStore.findBy(rs)
        return (le != null)
    }

    fun LogEntry.asPumlNode(level: Int): String {
        val led = LogEntryDecorator(this)
        val title = StringUtils.shortTitle(this.url, protocolHostPort)
        val type = led.selfType()
        val depth = "*".repeat(level)
        var answer = "$depth:..//<<$type>>//..$NL**$title**$NL"
        answer += "----$NL"
        answer += traceInfo(this)
        return answer + ";" + NL
    }

    private fun traceInfo(logEntry: LogEntry) : String {
        val obj = logEntry.obj!!
        var answer = "__" + obj::class.simpleName + "__" + NL
        if (obj is HasLinks) {
            obj.links.forEach {
                answer += StringUtils.shortTitle(it.href, protocolHostPort) + NL
            }
        }
        console.log("[LTD.traceInfo]")
        console.log(answer)
        return answer
    }

}
