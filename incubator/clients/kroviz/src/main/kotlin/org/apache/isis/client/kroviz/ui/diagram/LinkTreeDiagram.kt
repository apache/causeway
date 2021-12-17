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
import org.apache.isis.client.kroviz.core.event.LogEntry
import org.apache.isis.client.kroviz.core.event.ResourceSpecification
import org.apache.isis.client.kroviz.to.WithLinks
import org.apache.isis.client.kroviz.to.Property
import org.apache.isis.client.kroviz.to.Relation
import org.apache.isis.client.kroviz.to.Represention
import org.apache.isis.client.kroviz.ui.core.SessionManager
import org.apache.isis.client.kroviz.utils.StringUtils

object LinkTreeDiagram {

    private val protocolHostPort = SessionManager.getBaseUrl()

    fun build(aggregator: BaseAggregator): String {
        val pc = PumlCode()
        if (aggregator is AggregatorWithLayout) {
            val tree = aggregator.tree!!
            val root = tree.root
            pc.code += toPumlCode(root, 1)
        }
        pc.toMindmap()
        return pc.code
    }

    private fun toPumlCode(node: Node, level: Int): String {
        val url = node.name
        val rs = ResourceSpecification(url)
        val le = SessionManager.getEventStore().findBy(rs)
        val pc = PumlCode()
        if (le != null) {
            val title = StringUtils.shortTitle(url)
            pc.addStereotype(le.type)
            pc.addLink(url, title)
            pc.addHorizontalLine()
            pc.add(linkInfo(le))
            pc.toMindmapNode(level)
            node.children.forEach {
                val childCode = toPumlCode(it, level + 1)
                pc.add(childCode)
            }
            if (le.type == Represention.PROPERTY_DESCRIPTION.type) {
                val pdCode = propertyDescriptionInfo(le, level + 1)
                pc.add(pdCode)
            }
        }
        return pc.code
    }

    private fun linkInfo(logEntry: LogEntry): String {
        val pc = PumlCode()
        val obj = logEntry.obj
        if (obj != null) {
            val className = obj::class.simpleName!!
            pc.addClass(className)
            if (obj is WithLinks) {
                obj.links.forEach {
                    if (it.relation() != Relation.SELF) {
                        val url = it.href
                        val title = StringUtils.shortTitle(url)
                        pc.addLink(url, title)
                    }
                }
            }
        }
        pc.trim()
        return pc.code
    }

    private fun propertyDescriptionInfo(logEntry: LogEntry, level: Int): String {
        val pc = PumlCode()
        val obj = logEntry.obj
        if (obj != null) {
            val ets = (obj as Property).extensions!!
            pc.addLine("friendlyName: " + ets.getFriendlyName())
            pc.addHorizontalLine()
            pc.add("descriptions: " + ets.getDescription())
            pc.toMindmapNode(level)
        }
        return pc.code
    }

}
