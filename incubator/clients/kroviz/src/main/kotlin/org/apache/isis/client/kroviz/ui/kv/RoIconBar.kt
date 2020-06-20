package org.apache.isis.client.kroviz.ui.kv

import org.apache.isis.client.kroviz.core.event.EventStore
import org.apache.isis.client.kroviz.core.event.ResourceSpecification
import org.apache.isis.client.kroviz.core.model.Exposer
import org.apache.isis.client.kroviz.to.TObject
import org.apache.isis.client.kroviz.ui.kv.MenuFactory.buildForTitle
import org.apache.isis.client.kroviz.utils.IconManager
import org.apache.isis.client.kroviz.utils.Utils
import pl.treksoft.kvision.core.CssSize
import pl.treksoft.kvision.core.UNIT
import pl.treksoft.kvision.core.Widget
import pl.treksoft.kvision.dropdown.DropDown
import pl.treksoft.kvision.html.Button
import pl.treksoft.kvision.html.ButtonStyle
import pl.treksoft.kvision.panel.SimplePanel
import pl.treksoft.kvision.panel.VPanel
import kotlin.browser.document
import kotlin.dom.removeClass

object RoIconBar : SimplePanel() {

    val panel = VPanel()
    private val icons = mutableListOf<SimplePanel>()

    init {
        panel.addCssClass("icon-bar")
        panel.title = "Drop objects, factories, or actions here"
        this.add(createDeleteIcon())
        panel.setDropTargetData(Constants.format) { id ->
            when {
                Utils.isUrl(id!!) ->
                    this.add(createObjectIcon(id)!!)
                id.contains(Constants.actionSeparator) ->
                    this.add(createActionIcon(id))
                else ->
                    this.add(createFactoryIcon(id))
            }
        }
        hide()
    }

    private fun add(icon: SimplePanel) {
        icons.add(icon)
        panel.add(icon)
    }

    fun toggle() {
        if (panel.width?.first == 0) show() else hide()
    }

    override fun hide(): Widget {
        panel.width = CssSize(0, UNIT.px)
        panel.removeAll()
        return super.hide()
    }

    override fun show(): Widget {
        panel.width = CssSize(40, UNIT.px)
        icons.forEach { panel.add(it) }
        return super.show()
    }

    private fun createDeleteIcon(): Button {
        val del = Button(
                text = "",
                icon = IconManager.find("Delete"),
                style = ButtonStyle.LIGHT).apply {
            padding = CssSize(-16, UNIT.px)
            margin = CssSize(0, UNIT.px)
            title = "Drop icon here in order to remove it"
        }
        del.setDropTargetData(Constants.format) {
            icons.forEach { ii ->
                if (ii.id == it) {
                    icons.remove(ii)
                    panel.remove(ii)
                }
            }
        }
        return del
    }

    private fun createObjectIcon(url: String): DropDown? {
        val reSpec = ResourceSpecification(url)
        val logEntry = EventStore.find(reSpec)!!
        return when (val obj = logEntry.obj) {
            (obj == null) -> null
            is TObject -> {
                val exp = Exposer(obj)
                val ed = exp.dynamise()
                val hasIconName = ed.hasOwnProperty("iconName") as Boolean
                val iconName = if (hasIconName) (ed["iconName"] as String) else ""

                val icon = MenuFactory.buildForObject(
                        tObject = obj,
                        iconName = iconName,
                        withText = false)
                var title = Utils.extractTitle(logEntry.title)
                title += "\n${obj.title}"
                initIcon(icon, url, title, "icon-bar-object", icon.buttonId()!!)
                icon
            }
            else -> null
        }
    }

    private fun createActionIcon(id: String): SimplePanel {
        val titles = id.split(Constants.actionSeparator)
        val menuTitle = titles[0]
        val actionTitle = titles[1]
        val icon = MenuFactory.buildForAction(menuTitle, actionTitle)!!
        return initIcon(icon, id, id, "icon-bar-action", icon.id!!)
    }

    private fun createFactoryIcon(id: String): SimplePanel {
        val icon = buildForTitle(id)!!
        return initIcon(icon, id, id, "icon-bar-factory", icon.buttonId()!!)
    }

    private fun initIcon(icon: SimplePanel,
                         id: String,
                         title: String,
                         cssClass: String,
                         btnId: String)
            : SimplePanel {
        icon.setDragDropData(Constants.format, id)
        icon.id = id
        icon.title = title
        icon.addCssClass(cssClass)
        icon.afterInsertHook = {
            val btn = document.getElementById(btnId)!!
            btn.removeClass("dropdown-toggle")
        }
        return icon
    }

}
