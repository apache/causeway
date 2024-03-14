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
package org.apache.causeway.client.kroviz.ui.kv.override

/* (!) copied from io.kvision.window.Window in order to make
* Dialogs transparent on move
* add a dd menu to the icon
* */

import io.kvision.core.*
import io.kvision.dropdown.DropDown
import io.kvision.html.*
import io.kvision.modal.CloseIcon
import io.kvision.panel.SimplePanel
import io.kvision.snabbdom.VNode
import io.kvision.utils.obj
import io.kvision.utils.px
import io.kvision.window.MaximizeIcon
import io.kvision.window.MinimizeIcon
import kotlinx.browser.window
import org.w3c.dom.events.Event
import org.w3c.dom.events.MouseEvent
import io.kvision.html.Link as KvisionHtmlLink

internal const val DEFAULT_Z_INDEX = 900
internal const val WINDOW_HEADER_HEIGHT = 40
internal const val WINDOW_CONTENT_MARGIN_BOTTOM = 11

/**
 * Floating window container.
 *
 * @constructor
 * @param caption window title
 * @param contentWidth window content width
 * @param contentHeight window content height
 * @param isResizable determines if the window is resizable
 * @param isDraggable determines if the window is draggable
 * @param closeButton determines if Close button is visible
 * @param maximizeButton determines if Maximize button is visible
 * @param minimizeButton determines if Minimize button is visible
 * @param classes a set of CSS class names
 * @param init an initializer extension function
 */
