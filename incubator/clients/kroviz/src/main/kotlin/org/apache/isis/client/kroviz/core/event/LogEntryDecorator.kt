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
import org.apache.isis.client.kroviz.utils.Utils

class LogEntryDecorator(val logEntry: LogEntry) {

    val href: String
    val links: List<Link>
    val linked: List<LogEntry>

    init {
        href = logEntry.selfHref()
        links = logEntry.getLinks()
        linked = EventStore.getLinked()
    }

    fun findOrphans(children: Set<LogEntry>): Set<String> {
        console.log("[LED.findOrphans] $href")
        val kids = children.map { it.url }
        val orphans = mutableSetOf<String>()
        links.forEach {
            console.log(it)
            val rel = it.relation()
            when {
                (rel == Relation.UP) -> {
                }
                (rel == Relation.SELF) -> {
                }
                else -> {
                    val url = it.href
                    if (!kids.contains(url))
                        orphans.add(url)
                }
            }
        }
        return orphans
    }

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
        console.log("[LED.findChildrenByLinks] $href")
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

    fun selfType(): String {
        val selfLink = logEntry.selfLink()
        if (selfLink != null) {
            return selfLink.representation().type
        } else return ""
    }

    private fun hasUp(): Boolean {
        links.forEach {
            if (it.relation() == Relation.UP) {
                return true
            }
        }
        return false
    }

    fun hasParent(): Boolean {
        val answer = hasUp()
        if (answer) return true
        return findParent() != null
    }

    private fun findParent(): LogEntry? {
        val url = logEntry.url
        linked.forEach {
            when {
                it.selfHref() == url -> return null
                it.response.contains(url) -> return it
            }
        }
        return null
    }

    fun shortTitle(): String {
        var result = logEntry.url
        val signature = Constants.restInfix
        if (logEntry.url.contains(signature)) {
            // strip off protocol, host, port
            //           val protocolHostPort = UiManager.getUrl()
//            result = result.replace(protocolHostPort + signature, "")
            result = Utils.removeHexCode(result)
        }
        return result
    }

}
