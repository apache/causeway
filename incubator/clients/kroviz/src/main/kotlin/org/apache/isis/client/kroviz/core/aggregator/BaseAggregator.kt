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
package org.apache.isis.client.kroviz.core.aggregator

import org.apache.isis.client.kroviz.core.event.LogEntry
import org.apache.isis.client.kroviz.core.event.ResourceProxy
import org.apache.isis.client.kroviz.core.model.BaseDisplayModel
import org.apache.isis.client.kroviz.to.Link
import org.apache.isis.client.kroviz.to.TObject
import org.apache.isis.client.kroviz.ui.core.Constants
import org.apache.isis.client.kroviz.utils.UrlUtils

/**
 * An Aggregator:
 * @item is initially created in ResponseHandlers, displayModels, Menus
 * @item is assigned to at least one LogEntry,
 * @item is passed on to related LogEntries (eg. siblings in a list, Layout),
 * @item is notified about changes to related LogEntries,
 * @item invokes subsequent links, and
 * @item triggers creation a view for an object or a list.
 *
 * @see: https://www.enterpriseintegrationpatterns.com/patterns/messaging/Aggregator.html
 *
 * Could be named collector or assembler as well.
 */
abstract class BaseAggregator {

    open lateinit var dpm: BaseDisplayModel

    open fun update(logEntry: LogEntry, subType: String) {
        /* default is do nothing - can be overridden in subclasses */
    }

    open fun reset(): BaseAggregator {
        /* do nothing and */ return this
    }

    open fun getObject(): TObject? {
        return null
    }

    protected fun log(logEntry: LogEntry) {
        logEntry.setUndefined("no handler found")
        console.log("[BaseAggregator.log] ")
        console.log(logEntry)
        val className = this::class.simpleName
        throw Throwable("no handler found: $className")
    }

    fun TObject.getLayoutLink(): Link? {
        return links.firstOrNull { it.isLayout() }
    }

    fun TObject.getIconLink(): Link? {
        return links.firstOrNull { it.isIcon() }
    }

    override fun toString(): String {
        return "[${this::class} \n" +
                "TObject: ${this.getObject()} ]\n"
    }

    private fun Link.isLayout(): Boolean {
        return href.isNotEmpty() && UrlUtils.isLayout(href)
    }

    private fun Link.isIcon(): Boolean {
        return href.isNotEmpty() && UrlUtils.isObjectIcon(href)
    }

    protected fun invoke(
            link: Link,
            aggregator: BaseAggregator,
            subType: String = Constants.subTypeJson,
            referrer: String) {
        ResourceProxy().fetch(link, aggregator, subType, referrer = referrer)
    }

}
