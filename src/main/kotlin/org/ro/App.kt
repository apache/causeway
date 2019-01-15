package org.ro

import pl.treksoft.kvision.core.Border
import pl.treksoft.kvision.core.BorderStyle
import pl.treksoft.kvision.core.Component
import pl.treksoft.kvision.dropdown.DropDown.Companion.dropDown
import pl.treksoft.kvision.dropdown.Separator.Companion.separator
import pl.treksoft.kvision.hmr.ApplicationBase
import pl.treksoft.kvision.html.Link.Companion.link
import pl.treksoft.kvision.html.TAG
import pl.treksoft.kvision.html.Tag
import pl.treksoft.kvision.modal.Alert
import pl.treksoft.kvision.navbar.Nav
import pl.treksoft.kvision.navbar.Nav.Companion.nav
import pl.treksoft.kvision.navbar.Navbar.Companion.navbar
import pl.treksoft.kvision.navbar.NavbarType
import pl.treksoft.kvision.panel.FlexDir
import pl.treksoft.kvision.panel.FlexPanel.Companion.flexPanel
import pl.treksoft.kvision.panel.FlexWrap
import pl.treksoft.kvision.panel.Root
import pl.treksoft.kvision.require
import pl.treksoft.kvision.utils.px
import pl.treksoft.kvision.utils.vh
import pl.treksoft.kvision.window.Window
import kotlin.browser.document

object App : ApplicationBase {

    private lateinit var root: Root

    override fun start(state: Map<String, Any>) {
        root = Root("kvapp") {
            navbar(type = NavbarType.FIXEDTOP) {
                nav {
                    dropDown("Menu", icon = "fa-windows", forNavbar = true, withCaret = false) {
                        link("Calculator", icon = "fa-calculator").onClick {
                            Calculator.run(this@Root)
                        }
                        link("Paint", icon = "fa-paint-brush").onClick {
                            Paint.run(this@Root)
                        }
                        link("Web Browser", icon = "fa-firefox").onClick {
                            WebBrowser.run(this@Root)
                        }
                        separator()
                        link("About", icon = "fa-info-circle").onClick {
                            Alert.show("KVision Desktop", "KVision example application.")
                        }
                        link("Shutdown", icon = "fa-power-off").onClick {
                            document.location?.reload()
                        }
                    }
                }
                taskBar = nav()
            }
            flexPanel(FlexDir.COLUMN, FlexWrap.WRAP, spacing = 20) {
                padding = 20.px
                paddingTop = 70.px
                height = 100.vh
                add(DesktopIcon("fa-calculator", "Calculator").setEventListener<DesktopIcon> {
                    dblclick = {
                        Calculator.run(this@Root)
                    }
                })
                add(DesktopIcon("fa-paint-brush", "Paint").setEventListener<DesktopIcon> {
                    dblclick = {
                        Paint.run(this@Root)
                    }
                })
                add(DesktopIcon("fa-firefox", "Web Browser").setEventListener<DesktopIcon> {
                    dblclick = {
                        WebBrowser.run(this@Root)
                    }
                })
            }
        }
    }

    override fun dispose(): Map<String, Any> {
        root.dispose()
        return mapOf()
    }

    val css = require("./css/kvapp.css")

    lateinit var taskBar: Nav

    fun addTask(label: String, window: Window): Component {
        val task = Tag(TAG.LI) {
            link(label) {
                paddingTop = 12.px
                paddingBottom = 12.px
                margin = 2.px
                border = Border(1.px, BorderStyle.SOLID)
            }.onClick {
                window.toFront()
                window.focus()
            }
        }
        taskBar.add(task)
        return task
    }

    fun removeTask(task: Component) {
        taskBar.remove(task)
        task.dispose()
    }
}
