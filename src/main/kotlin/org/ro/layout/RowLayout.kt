package org.ro.layout

import kotlinx.serialization.Serializable
import org.ro.to.Action
import org.ro.to.Member
import org.ro.to.TObject
import org.ro.to.bs3.Row
import org.ro.ui.IconManager
import pl.treksoft.kvision.core.CssSize
import pl.treksoft.kvision.core.UNIT
import pl.treksoft.kvision.html.Button
import pl.treksoft.kvision.html.ButtonStyle
import pl.treksoft.kvision.panel.HPanel
import pl.treksoft.kvision.panel.VPanel

@Serializable
data class RowLayout(val cols: MutableList<ColsLayout> = mutableListOf<ColsLayout>(),
                     val metadataError: String? = null,
                     val cssClass: String? = null,
                     val id: String? = null
) {
//    private val maxSpan = 12

    constructor(row: Row) : this() {
        row.cols.forEach {
            cols.add(ColsLayout(it))
        }
    }

    fun build(tObject: TObject, actions: Map<String, Action>?): HPanel {
        val result = HPanel()
        result.width = CssSize(100, UNIT.perc)
        result.height = CssSize(100, UNIT.perc)

        val icon = IconManager.find("Actions")
        val button = Button(text = tObject.title, icon = icon, style = ButtonStyle.LINK).onClick {
//            val tObject = (data as Exposer).delegate
//            UiManager.displayObject(tObject)
        }
        result.add(button)

        return result
    }

    fun build(members: Map<String, Member>): VPanel {
        val result = VPanel()
        result.width = CssSize(100, UNIT.perc)
        result.height = CssSize(100, UNIT.perc)

        for (c in cols) {
            val b = c.build(members)
            result.add(b)
        }
        return result
    }

}
