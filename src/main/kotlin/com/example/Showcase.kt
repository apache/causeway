package com.example

import pl.treksoft.kvision.core.Border
import pl.treksoft.kvision.core.BorderStyle
import pl.treksoft.kvision.core.Col
import pl.treksoft.kvision.dropdown.DropDown.Companion.dropDown
import pl.treksoft.kvision.form.check.CheckBox.Companion.checkBox
import pl.treksoft.kvision.form.text.Text.Companion.text
import pl.treksoft.kvision.hmr.ApplicationBase
import pl.treksoft.kvision.html.Link.Companion.link
import pl.treksoft.kvision.html.TAG
import pl.treksoft.kvision.html.Tag.Companion.tag
import pl.treksoft.kvision.i18n.I18n.tr
import pl.treksoft.kvision.navbar.Nav.Companion.nav
import pl.treksoft.kvision.navbar.NavForm.Companion.navForm
import pl.treksoft.kvision.navbar.Navbar.Companion.navbar
import pl.treksoft.kvision.navbar.NavbarType
import pl.treksoft.kvision.panel.FlexDir
import pl.treksoft.kvision.panel.FlexPanel.Companion.flexPanel
import pl.treksoft.kvision.panel.FlexWrap
import pl.treksoft.kvision.panel.Root
import pl.treksoft.kvision.panel.TabPanel.Companion.tabPanel
import pl.treksoft.kvision.panel.VPanel.Companion.vPanel
import pl.treksoft.kvision.require
import pl.treksoft.kvision.utils.px
import pl.treksoft.kvision.utils.vh

object Showcase : ApplicationBase {

    private lateinit var root: Root

    override fun start(state: Map<String, Any>) {

        root = Root("showcase") {
            vPanel(spacing = 0) {
//                height = 100.vh
                padding = 0.px
                navbar("NavBar", type = NavbarType.FIXEDTOP) {
                    height = 24.px
                    padding = 0.px
                    nav {
                        tag(TAG.LI) {
                            link(tr("File"), icon = "fa-file")
                        }
                        tag(TAG.LI) {
                            link(tr("Edit"), icon = "fa-bars")
                        }
                        dropDown(
                                tr("Favourites"),
                                listOf(tr("Basic formatting") to "#!/basic", tr("Forms") to "#!/forms"),
                                icon = "fa-star",
                                forNavbar = true
                        )
                    }
                    navForm {
                        text(label = tr("Search:"))
                        checkBox()
                    }
                    nav(rightAlign = true) {
                        tag(TAG.LI) {
                            link(tr("System"), icon = "fa-windows")
                        }
                    }
                }
                flexPanel(FlexDir.COLUMN, FlexWrap.WRAP, spacing = 20) {
                    padding = 0.px
                    paddingTop = 50.px
                    height = 100.vh
                    tabPanel {
                        border = Border(2.px, BorderStyle.SOLID, Col.SILVER)
                        addTab(tr("Basic formatting"), EditPanel(), "fa-bars", route = "/basic")
                        addTab(tr("Forms"), FormTab(), "fa-edit", route = "/forms")
                        addTab(tr("Buttons"), ButtonsTab(), "fa-check-square-o", route = "/buttons")
                        addTab(tr("Dropdowns & Menus"), DropDownTab(), "fa-arrow-down", route = "/dropdowns")
                        addTab(tr("Containers"), ContainersTab(), "fa-database", route = "/containers")
                        addTab(tr("Layouts"), LayoutsTab(), "fa-th-list", route = "/layouts")
                        addTab(tr("Modals"), ModalsTab(), "fa-window-maximize", route = "/modals")
                        addTab(tr("Data binding"), DataTab(), "fa-retweet", route = "/data")
                        addTab(tr("Windows"), WindowsTab(), "fa-window-restore", route = "/windows")
                        addTab(tr("Drag & Drop"), DragDropTab(), "fa-arrows-alt", route = "/dragdrop")
                    }
                }
                navbar("StatusBar", type = NavbarType.FIXEDBOTTOM) {}
            }
        }
    }

    override fun dispose(): Map<String, Any> {
        root.dispose()
        return mapOf()
    }

    val css = require("css/kroviz.css")
}
