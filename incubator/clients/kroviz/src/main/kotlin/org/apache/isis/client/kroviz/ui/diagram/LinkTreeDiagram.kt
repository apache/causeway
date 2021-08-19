package org.apache.isis.client.kroviz.ui.diagram

import org.apache.isis.client.kroviz.core.event.EventStore
import org.apache.isis.client.kroviz.core.event.LogEntry
import org.apache.isis.client.kroviz.core.event.LogEntryDecorator

object LinkTreeDiagram {

    private val NL = "\n"
    private val SEP = " | "
    private val OPEN = "{"
    private val CLOSE = "}"
    private val PLUS = "+"

    fun build(): String {
        var code = "@startsalt$NL$OPEN$NL$OPEN T#$NL"
        val roots: List<LogEntry> = findRoots()
        roots.forEach {
            code += iterateOverChildren(it, PLUS)
        }
        code += "$CLOSE$NL$CLOSE$NL@endsalt"
        return code
    }

    private fun findRoots(): List<LogEntry> {
        val rootSet = mutableListOf<LogEntry>()
        EventStore.getLinked().forEach { le ->
            val led = LogEntryDecorator(le)
            if (!led.hasParent()) rootSet.add(le)  // this may still include
        }
        return rootSet
    }

    private fun iterateOverChildren(logEntry: LogEntry, prefix: String): String {
        val led = LogEntryDecorator(logEntry)
        val children = led.findChildren()
        val orphans = led.findOrphans(children)
        var code = prefix + " " + led.shortTitle() + SEP + led.selfType() + SEP + orphans.toString() + NL
        children.forEach {
            code += iterateOverChildren(it, prefix + PLUS)
        }
        return code
    }

}