@Suppress("TooManyFunctions")
@Deprecated("use Window, when transparency and icon menu work")
open class RoWindow(
        caption: String? = null,
        contentWidth: CssSize? = CssSize(0, UNIT.auto),
        contentHeight: CssSize? = CssSize(0, UNIT.auto),
        isResizable: Boolean = true,
        isDraggable: Boolean = true,
        closeButton: Boolean = false,
        maximizeButton: Boolean = true,
        minimizeButton: Boolean = true,
        icon: String? = null,
        menu: List<KvisionHtmlLink>? = null,
        init: (RoWindow.() -> Unit)? = null
) :
    SimplePanel() {

    init {
        this.addCssClass("modal-content")
        this.addCssClass("kv-window")
    }
    /**
     * Window caption text.
     */
    var caption
        get() = captionTag.content
        set(value) {
            captionTag.content = value
            checkHeaderVisibility()
        }

    /**
     * Window content width.
     */
    var contentWidth
        get() = width
        set(value) {
            width = value
        }

    /**
     * Window content height.
     */
    var contentHeight
        get() = content.height
        set(value) {
            content.height = value
        }

    /**
     * Window content height.
     */
    var contentOverflow
        get() = content.overflow
        set(value) {
            content.overflow = value
        }

    /**
     * Determines if the window is resizable.
     */
    var isResizable by refreshOnUpdate(isResizable) { checkIsResizable() }

    /**
     * Determines if the window is draggable.
     */
    var isDraggable by refreshOnUpdate(isDraggable) { checkIsDraggable(); checkHeaderVisibility() }

    /**
     * Determines if Close button is visible.
     */
    var closeButton
        get() = closeIcon.visible
        set(value) {
            closeIcon.visible = value
            checkHeaderVisibility()
        }

    /**
     * Determines if Maximize button is visible.
     */
    var maximizeButton
        get() = maximizeIcon.visible
        set(value) {
            maximizeIcon.visible = value
            checkHeaderVisibility()
        }

    /**
     * Determines if Maximize button is visible.
     */
    var minimizeButton
        get() = minimizeIcon.visible
        set(value) {
            minimizeIcon.visible = value
            checkHeaderVisibility()
        }

    /**
     * Window icon.
     */
    var icon
        get() = if (windowIcon.icon == "") null else windowIcon.icon
        set(value) {
            windowIcon.icon = value ?: ""
            windowIcon.visible = (value != null && value != "")
        }

    private val header = SimplePanel("modal-header")

    /**
     * @suppress
     * Internal property.
     */
    protected val content = SimplePanel().apply {
        this.height = contentHeight
        this.overflow = Overflow.AUTO
    }
    private val closeIcon = CloseIcon()
    private val maximizeIcon = MaximizeIcon()
    private val minimizeIcon = MinimizeIcon()
    private val captionTag = Tag(TAG.H5, caption).apply {
        addCssClass("modal-title")
        alignSelf = AlignItems.START
    }
    private val iconsContainer = SimplePanel("kv-window-icons-container")

    private val windowIcon = Icon(icon ?: "").apply {
        addCssClass("window-icon")
        visible = (icon != null && icon != "")
    }

    private val windowButton = DropDown(
            text = "",
            icon = windowIcon.icon,
            style = ButtonStyle.LIGHT).apply {
        marginLeft = CssSize(-16, UNIT.px)
        marginTop = CssSize(-1, UNIT.px)
        background = Background(color = Color.name(Col.WHITE))
    }


    private var isResizeEvent = false

    init {
        id = "kv_window_$counter"
        @Suppress("LeakingThis")
        position = Position.ABSOLUTE
        @Suppress("LeakingThis")
        overflow = Overflow.HIDDEN
        @Suppress("LeakingThis")
        width = contentWidth
        @Suppress("LeakingThis")
        zIndex = ++zIndexCounter
        if (menu == null) {
            header.add(windowIcon)
        } else {
            val windowButton = DropDown(
                    text = "",
                    icon = icon,
                    style = ButtonStyle.LIGHT).apply {
                marginLeft = CssSize(-16, UNIT.px)
                marginTop = CssSize(-1, UNIT.px)
//                background = Background(color = Color.name(Col.WHITE))
                addBsBgColor(BsBgColor.TRANSPARENT)
            }
            menu.forEach { m ->
                windowButton.add(m)
            }
            header.add(windowButton)
            windowButton.setEventListener<Icon> {
                click = { _ ->
                    console.log("[RoWindow.windowButton.click]")
                }
                mousedown = { e ->
                    e.stopPropagation()
                }
            }
        }
        header.add(captionTag)
        header.add(iconsContainer)
        minimizeIcon.visible = minimizeButton
        minimizeIcon.setEventListener<MinimizeIcon> {
            click = { _ ->
                @Suppress("UnsafeCastFromDynamic")
                if (this@RoWindow.dispatchEvent("minimizeWindow", obj {}) != false) {
                    toggleMinimize()
                }
            }
            mousedown = { e ->
                e.stopPropagation()
            }
        }
        iconsContainer.add(minimizeIcon)
        maximizeIcon.visible = maximizeButton
        maximizeIcon.setEventListener<MaximizeIcon> {
            click = { _ ->
                @Suppress("UnsafeCastFromDynamic")
                if (this@RoWindow.dispatchEvent("maximizeWindow", obj {}) != false) {
                    toggleMaximize()
                }
            }
            mousedown = { e ->
                e.stopPropagation()
            }
        }
        iconsContainer.add(maximizeIcon)
        closeIcon.visible = closeButton
        closeIcon.setEventListener<CloseIcon> {
            click = { _ ->
                @Suppress("UnsafeCastFromDynamic")
                if (this@RoWindow.dispatchEvent("closeWindow", obj {}) != false) {
                    close()
                }
            }
            mousedown = { e ->
                e.stopPropagation()
            }
        }
        iconsContainer.add(closeIcon)
        checkHeaderVisibility()
        addInternal(header)
        addInternal(content)
        checkIsDraggable()
        if (isResizable) {
            @Suppress("LeakingThis")
            resize = Resize.BOTH
            content.marginBottom = WINDOW_CONTENT_MARGIN_BOTTOM.px
        }
        @Suppress("LeakingThis")
        setEventListener<RoWindow> {
            click = {
                toFront()
                focus()
            }
        }
        @Suppress("LeakingThis")
        init?.invoke(this)
        counter++
    }

    private fun checkHeaderVisibility() {
        @Suppress("ComplexCondition")
        if (!closeButton && !maximizeButton && !minimizeButton && caption == null && !isDraggable) {
            header.hide()
        } else {
            header.show()
        }
    }

    open fun checkIsDraggable() {
        var isDrag: Boolean
        if (isDraggable) {
            header.setEventListener<SimplePanel> {
                mousedown = { e ->
                    if (e.button.toInt() == 0) {
                        isDrag = true
                        val dragStartX = this@RoWindow.getElementJQuery()?.position()?.left?.toInt() ?: 0
                        val dragStartY = this@RoWindow.getElementJQuery()?.position()?.top?.toInt() ?: 0
                        val dragMouseX = e.pageX
                        val dragMouseY = e.pageY
                        val moveCallback = { me: Event ->
                            if (isDrag) {
                                setOpacity("0.3")
                                this@RoWindow.left = (dragStartX + (me as MouseEvent).pageX - dragMouseX).toInt().px
                                this@RoWindow.top = (dragStartY + (me).pageY - dragMouseY).toInt().px
                            }
                        }
                        window.addEventListener("mousemove", moveCallback)
                        var upCallback: ((Event) -> Unit)? = null
                        upCallback = {
                            isDrag = false
                            setOpacity("1.0")
                            window.removeEventListener("mousemove", moveCallback)
                            window.removeEventListener("mouseup", upCallback)
                        }
                        window.addEventListener("mouseup", upCallback)
                    }
                }
            }
        } else {
            isDrag = false
            header.removeEventListeners()
        }
    }

    private fun setOpacity(value: String) {
        val opacity = value.toDouble()
        this@RoWindow.getElementJQuery()?.css(
                "background-color",
                "rgba(255, 255, 255, $opacity)"
        )
    }

    private fun checkIsResizable() {
        checkResizablEventHandler()
        if (isResizable) {
            resize = Resize.BOTH
            val intHeight = (getElementJQuery()?.height()?.toInt() ?: 0)
            content.height = (intHeight - WINDOW_HEADER_HEIGHT - WINDOW_CONTENT_MARGIN_BOTTOM).px
            content.marginBottom = WINDOW_CONTENT_MARGIN_BOTTOM.px
        } else {
            resize = Resize.NONE
            val intHeight = (getElementJQuery()?.height()?.toInt() ?: 0)
            content.height = (intHeight - WINDOW_HEADER_HEIGHT).px
            content.marginBottom = 0.px
        }
    }

    @Suppress("UnsafeCastFromDynamic")
    private fun checkResizablEventHandler() {
        if (isResizable) {
            if (!isResizeEvent) {
                isResizeEvent = true
                RoManagerBootstrap.setResizeEvent(this) {
                    val eid = getElementJQuery()?.attr("id")
                    if (isResizable && eid == id) {
                        val outerWidth = (getElementJQuery()?.outerWidth()?.toInt() ?: 0)
                        val outerHeight = (getElementJQuery()?.outerHeight()?.toInt() ?: 0)
                        val intWidth = (getElementJQuery()?.width()?.toInt() ?: 0)
                        val intHeight = (getElementJQuery()?.height()?.toInt() ?: 0)
                        content.width = intWidth.px
                        content.height = (intHeight - WINDOW_HEADER_HEIGHT - WINDOW_CONTENT_MARGIN_BOTTOM).px
                        width = outerWidth.px
                        height = outerHeight.px
                        this.dispatchEvent("resizeWindow", obj {
                            detail = obj {
                                this.width = outerWidth
                                this.height = outerHeight
                            }
                        })
                    }
                }
            }
        } else if (isResizeEvent) {
            RoManagerBootstrap.clearResizeEvent(this)
            isResizeEvent = false
        }
    }

    override fun add(child: Component): SimplePanel {
        content.add(child)
        return this
    }

    override fun addAll(children: List<Component>): SimplePanel {
        content.addAll(children)
        return this
    }

    override fun remove(child: Component): SimplePanel {
        content.remove(child)
        return this
    }

    override fun removeAll(): SimplePanel {
        content.removeAll()
        return this
    }

    override fun getChildren(): List<Component> {
        return content.getChildren()
    }

    override fun afterCreate(node: VNode) {
        checkResizablEventHandler()
    }

    override fun afterDestroy() {
        if (isResizeEvent) {
            RoManagerBootstrap.clearResizeEvent(this)
            isResizeEvent = false
        }
    }

    /**
     * Moves the current window to the front.
     */
    open fun toFront() {
        if ((zIndex ?: 0) < zIndexCounter) zIndex = ++zIndexCounter
    }

    /**
     * Makes the current window focused.
     */
    override fun focus() {
        getElementJQuery()?.focus()
    }

    /**
     * Close the window.
     */
    open fun close() {
        hide()
    }

    /**
     * Maximize or restore the window size.
     */
    open fun toggleMaximize() {
    }

    /**
     * Minimize or restore the window size.
     */
    open fun toggleMinimize() {
    }

    companion object {
        internal var counter = 0
        internal var zIndexCounter = DEFAULT_Z_INDEX
    }

}

