package org.apache.isis.client.kroviz.ui.builder

import org.apache.isis.client.kroviz.to.TObject
import org.apache.isis.client.kroviz.to.bs3.Col
import org.apache.isis.client.kroviz.ui.kv.RoDisplay
import pl.treksoft.kvision.panel.*

class ColBuilder {

    fun create(col: Col, tObject: TObject, dsp: RoDisplay): FlexPanel {
        val result = FlexPanel(
                FlexDir.COLUMN,
                FlexWrap.NOWRAP,
                FlexJustify.SPACEBETWEEN,
                FlexAlignItems.CENTER,
                FlexAlignContent.STRETCH,
                spacing = 10)
        for (tg in col.tabGroupList) {
            val tgCpt = TabGroupBuilder().create(tg, tObject, dsp)
            result.add(tgCpt)
        }
        for (fs in col.fieldSetList) {
            val fsCpt = FieldSetBuilder().create(fs, tObject, dsp)!!
            val fsPanel = FieldsetPanel(legend = fs.name).add(fsCpt)
            result.add(fsPanel)
        }
        return result
    }

}
