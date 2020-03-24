package org.apache.isis.client.kroviz.ui.builder

import org.apache.isis.client.kroviz.to.TObject
import org.apache.isis.client.kroviz.to.bs3.Col
import org.apache.isis.client.kroviz.ui.kv.RoDisplay
import pl.treksoft.kvision.core.Component
import pl.treksoft.kvision.panel.HPanel
import pl.treksoft.kvision.panel.SimplePanel

class ColBuilder {

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
