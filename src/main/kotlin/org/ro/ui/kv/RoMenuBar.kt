package org.ro.ui.kv

import org.ro.core.Menu
import org.ro.core.MenuEntry
import org.ro.core.aggregator.ActionAggregator
import org.ro.core.event.EventStore
import org.ro.ui.IconManager
import pl.treksoft.kvision.core.CssSize
import pl.treksoft.kvision.core.UNIT
import pl.treksoft.kvision.dropdown.DropDown
import pl.treksoft.kvision.dropdown.ddLink
import pl.treksoft.kvision.dropdown.dropDown
import pl.treksoft.kvision.i18n.tr
import pl.treksoft.kvision.navbar.*
import pl.treksoft.kvision.panel.SimplePanel
import pl.treksoft.kvision.panel.vPanel

object RoMenuBar : SimplePanel() {
    lateinit var navbar: Navbar
    lateinit var nav: Nav

    init {
        vPanel() {
            val label = "" //IMPROVE use for branding
            navbar = navbar(label, NavbarType.FIXEDTOP) {
                height = CssSize(40, UNIT.px)
                nav = nav()
                val mainEntry = buildMainEntry()
                nav.add(mainEntry)
            }
        }
    }

    //TODO extract invocationsof ddLink to MenuFactory ?
    private fun buildMainEntry(): DropDown {
        val iconName = IconManager.find("Burger") //IMPROVE use for branding
        val dd = dropDown("", icon = iconName, forNavbar = true)
        {
            ddLink("Connect ...", icon = IconManager.find("Connect"))
                    .onClick { LoginPrompt().open() }
            val logTitle = "Log Entries"
            ddLink(logTitle, icon = IconManager.find(logTitle))
                    .onClick {
                        val model = EventStore.log
                        UiManager.add(logTitle, EventLogTable(model))
                    }
            val sampleTitle = "Image Sample"
            ddLink(sampleTitle, icon = IconManager.find(sampleTitle))
                    .onClick {
                        val panel = ImagePanel()
                        UiManager.add(sampleTitle, panel)
                    }
        }
        return dd
    }

    // tr("Separator") to DD.SEPARATOR.option,
    fun amendMenu() {
        for (title: String in Menu.filterUniqueMenuTitles()) {
            val label = tr(title)
            val icon = IconManager.find(title)
            val dd = dropDown(label, icon = icon, forNavbar = true) {
                for (me: MenuEntry in Menu.filterEntriesByTitle(title)) {
                    val l = me.action.getInvokeLink()!!
                    val actionLabel = me.action.id
                    val actionIcon = IconManager.find(actionLabel)
                    ddLink(actionLabel, icon = actionIcon).onClick {
                        ActionAggregator().invoke(l)
                    }
                }
            }
            nav.add(dd)
        }
    }


}
