package org.ro.ui.kv

import org.ro.core.model.ObjectDM
import org.ro.ui.Displayable
import org.ro.ui.builder.LayoutBuilder
import pl.treksoft.kvision.core.CssSize
import pl.treksoft.kvision.core.FontStyle
import pl.treksoft.kvision.core.FontWeight
import pl.treksoft.kvision.core.UNIT
import pl.treksoft.kvision.dropdown.DropDown
import pl.treksoft.kvision.panel.VPanel

class RoDisplay(val displayModel: ObjectDM) : Displayable, VPanel() {

    var menu: DropDown? = null
    lateinit private var objectPanel: VPanel

    init {
        val ol = displayModel.layout
        if (ol != null) {
            val model = displayModel.data!!
            val tObject = model.delegate
            val grid = displayModel.grid!!
            objectPanel = LayoutBuilder().create(ol, grid, tObject, this)
            objectPanel.width = CssSize(100, UNIT.perc)
            add(objectPanel)
        }
    }

    override fun setDirty(value: Boolean) {
        displayModel.setDirty(value)
        if (value) {
            this.fontStyle = FontStyle.ITALIC
            this.fontWeight = FontWeight.BOLD
            if (menu != null) {
                MenuFactory.enableSaveUndo(menu!!)
            }
        } else {
            this.fontStyle = FontStyle.NORMAL
            this.fontWeight = FontWeight.NORMAL
            if (menu != null) {
                MenuFactory.disableSaveUndo(menu!!)
            }
        }
    }

}
