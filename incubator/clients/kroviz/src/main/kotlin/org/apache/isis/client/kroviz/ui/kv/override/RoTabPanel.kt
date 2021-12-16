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
package org.apache.isis.client.kroviz.ui.kv.override

/**
 * Copied from TabPanel in order to:
 * * add IconMenu to (Ro)Tab
 */

import com.github.snabbdom.VNode
import io.kvision.core.*
import io.kvision.panel.SimplePanel
import io.kvision.panel.VPanel
import io.kvision.routing.RoutingManager
import io.kvision.utils.auto
import io.kvision.utils.obj
import org.apache.isis.client.kroviz.ui.core.ViewManager

/**
 * Tab position.
 */
enum class TabPosition {
    TOP,
    LEFT,
    RIGHT
}

/**
 * Left or right tab size.
 */
enum class SideTabSize {
    SIZE_1,
    SIZE_2,
    SIZE_3,
    SIZE_4,
    SIZE_5,
    SIZE_6
}

/**
 * The container rendering its children as tabs.
 *
 * It supports activating children by a JavaScript route.
 *
 * @constructor
 * @param tabPosition tab position
 * @param sideTabSize side tab size
 * @param scrollableTabs determines if tabs are scrollable (default: false)
 * @param draggableTabs determines if tabs are draggable (default: false)
 * @param className CSS class names
 * @param init an initializer extension function
 */
