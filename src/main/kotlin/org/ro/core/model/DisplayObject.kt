package org.ro.core.model

import org.ro.layout.Layout
import org.ro.org.ro.core.model.BaseDisplayable
import org.ro.to.TObject

class DisplayObject(override val title: String) : BaseDisplayable() {
    var data: Exposer? = null
    var layout: Layout? = null
    var isRendered = false

    fun canBeDisplayed(): Boolean {
        if (layout == null) {
            return false
        } else {
            return !isRendered
        }
    }

    fun addData(obj: TObject) {
        val exo = Exposer(obj)
        data = exo.dynamise()
    }

}
