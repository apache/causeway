package org.ro.ui.builder

import org.ro.layout.Cols
import org.ro.to.TObject
import org.ro.ui.kv.RoDisplay
import pl.treksoft.kvision.panel.SimplePanel
import pl.treksoft.kvision.panel.VPanel

class ColsBuilder {

    fun create(colsLayout: Cols, tObject: TObject, dsp: RoDisplay): SimplePanel {
        val result = VPanel()
        val colList = colsLayout.colList
        colList.forEach {
            val b = ColBuilder().create(it, tObject, dsp)
            result.add(b)
        }
        return result
    }

}
