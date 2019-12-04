package org.ro.ui.kv

import org.ro.core.aggregator.ActionAggregator
import org.ro.to.TObject
import org.ro.ui.IconManager
import pl.treksoft.kvision.dropdown.ContextMenu
import pl.treksoft.kvision.dropdown.cmLink
import pl.treksoft.kvision.dropdown.header
import pl.treksoft.kvision.i18n.tr

object MenuFactory {

    fun buildFor(tObject: TObject): ContextMenu {
        val type = tObject.domainType
        val actions = tObject.getActions()
        return ContextMenu {
            header(tr("Actions for $type"))
            actions.forEach {
                val title = it.id
                val iconName = IconManager.find(title)
                val link = it.getInvokeLink()!!
                cmLink(tr(title), icon = iconName) {
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