@Suppress("LeakingThis")
@Deprecated("remove when icon menu works with TabPanel")
open class RoTabPanel(
    protected val tabPosition: TabPosition = TabPosition.TOP,
    protected val sideTabSize: SideTabSize = SideTabSize.SIZE_3,
    val scrollableTabs: Boolean = true,
    val draggableTabs: Boolean = false,
    className: String? = null,
    init: (RoTabPanel.() -> Unit)? = null
) : SimplePanel((className?.let { "$it " } ?: "") + "kv-tab-panel") {

    protected val navClasses = when (tabPosition) {
        TabPosition.TOP -> if (scrollableTabs) "nav nav-tabs tabs-top" else "nav nav-tabs"
        TabPosition.LEFT -> "nav nav-tabs tabs-left flex-column"
        TabPosition.RIGHT -> "nav nav-tabs tabs-right flex-column"
    }

    internal val tabs = mutableListOf<RoTab>()

    private val nav = TabPanelNav(this, navClasses)
    private val content = TabPanelContent(this)

    /**
     * The index of the active tab.
     */
    var activeIndex: Int = -1
        set(value) {
            if (value >= -1 && value < tabs.size) {
                field = value
                tabs.forEach {
                    it.link.removeCssClass("active")
                }
                tabs.getOrNull(value)?.link?.addCssClass("active")
                @Suppress("UnsafeCastFromDynamic")
                this.dispatchEvent("changeTab", obj { detail = obj { data = value } })
            }
        }

    /**
     * The active tab.
     */
    var activeTab: RoTab?
        get() = tabs.getOrNull(activeIndex)
        set(value) {
            activeIndex = value?.let { tabs.indexOf(value) } ?: -1
        }

    init {
        //TODO to be set by caller
        width = auto
        marginTop = CssSize(40, UNIT.px)
        when (tabPosition) {
            TabPosition.TOP -> {
                this.addPrivate(nav)
                this.addPrivate(content)
            }
            TabPosition.LEFT -> {
                this.addSurroundingCssClass("container-fluid")
                this.addCssClass("row")
                val sizes = calculateSideClasses()
                this.addPrivate(WidgetWrapper(nav, "${sizes.first} ps-0 pe-0"))
                this.addPrivate(WidgetWrapper(content, "${sizes.second} ps-0 pe-0"))
            }
            TabPosition.RIGHT -> {
                this.addSurroundingCssClass("container-fluid")
                this.addCssClass("row")
                val sizes = calculateSideClasses()
                this.addPrivate(WidgetWrapper(content, "${sizes.second} ps-0 pe-0"))
                this.addPrivate(WidgetWrapper(nav, "${sizes.first} ps-0 pe-0"))
            }
        }
        init?.invoke(this)
    }

    protected fun calculateSideClasses(): Pair<String, String> {
        return when (sideTabSize) {
            SideTabSize.SIZE_1 -> Pair("col-sm-1", "col-sm-11")
            SideTabSize.SIZE_2 -> Pair("col-sm-2", "col-sm-10")
            SideTabSize.SIZE_3 -> Pair("col-sm-3", "col-sm-9")
            SideTabSize.SIZE_4 -> Pair("col-sm-4", "col-sm-8")
            SideTabSize.SIZE_5 -> Pair("col-sm-5", "col-sm-7")
            SideTabSize.SIZE_6 -> Pair("col-sm-6", "col-sm-6")
        }
    }

    /**
     * Returns the number of tabs.
     */
    open fun getSize(): Int {
        return tabs.size
    }

    /**
     * Returns the list of tabs.
     */
    open fun getTabs(): List<RoTab> {
        return tabs
    }

    /**
     * Get the Tab component by index.
     * @param index the index of a Tab
     */
    open fun getTab(index: Int): RoTab? {
        return tabs.getOrNull(index)
    }

    /**
     * Get the index of the given tab.
     * @param tab a Tab component
     */
    open fun getTabIndex(tab: RoTab): Int {
        return tabs.indexOf(tab)
    }

    /**
     * Removes tab at given index.
     * @param index the index of the tab
     */
    open fun removeTab(index: Int): RoTabPanel {
        val tab = getTabs().get(index)
        ViewManager.remove(tab as SimplePanel)

        getTab(index)?.let {
            removeTab(it)
            refresh()
        }
        return this
    }

    /**
     * Find the tab which contains the given component.
     * @param component a component
     */
    open fun findTabWithComponent(component: Component): RoTab? {
        return tabs.find { it.getChildren().contains(component) }
    }

    /**
     * Move the tab to a different position.
     * @param fromIndex source tab index
     * @param toIndex destination tab index
     */
    open fun moveTab(fromIndex: Int, toIndex: Int) {
        tabs.getOrNull(fromIndex)?.let {
            tabs.remove(it)
            tabs.add(toIndex, it)
            if (activeIndex == fromIndex) {
                activeIndex = toIndex
            } else if (activeIndex in (fromIndex + 1)..toIndex) {
                activeIndex--
            } else if (activeIndex in toIndex until fromIndex) {
                activeIndex++
            }
            refresh()
        }
    }

    /**
     * Add new Tab component.
     * @param tab a Tab component
     * @param position tab position
     */
    protected open fun addTab(tab: RoTab, position: Int? = null) {
        tab.parent = nav
        if (position == null) {
            tabs.add(tab)
        } else {
            tabs.add(position, tab)
        }
        if (tabs.size == 1) {
            tab.link.addCssClass("active")
            activeIndex = 0
        }
        if (draggableTabs) {
            tab.setDragDropData("text/plain", tab.tabId.toString())
            tab.setDropTargetData("text/plain") { data ->
                val toIdx = getTabIndex(tab)
                data?.toIntOrNull()?.let { tabId ->
                    tabs.find { it.tabId == tabId }?.let {
                        val fromIdx = getTabIndex(it)
                        moveTab(fromIdx, toIdx)
                    }
                }
            }
        }
        if (tab.route != null) {
            RoutingManager.getRouter().kvResolve()
        }
    }

    /**
     * Add new child component.
     * @param child a child component
     * @param position tab position
     */
    protected open fun addChild(child: Component, position: Int? = null) {
        if (child is RoTab) {
            addTab(child, position)
        } else {
            addTab(RoTab("", child), position)
        }
    }

    /**
     * Delete the given Tab component.
     * @param tab a Tab component
     */
    protected open fun removeTab(tab: RoTab) {
        val index = tabs.indexOf(tab)
        if (index >= 0) {
            tabs.remove(tab)
            tab.parent = null
            if (activeIndex >= tabs.size) {
                activeIndex = tabs.size - 1
            } else if (activeIndex > index) {
                activeIndex--
            } else if (activeIndex == index) {
                activeIndex = activeIndex
            }
        }
    }

    override fun add(child: Component): RoTabPanel {
        addChild(child)
        refresh()
        return this
    }

    override fun add(position: Int, child: Component): RoTabPanel {
        addChild(child, position)
        refresh()
        return this
    }

    /**
     * Creates and adds new tab component.
     * @param title title of the tab
     * @param panel child component
     * @param icon icon of the tab
     * @param image image of the tab
     * @param closable determines if this tab is closable
     * @param route JavaScript route to activate given child
     * @return current container
     */
    open fun addTab(
        title: String, panel: Component, icon: String? = null,
        image: ResString? = null, closable: Boolean = false, route: String? = null
    ): RoTabPanel {
        addTab(RoTab(title, panel, icon, image, closable, route))
        refresh()
        return this
    }

    override fun addAll(children: List<Component>): RoTabPanel {
        children.forEach(::addChild)
        refresh()
        return this
    }

    override fun remove(child: Component): RoTabPanel {
        if (child is RoTab) {
            removeTab(child)
            refresh()
        } else {
            findTabWithComponent(child)?.let {
                removeTab(it)
                refresh()
            }
        }
        return this
    }

    override fun removeAt(position: Int): RoTabPanel {
        if (position >= 0 && position < tabs.size) {
            val tab = tabs.removeAt(position)
            tab.parent = null
            if (activeIndex >= tabs.size) {
                activeIndex = tabs.size - 1
            } else if (activeIndex > position) {
                activeIndex--
            } else if (activeIndex == position) {
                activeIndex = activeIndex
            }
        }
        return this
    }

    override fun removeAll(): RoTabPanel {
        tabs.forEach { removeTab(it) }
        return this
    }

    override fun disposeAll(): RoTabPanel {
        tabs.forEach { it.dispose() }
        removeAll()
        return this
    }

    fun findTab(title: String): Int? {
        getTabs().forEachIndexed { index, component ->
            if ((component is VPanel) && (component.title == title)) {
                return index
            }
        }
        return null
    }

    /**
     * A helper component for rendering tabs.
     */
    class TabPanelNav(internal val tabPanel: RoTabPanel, className: String) : SimplePanel(className) {

        override fun render(): VNode {
            return render("ul", childrenVNodes())
        }

        override fun childrenVNodes(): Array<VNode> {
            return tabPanel.tabs.filter { it.visible }.map { it.renderVNode() }.toTypedArray()
        }

    }

    /**
     * A helper component for rendering tab content.
     */
    class TabPanelContent(private val tabPanel: RoTabPanel) : SimplePanel() {

        override fun childrenVNodes(): Array<VNode> {
            return tabPanel.tabs.getOrNull(tabPanel.activeIndex)?.getChildren()?.map { it.renderVNode() }
                ?.toTypedArray()
                ?: emptyArray()
        }

    }
}

/**
 * DSL builder extension function.
 *
 * It takes the same parameters as the constructor of the built component.
 */
fun Container.tabPanel(
    tabPosition: TabPosition = TabPosition.TOP,
    sideTabSize: SideTabSize = SideTabSize.SIZE_3,
    scrollableTabs: Boolean = false,
    draggableTabs: Boolean = false,
    className: String? = null,
    init: (RoTabPanel.() -> Unit)? = null
): RoTabPanel {
    val tabPanel = RoTabPanel(tabPosition, sideTabSize, scrollableTabs, draggableTabs, className, init)
    this.add(tabPanel)
    return tabPanel
}
