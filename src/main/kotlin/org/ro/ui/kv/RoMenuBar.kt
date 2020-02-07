package org.ro.ui.kv

import org.ro.to.mb.Menubars
import org.ro.ui.ExportAlert
import org.ro.ui.IconManager
import org.ro.ui.Point
import org.w3c.dom.parsing.DOMParser
import pl.treksoft.kvision.core.CssSize
import pl.treksoft.kvision.core.UNIT
import pl.treksoft.kvision.dropdown.DropDown
import pl.treksoft.kvision.dropdown.ddLink
import pl.treksoft.kvision.dropdown.dropDown
import pl.treksoft.kvision.html.ButtonStyle
import pl.treksoft.kvision.navbar.*
import pl.treksoft.kvision.panel.SimplePanel
import pl.treksoft.kvision.panel.vPanel
import kotlin.browser.document

object RoMenuBar : SimplePanel() {
    lateinit var navbar: Navbar
    lateinit var nav: Nav

    init {
        vPanel() {
            val label = "" //IMPROVE use for branding
            navbar = navbar(label = label, type = NavbarType.FIXEDTOP) {
                marginLeft = CssSize(-32, UNIT.px)
                height = CssSize(40, UNIT.px)
                nav = nav()
                val mainEntry = buildMainMenu()
                nav.add(mainEntry)
            }
        }
    }

    private fun buildMainMenu(): DropDown {
        val iconName = IconManager.find("Burger") //IMPROVE use for branding
        return dropDown(
                "",
                icon = iconName,
                forNavbar = false,
                style = ButtonStyle.LIGHT)
        {
            ddLink(
                    "Connect ...",
                    icon = IconManager.find("Connect")
            ).onClick { e ->
                val at = Point(e.pageX.toInt(), e.pageY.toInt())
                LoginPrompt().open(at)
            }

            val exportTitle = "Export Events for Replay"
            ddLink(exportTitle,
                    icon = IconManager.find("Export")
            ).onClick {
                ExportAlert().open()
            }
            val sampleTitle = "Image Sample"
            ddLink(sampleTitle,
                    icon = IconManager.find(sampleTitle)
            ).onClick {
                val panel = PlantumlPanel() as SimplePanel
                UiManager.add(sampleTitle, panel)
            }
        }
    }

    fun amendMenu(menuBars: Menubars) {
//        brandLogo()
        menuBars.primary.menu.forEach { m ->
            nav.add(MenuFactory.buildFor(m))
        }
        //TODO handle secondary / tertiary as well
        nav.add(MenuFactory.buildFor(menuBars.secondary.menu.first()))
        nav.add(MenuFactory.buildFor(menuBars.tertiary.menu.first()))
    }

    // animated svg added at the end of the nav
    private fun brandLogo() {
        val response = """<!DOCTYPE svg PUBLIC "-//W3C//DTD SVG 1.1//EN" "http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd">
<svg version="1.1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink"
 width="1%" height="1%" viewBox="-4 -4 8 8">
 <title>SVG animation using SMIL</title>
 <circle cx="0" cy="1" r="2" stroke="red" fill="none">
  <animateTransform
   attributeName="transform"
   attributeType="XML"
   type="rotate"
   from="0"
   to="360"
   begin="0s"
   dur="1s"
   repeatCount="indefinite"/>
 </circle>
</svg>"""
        val p = DOMParser()
        val svg = p.parseFromString(response, "image/svg+xml")
        val dp = document.getElementById("kv_navbar_0")
        dp.asDynamic().appendChild(svg.documentElement)
    }

}
