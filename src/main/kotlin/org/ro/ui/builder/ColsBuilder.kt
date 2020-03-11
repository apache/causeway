package org.ro.ui.builder

import org.ro.to.TObject
import org.ro.to.bs3.Col
import org.ro.ui.kv.RoDisplay
import pl.treksoft.kvision.panel.SimplePanel
import pl.treksoft.kvision.panel.VPanel

class ColsBuilder {

    fun create(col: Col, tObject: TObject, dsp: RoDisplay): SimplePanel {
        val result = VPanel()
     //   val colList = col.colList
 //       colList.forEach {
            val b = ColBuilder().create(col, tObject, dsp)
            result.add(b)
   //     }
        return result
    }

}
