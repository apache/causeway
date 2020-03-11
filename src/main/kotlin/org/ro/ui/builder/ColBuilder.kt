package org.ro.ui.builder

import org.ro.to.TObject
import org.ro.to.bs3.Col
import org.ro.ui.kv.RoDisplay
import pl.treksoft.kvision.core.Component
import pl.treksoft.kvision.panel.HPanel
import pl.treksoft.kvision.panel.SimplePanel

class ColBuilder() {

    fun create(col: Col, tObject: TObject, dsp: RoDisplay): SimplePanel {
        val result = HPanel()
        var cpt: Component?
        for (tg in col.tabGroupList) {
            cpt = TabGroupBuilder().create(tg, tObject, dsp)
            result.add(cpt)
        }
        for (fs in col.fieldSetList) {
            cpt = FieldSetBuilder().create(fs, tObject, dsp)
            result.add(cpt!!)
        }
        return result
    }

}
