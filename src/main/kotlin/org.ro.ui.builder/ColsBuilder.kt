package org.ro.ui.builder

import org.ro.layout.ColsLayout
import org.ro.to.TObject
import org.ro.ui.kv.RoDisplay
import pl.treksoft.kvision.panel.HPanel
import pl.treksoft.kvision.panel.VPanel

class ColsBuilder {

    fun create(colsLayout: ColsLayout, tObject: TObject, dsp: RoDisplay): VPanel {
        val result = VPanel()
        val b: HPanel = ColBuilder().create(colsLayout.col!!, tObject, dsp)
        result.add(b)
        return result
    }

}
