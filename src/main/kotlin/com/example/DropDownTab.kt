package com.example

import pl.treksoft.kvision.dropdown.ContextMenu
import pl.treksoft.kvision.dropdown.DD
import pl.treksoft.kvision.dropdown.DropDown.Companion.dropDown
import pl.treksoft.kvision.dropdown.Header.Companion.header
import pl.treksoft.kvision.dropdown.Separator.Companion.separator
import pl.treksoft.kvision.form.check.CheckBox.Companion.checkBox
import pl.treksoft.kvision.form.text.Text.Companion.text
import pl.treksoft.kvision.html.Button.Companion.button
import pl.treksoft.kvision.html.ButtonStyle
import pl.treksoft.kvision.html.Image.Companion.image
import pl.treksoft.kvision.html.Label.Companion.label
import pl.treksoft.kvision.html.Link.Companion.link
import pl.treksoft.kvision.html.TAG
import pl.treksoft.kvision.html.Tag.Companion.tag
import pl.treksoft.kvision.i18n.I18n.tr
import pl.treksoft.kvision.navbar.Nav.Companion.nav
import pl.treksoft.kvision.navbar.NavForm.Companion.navForm
import pl.treksoft.kvision.navbar.Navbar.Companion.navbar
import pl.treksoft.kvision.panel.HPanel.Companion.hPanel
import pl.treksoft.kvision.panel.SimplePanel
import pl.treksoft.kvision.panel.VPanel.Companion.vPanel
import pl.treksoft.kvision.require
import pl.treksoft.kvision.utils.px

class DropDownTab : SimplePanel() {
    init {
        this.marginTop = 10.px
        this.minHeight = 400.px
        vPanel(spacing = 30) {
            navbar("NavBar") {
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
            dropDown(
                tr("Dropdown with navigation menu"), listOf(
                    tr("Basic formatting") to "#!/basic",
                    tr("Forms") to "#!/forms",
                    tr("Buttons") to "#!/buttons",
                    tr("Dropdowns & Menus") to "#!/dropdowns",
                    tr("Containers") to "#!/containers"
                ), "fa-arrow-right", style = ButtonStyle.SUCCESS
            ).apply {
                minWidth = 250.px
            }
            dropDown(tr("Dropdown with custom list"), icon = "fa-picture-o", style = ButtonStyle.WARNING) {
                minWidth = 250.px
                image(require("img/cat.jpg")) { margin = 10.px; title = "Cat" }
                separator()
                image(require("img/dog.jpg")) { margin = 10.px; title = "Dog" }
            }
            hPanel(spacing = 5) {
                val fdd = dropDown(
                    tr("Dropdown with special options"), listOf(
                        tr("Header") to DD.HEADER.option,
                        tr("Basic formatting") to "#!/basic",
                        tr("Forms") to "#!/forms",
                        tr("Buttons") to "#!/buttons",
                        tr("Separator") to DD.SEPARATOR.option,
                        tr("Dropdowns (disabled)") to DD.DISABLED.option,
                        tr("Separator") to DD.SEPARATOR.option,
                        tr("Containers") to "#!/containers"
                    ), "fa-asterisk", style = ButtonStyle.PRIMARY
                ) {
                    dropup = true
                    minWidth = 250.px
                }
                button(tr("Toggle dropdown"), style = ButtonStyle.INFO).onClick { e ->
                    fdd.toggle()
                    e.stopPropagation()
                }
            }
            label(tr("Open the context menu with right mouse click."))
            val contextMenu = ContextMenu {
                header(tr("Menu header"))
                link(tr("Basic formatting"), "#!/basic")
                link(tr("Forms"), "#!/forms")
                link(tr("Buttons"), "#!/buttons")
                link(tr("Dropdown & Menus"), "#!/dropdowns")
                separator()
                dropDown(tr("Dropdown"), forNavbar = true) {
                    link(tr("Containers"), "#!/containers")
                    link(tr("Layout"), "#!/layout")
                }
            }
            setContextMenu(contextMenu)
        }
    }
}
