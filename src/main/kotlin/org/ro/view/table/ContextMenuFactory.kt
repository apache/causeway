package org.ro.org.ro.view.table

import org.ro.to.TObject
import org.ro.view.IconManager
import pl.treksoft.kvision.dropdown.ContextMenu
import pl.treksoft.kvision.dropdown.Header.Companion.header
import pl.treksoft.kvision.html.Link.Companion.link
import pl.treksoft.kvision.i18n.I18n

class ContextMenuFactory {

    fun buildFor(tObject: TObject): ContextMenu {
        val contextMenu = ContextMenu {
            header(I18n.tr("TODO Use REAL Object Actions"))
            //TODO refactor
            val title1 = "Edit"
            val iconName1 = IconManager.find(title1)
            link(I18n.tr(title1), icon = iconName1) {
                setEventListener {
                    click = { e ->
                        e.stopPropagation()
//                        invoke edit action
                        this@ContextMenu.hide()
                    }
                }
            }
            val title2 = "Delete"
            val iconName2 = IconManager.find(title2)
            link(I18n.tr(title2), icon = iconName2) {
                setEventListener {
                    click = { e ->
                        e.stopPropagation()
//                        invoke delete action
                        this@ContextMenu.hide()
                    }
                }
            }
        }
        return contextMenu
    }
}
