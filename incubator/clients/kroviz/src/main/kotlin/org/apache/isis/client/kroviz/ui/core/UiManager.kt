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
package org.apache.isis.client.kroviz.ui.core

import io.kvision.core.CssSize
import io.kvision.core.UNIT
import io.kvision.core.Widget
import io.kvision.dropdown.ContextMenu
import io.kvision.panel.SimplePanel
import io.kvision.utils.ESC_KEY
import kotlinx.browser.document
import kotlinx.browser.window
import org.apache.isis.client.kroviz.App
import org.apache.isis.client.kroviz.core.Session
import org.apache.isis.client.kroviz.core.aggregator.BaseAggregator
import org.apache.isis.client.kroviz.core.aggregator.ObjectAggregator
import org.apache.isis.client.kroviz.core.aggregator.UndefinedDispatcher
import org.apache.isis.client.kroviz.core.event.EventStore
import org.apache.isis.client.kroviz.core.event.LogEntry
import org.apache.isis.client.kroviz.core.model.CollectionDM
import org.apache.isis.client.kroviz.core.model.ObjectDM
import org.apache.isis.client.kroviz.to.TObject
import org.apache.isis.client.kroviz.to.ValueType
import org.apache.isis.client.kroviz.to.mb.Menubars
import org.apache.isis.client.kroviz.ui.builder.RoDisplay
import org.apache.isis.client.kroviz.ui.kv.override.RoTab
import org.apache.isis.client.kroviz.utils.*
import org.w3c.dom.events.KeyboardEvent

/**
 * Single point of contact for view components consisting of:
 * @item RoMenubar,
 * @item RoView (tabs, etc.),
 * @item RoStatusbar,
 */
object UiManager {

    var app: App? = null
    private val popups = mutableListOf<Widget>()
    private val settings = mutableMapOf<String, Any>()
    var position: Point? = null

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

    private fun getRoApp(): RoApp {
        return app!!.roApp!!
    }

    private fun getRoView(): RoView {
        return getRoApp().roView
    }

    fun getRoIconBar(): RoIconBar {
        return getRoApp().roIconBar
    }

    fun getRoStatusBar(): RoStatusBar {
        return getRoApp().roStatusBar
    }

    private fun activeObject(): ObjectDM? {
        val activeTab = getRoView().findActive()
        if (activeTab != null) {
            return (activeTab as RoDisplay).displayModel
        }
        return null
    }

    fun add(title: String, panel: SimplePanel, aggregator: BaseAggregator = UndefinedDispatcher()) {
        getRoView().addTab(title, panel)
        getEventStore().addView(title, aggregator, panel)
    }

    fun remove(panel: SimplePanel) {
        getRoView().removeTab(panel)
    }

    /**
     * SVG code added to Tabs disappears after a refresh, therefore an SVG object (code, uuid)
     * is added as attribute to the tab in order to be able to recreate it on refresh.
     */
    fun addSvg(title: String, svgCode: String) {
        fun buildSvgPanel(uuid: UUID): FormPanelFactory {
            val formItems = mutableListOf<FormItem>()
            val newFi = FormItem("svg", ValueType.SVG_INLINE, callBack = uuid)
            formItems.add(newFi)
            return FormPanelFactory(formItems)
        }

        val uuid = UUID()
        DomUtil.appendTo(uuid, svgCode)

        val panel = buildSvgPanel(uuid)
        getRoView().addTab(title, panel)
        val tab = getRoView().findActive()!! as RoTab

        val svg = ScalableVectorGraphic(svgCode, uuid)
        tab.svg = svg

        val aggregator: BaseAggregator = UndefinedDispatcher()
        getEventStore().addView(title, aggregator, panel)
    }

    fun closeView(tab: SimplePanel) {
        val tt = tab.title
        if (tt != null) {
            getEventStore().closeView(tt)
        }
    }

    fun amendMenu(menuBars: Menubars) {
        getRoApp().roMenuBar.amendMenu(menuBars)
        setNormalCursor()
    }

    fun updateStatus(entry: LogEntry) {
        getRoStatusBar().update(entry)
    }

    fun updateSession(user: String, session: Session, isFirstSession: Boolean) {
        getRoStatusBar().updateUser(user)
        val menubar = getRoApp().roMenuBar
        if (isFirstSession) menubar.addSeparatorToMainMenu()
        menubar.add(session)
    }

    fun switchSession(session: Session) {
        getRoApp().roMenuBar.switch(session)
    }

    fun setBusyCursor() {
        document.body?.style?.cursor = "progress"
    }

    fun setNormalCursor() {
        document.body?.style?.cursor = "default"
    }

    fun openCollectionView(aggregator: BaseAggregator) {
        val displayable = aggregator.dpm
        val title: String = StringUtils.extractTitle(displayable.title)
        val panel = RoTable(displayable as CollectionDM)
        add(title, panel, aggregator)
        displayable.isRendered = true
        setNormalCursor()
    }

    fun openObjectView(aggregator: ObjectAggregator) {
        val dm = aggregator.dpm as ObjectDM
        var title: String = StringUtils.extractTitle(dm.title)
        if (title.isEmpty()) {
            title = aggregator.actionTitle
        }
        val panel = RoDisplay(dm)
        add(title, panel, aggregator)
        dm.isRendered = true
        setNormalCursor()
    }

    fun openDialog(panel: RoDialog, at: Point = Point(100, 100)) {
        val offset = getNumberOfPopups() * 4
        panel.left = CssSize(at.x + offset, UNIT.px)
        panel.top = CssSize(at.y + offset, UNIT.px)
        getRoApp().add(panel)
        push(panel)
    }

    fun closeDialog(panel: RoDialog) {
        getRoApp().remove(panel)
        pop()
    }

    fun loadDomainTypes(): Boolean {
        val k = "loadDomainTypes"
        return when {
            settings.containsKey(k) -> settings.getValue(k) as Boolean
            else -> false
        }
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

    private fun getNumberOfPopups(): Int {
        return popups.size
    }

    fun performUserAction(aggregator: BaseAggregator, obj: TObject) {
        setBusyCursor()
        getEventStore().addUserAction(aggregator, obj)
        setNormalCursor()
    }

    private fun getEventStore(): EventStore {
        return SessionManager.getEventStore()
    }

}
