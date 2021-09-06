/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
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
            code += "* / $NL"
            code += createNodes(entryList, 2)
        } else {
            code += root.asPumlNode(1)
            val led = LogEntryDecorator(root)
            val childList = led.findChildrenOfLogEntry()
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
            val kidSet = led.findChildrenOfLogEntry()
            code += createChildNodes(kidSet, level + 1)
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
        val url = this.url
        val title = StringUtils.shortTitle(url, protocolHostPort)
        val type = led.selfType()
        val depth = "*".repeat(level)
        val pc = PumlCode()
        pc.add(depth).add(":")
        pc.addStereotype(type)
        pc.addLink(url, title)
        pc.addHorizontalLine()
        pc.add(traceInfo(this))
        pc.addLine(";")
        return pc.code
    }

    private fun traceInfo(logEntry: LogEntry): String {
        val obj = logEntry.obj!!
        val className = obj::class.simpleName!!
        val pc = PumlCode().addClass(className)
        if (obj is HasLinks) {
            obj.links.forEach {
                val url = it.href
                val title = StringUtils.shortTitle(url, protocolHostPort)
                pc.addLink(url, title)
            }
        }
        return pc.code
    }

}
