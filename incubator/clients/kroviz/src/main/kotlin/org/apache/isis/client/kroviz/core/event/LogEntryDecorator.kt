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
package org.apache.isis.client.kroviz.core.event

import org.apache.isis.client.kroviz.to.Link
import org.apache.isis.client.kroviz.to.Relation
import org.apache.isis.client.kroviz.ui.core.Constants

class LogEntryDecorator(val logEntry: LogEntry) {

    val href: String = logEntry.selfHref()
    val links: List<Link> = logEntry.getLinks()
    val linked: List<LogEntry> = EventStore.getLinked()

    fun findChildren(): Set<LogEntry> {
        val children = findChildrenByUpRelation()
        children.plus(findChildrenByReference())
        children.plus(findChildrenByLinks())
        return children
    }

    private fun findChildrenByUpRelation(): Set<LogEntry> {
        val children = mutableSetOf<LogEntry>()
        linked.forEach {
            it.getLinks().forEach { l ->
                if ((l.relation() == Relation.UP) && (l.href == href)) {
                    children.add(it)
                }
            }
        }
        return children
    }

    private fun findChildrenByLinks(): Set<LogEntry> {
        console.log("[LED.findChildrenByLinks]")
        val children = mutableSetOf<LogEntry>()
        links.forEach {
            console.log(it.toString())
            val rel = it.relation()
            when {
                (rel == Relation.UP) -> {
                }
                (rel == Relation.SELF) -> {
                }
                else -> {
                    val rsj = ResourceSpecification(it.href, Constants.subTypeJson)
                    var le = EventStore.findBy(rsj)
                    if (le == null) {
                        val rsx = ResourceSpecification(it.href, Constants.subTypeXml)
                        le = EventStore.findBy(rsx)
                    }
                    console.log(le.toString())
                    if (le != null) children.add(le)
                }
            }
        }
        return children
    }

    private fun findChildrenByReference(): Set<LogEntry> {
        val str = logEntry.response
        val children = mutableSetOf<LogEntry>()
        linked.forEach {
            if (it != logEntry && str.contains(it.url)) {
                children.add(it)
            }
        }
        return children
    }

    fun findChildrenIn(aggregatedList: List<LogEntry>): List<LogEntry> {
        console.log("[LED.findChildrenIn]")
        val selfUrl = href
        val children = mutableListOf<LogEntry>()
        aggregatedList.forEach {
            if (it.url != selfUrl && it.response.contains(selfUrl)) {
                children.add(it)
            }
        }
        return children
    }

    fun selfType(): String {
        val selfLink = logEntry.selfLink()
        return if (selfLink != null) {
            selfLink.representation().type
        } else {
            console.log("[LED.selfType]")
            console.log(logEntry)
            ""
        }
    }

    fun findParent(): LogEntry? {
        val url = logEntry.url
        linked.forEach {
            when {
                it.selfHref() == url -> return null
                it.response.contains(url) -> return it
            }
        }
        return null
    }

}
