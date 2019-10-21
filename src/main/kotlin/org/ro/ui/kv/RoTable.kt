package org.ro.ui.kv

import org.ro.core.model.DisplayList
import org.ro.core.model.Exposer
import pl.treksoft.kvision.panel.FlexAlignItems
import pl.treksoft.kvision.panel.FlexWrap
import pl.treksoft.kvision.panel.HPanel
import pl.treksoft.kvision.panel.VPanel
import pl.treksoft.kvision.tabulator.Layout
import pl.treksoft.kvision.tabulator.Tabulator
import pl.treksoft.kvision.tabulator.Tabulator.Companion.tabulator
import pl.treksoft.kvision.tabulator.TabulatorOptions
import pl.treksoft.kvision.utils.px

/**
 * access attributes from dynamic (JS) objects with varying
 * - numbers of attributes
 * - attribute types (can only be determined at runtime) and
 * - accessor names
 */
class RoTable(displayList: DisplayList) : VPanel() {

    init {
        title = displayList.extractTitle()
        val model = displayList.data
        val columns = ColumnFactory().buildColumns(displayList, true)

        HPanel(
                FlexWrap.NOWRAP,
                alignItems = FlexAlignItems.CENTER,
                spacing = 20) {
            padding = 10.px
        }

        val options = TabulatorOptions(
                height = "calc(100vh - 128px)",
                layout = Layout.FITCOLUMNS,
                columns = columns,
                persistenceMode = false//,
                //selectable = true
        )

        tabulator(model, options = options) {
            marginTop = 0.px
            marginBottom = 0.px
            setEventListener<Tabulator<Exposer>> {
                tabulatorRowClick = {
                }
            }
        }
    }

}
