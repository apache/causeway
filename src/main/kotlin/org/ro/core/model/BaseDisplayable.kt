package org.ro.core.model

import org.ro.layout.Layout
import org.ro.to.TObject

abstract class BaseDisplayable() {

    var isRendered = false

    abstract val title: String

    abstract var layout: Layout?

    abstract fun canBeDisplayed(): Boolean

    abstract fun addData(obj: TObject)

    abstract fun reset()

    fun extractTitle(): String {
        val strList = this.title.split("/")
        val len = strList.size
        if (len > 2) {
            return strList.get(len - 2)
        } else {
            return "no title"
        }
    }

}
