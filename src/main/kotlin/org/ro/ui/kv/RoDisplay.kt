package org.ro.ui.kv

import org.ro.core.model.DisplayObject
import pl.treksoft.kvision.core.CssSize
import pl.treksoft.kvision.core.UNIT
import pl.treksoft.kvision.dropdown.DropDown
import pl.treksoft.kvision.panel.VPanel

class RoDisplay(val displayObject: DisplayObject) : VPanel() {

    var menu: DropDown? = null

    init {
        val ol = displayObject.layout
        if (ol != null) {
            val model = displayObject.data!!
            val tObject = model.delegate
            val objectPanel = ol.build(tObject, this)
            objectPanel.width = CssSize(100, UNIT.perc)
            add(objectPanel)
        }
    }

    fun setDirty(value:Boolean) {
        displayObject.setDirty(value)
        if (menu != null) {
            MenuFactory.enableSaveUndo(menu!!)
        }
    }

}
