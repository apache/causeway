package org.apache.isis.client.kroviz.ui.diagram

import org.apache.isis.client.kroviz.core.event.EventStore
import org.apache.isis.client.kroviz.core.event.LogEntry
import org.apache.isis.client.kroviz.core.event.ResourceSpecification
import org.apache.isis.client.kroviz.to.HasLinks
import org.apache.isis.client.kroviz.to.Link
import org.apache.isis.client.kroviz.to.Relation
import org.apache.isis.client.kroviz.ui.core.UiManager

object SequenceDiagram {

    fun build(logEntries: List<LogEntry>): String? {
        val first: LogEntry? = logEntries.find { le ->
            le.url.endsWith("/restful/")
        }
        if (first != null) {
            return with(first)
        }
        return null
    }

    private val Q = "\""
    private val NL = "\\n"

    fun with(rootLE: LogEntry): String {
        var code = "$Q@startuml$NL"
        code += iterateOverChildren(rootLE)
        code += "@enduml$Q"
        return code
    }

    private fun iterateOverChildren(logEntry: LogEntry): String {
        var code = ""
        val tObj = logEntry.obj
        val parentUrl = logEntry.url
        if (tObj is HasLinks) {
            tObj.getLinks().forEach { l ->
                val rel = l.relation()
                if (rel != Relation.UP && rel != Relation.SELF) {
                    code += amendWithChild(parentUrl, l)
                }
            }
        }
        return code
    }

    private fun amendWithChild(parentUrl: String, child: Link): String {
        // kroki.io can not handle / (slash) in strings; escaping doesn't work either
        val baseUrl = UiManager.getUrl()
        var source = parentUrl.replace(baseUrl, "")
        source = source.replace("/", "_")
        val childUrl = child.href
        var target = childUrl.replace(baseUrl, "")
        target = target.replace("/", "_")
        var code = "$source -> $target $NL"

        val rs = ResourceSpecification(childUrl)
        val childLE = EventStore.findBy(rs)
        if (childLE != null) {
            code += iterateOverChildren(childLE)
        }
        return code
    }

}
