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

import org.apache.isis.client.kroviz.core.aggregator.AggregatorWithLayout
import org.apache.isis.client.kroviz.core.aggregator.BaseAggregator
import org.apache.isis.client.kroviz.core.event.EventStore
import org.apache.isis.client.kroviz.core.event.LogEntry
import org.apache.isis.client.kroviz.core.event.ResourceSpecification
import org.apache.isis.client.kroviz.to.HasLinks
import org.apache.isis.client.kroviz.ui.core.UiManager
import org.apache.isis.client.kroviz.utils.StringUtils

object LinkTreeDiagram {

    private val protocolHostPort = UiManager.getUrl()

    fun build(aggregator: BaseAggregator): String {
        console.log("[LTD.build]")
        val pc = PumlCode()
        if (aggregator is AggregatorWithLayout) {
            val tree = aggregator.tree!!
            val root = tree.root
            console.log(root)
            pc.code += toPumlCode(root, 1)
        }
        pc.mindmap()
        console.log(pc.code)
        return pc.code
    }

    private fun toPumlCode(node: Node, level: Int): String {
        val url = node.key
        val rs = ResourceSpecification(url)
        val le = EventStore.findBy(rs)
        val pc = PumlCode()
        if (le != null) {
            val title = StringUtils.shortTitle(url, protocolHostPort)
            val type = le.selfType()
            val depth = "*".repeat(level)
            pc.add(depth).add(":")
            pc.addStereotype(type)
            pc.addLink(url, title)
            pc.addHorizontalLine()
            pc.add(traceInfo(le))
            pc.addLine(";")
            node.children.forEach {
                val childCode = toPumlCode(it, level + 1)
                pc.add(childCode)
            }
        }
        return pc.code
    }

    private fun traceInfo(logEntry: LogEntry): String {
        val pc = PumlCode()
        val obj = logEntry.obj
        if (obj != null) {
            val className = obj::class.simpleName!!
            pc.addClass(className)
            if (obj is HasLinks) {
                obj.links.forEach {
                    val url = it.href
                    val title = StringUtils.shortTitle(url, protocolHostPort)
                    pc.addLink(url, title)
                }
            }
        }
        return pc.code
    }

    private fun LogEntry.selfType(): String {
        val selfLink = this.selfLink()
        return if (selfLink != null) {
            selfLink.representation().type
        } else {
            console.log("[LE.selfType]")
            console.log(this)
            ""
        }
    }

}
