package org.apache.isis.client.kroviz.ui.builder

import org.apache.isis.client.kroviz.to.TObject
import org.apache.isis.client.kroviz.to.bs3.Col
import org.apache.isis.client.kroviz.ui.kv.RoDisplay
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
