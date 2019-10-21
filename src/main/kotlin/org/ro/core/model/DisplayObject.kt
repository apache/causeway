package org.ro.core.model

import org.ro.layout.Layout
import org.ro.to.TObject

class DisplayObject(override val title: String) : BaseDisplayable() {
    var data: Exposer? = null
    override var layout: Layout? = null

    override fun canBeDisplayed(): Boolean {
        if (layout == null) {
            return false
        } else {
            return !isRendered
        }
    }

    override fun addData(obj: TObject) {
        val exo = Exposer(obj)
        data = exo.dynamise()
    }

    override fun reset() {
        TODO("not implemented")
    }

}
