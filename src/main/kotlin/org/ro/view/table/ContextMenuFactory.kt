package org.ro.org.ro.view.table

import org.ro.core.aggregator.ActionAggregator
import org.ro.to.TObject
import org.ro.view.IconManager
import pl.treksoft.kvision.dropdown.ContextMenu
import pl.treksoft.kvision.dropdown.Header.Companion.header
import pl.treksoft.kvision.html.Link.Companion.link
import pl.treksoft.kvision.i18n.I18n

class ContextMenuFactory {

    fun buildFor(tObject: TObject): ContextMenu {
        val type = tObject.domainType
        val actions = tObject.getActions()
        return ContextMenu {
            header(I18n.tr("Actions for $type"))
            actions.forEach {
                val title = it.id
                val iconName = IconManager.find(title)
                val link = it.getInvokeLink()!!//.href
                link(I18n.tr(title), icon = iconName) {
                    setEventListener {
                        click = { e ->
                            e.stopPropagation()
                            ActionAggregator().invoke(link)
                            this@ContextMenu.hide()
                        }
                    }
                }

            }
        }
    }

}
