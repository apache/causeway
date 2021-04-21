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
package org.apache.isis.client.kroviz.ui.kv

import kotlinx.browser.window
import org.apache.isis.client.kroviz.core.Session
import org.apache.isis.client.kroviz.core.aggregator.BaseAggregator
import org.apache.isis.client.kroviz.core.aggregator.ObjectAggregator
import org.apache.isis.client.kroviz.core.aggregator.UndefinedDispatcher
import org.apache.isis.client.kroviz.core.event.EventStore
import org.apache.isis.client.kroviz.core.event.LogEntry
import org.apache.isis.client.kroviz.core.event.ResourceSpecification
import org.apache.isis.client.kroviz.core.model.ListDM
import org.apache.isis.client.kroviz.core.model.ObjectDM
import org.apache.isis.client.kroviz.to.Relation
import org.apache.isis.client.kroviz.to.TObject
import org.apache.isis.client.kroviz.to.mb.Menubars
import org.apache.isis.client.kroviz.utils.Utils
import org.w3c.dom.events.KeyboardEvent
import io.kvision.core.Widget
import io.kvision.dropdown.ContextMenu
import io.kvision.panel.SimplePanel
import io.kvision.utils.ESC_KEY

/**
 * Single point of contact for view components consisting of:
 * @item RoMenubar,
 * @item RoView (tabs, etc.),
 * @item RoStatusbar,
 * @item Session
 */
object UiManager {

    private var session: Session? = null
    private val popups = mutableListOf<Widget>()
    private val settings = mutableMapOf<String,Any>()

    init {
        window.addEventListener("keydown", fun(event) {
            event.stopPropagation()
            val ke = event as KeyboardEvent
            if (ke.keyCode == ESC_KEY) {
                pop()
            }
            if (ke.ctrlKey && ke.keyCode == 83) { // S
                activeObject()?.save()
                event.preventDefault()
            }
            if (ke.ctrlKey && ke.keyCode == 90) { // Z
                activeObject()?.undo()
            }
        })
    }

    private fun activeObject(): ObjectDM? {
        val activeTab = RoView.findActive()
        if (activeTab != null) {
            return (activeTab as RoDisplay).displayModel
        }
        return null
    }

    fun add(title: String, panel: SimplePanel, aggregator: BaseAggregator = UndefinedDispatcher()) {
        RoView.addTab(title, panel)
        EventStore.addView(title, aggregator, panel)
    }

    fun closeView(tab: SimplePanel) {
        val tt = tab.title
        if (tt != null) {
            EventStore.closeView(tt)
        }
    }

    fun amendMenu(menuBars: Menubars) {
        RoMenuBar.amendMenu(menuBars)
    }

    fun updateStatus(entry: LogEntry) {
        RoStatusBar.update(entry)
    }

    fun updateUser(user: String) {
        RoStatusBar.updateUser(user)
    }

    fun openListView(aggregator: BaseAggregator) {
        val displayable = aggregator.dpm
        val title: String = Utils.extractTitle(displayable.title)
        val panel = RoTable(displayable as ListDM)
        add(title, panel, aggregator)
        displayable.isRendered = true
    }

    fun openObjectView(aggregator: ObjectAggregator) {
        val dm = aggregator.dpm as ObjectDM
        var title: String = Utils.extractTitle(dm.title)
        if (title.isEmpty()) {
            title = aggregator.actionTitle
        }
        val panel = RoDisplay(dm)
        add(title, panel, aggregator)
        dm.isRendered = true
    }

    fun displayModel(tObject: TObject) {
        val aggregator = ObjectAggregator(tObject.title)
        linkLayout(tObject, aggregator)
        val logEntry = EventStore.find(tObject)!!
        logEntry.addAggregator(aggregator)
        aggregator.update(logEntry, Constants.subTypeJson)
        aggregator.handleObject(tObject)
    }

    private fun linkLayout(tObject: TObject, aggregator: ObjectAggregator) {
        val layoutLink = tObject.links.firstOrNull {
            it.relation() == Relation.OBJECT_LAYOUT
        }
        val reSpec = ResourceSpecification(layoutLink!!.href)
        val logEntry = EventStore.find(reSpec)
        logEntry!!.addAggregator(aggregator)
    }

    fun openDialog(panel: RoDialog) {
        RoApp.add(panel)
        push(panel)
    }

    fun closeDialog(panel: RoDialog) {
        RoApp.remove(panel)
        pop()
    }

    fun getUrl(): String {
        return when (session) {
            null -> ""
            else -> session!!.url
        }
    }

    fun loadDomainTypes(): Boolean {
        val k = "loadDomainTypes"
        return when {
            settings.containsKey(k) -> settings.getValue(k) as Boolean
            else -> false
        }
    }

    fun login(url: String, username: String, password: String) {
        session = Session()
        session!!.login(url, username, password)
    }

    fun getCredentials(): String {
        return session!!.getCredentials()
    }

    private fun push(widget: Widget) {
        popups.add(widget)
    }

    private fun pop() {
        val len = popups.size
        if (len > 0) {
            when (val widget = popups[len - 1]) {
                is RoDialog -> widget.close()
                is ContextMenu -> {
                    widget.hide()
                    widget.dispose()
                }
            }
            popups.removeAt(len - 1)
        }
    }

}
