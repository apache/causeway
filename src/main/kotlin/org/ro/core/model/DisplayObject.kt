package org.ro.core.model

import org.ro.core.aggregator.ActionDispatcher
import org.ro.layout.Layout
import org.ro.to.TObject
import org.ro.to.TransferObject

class DisplayObject(override val title: String) : BaseDisplayable() {
    var data: Exposer? = null
    override var layout: Layout? = null

    override fun canBeDisplayed(): Boolean {
        if (layout == null) {
            return false
        }
        return !isRendered
    }

    override fun addData(obj: TransferObject) {
        val exo = Exposer(obj as TObject)
        data = exo.dynamise() as? Exposer
    }

    //eventually move up
    private var dirty: Boolean = false

    fun setDirty(value: Boolean) {
        console.log("[RoDisplay.setDirty] $value")
        dirty = value
    }

    fun save() {
        console.log("[DisplayObject.save]")
        if (dirty) {
            val tObject = data!!.delegate
            val link = tObject.links.first()
            console.log(link)
            ActionDispatcher().invoke(link)
        }
    }

}
