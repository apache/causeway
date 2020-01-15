package org.ro.core.model

import org.ro.layout.Layout
import org.ro.to.TObject
import org.ro.to.TransferObject

abstract class BaseDisplayable {

    var isRendered = false

    abstract val title: String

    abstract var layout: Layout?

    abstract fun canBeDisplayed(): Boolean

    abstract fun addData(obj: TransferObject)

    open fun getObject(): TObject? {
       // subclass responsibility
        return null
    }

    open fun reset() {
        // subclass responsibility
    }

    fun extractTitle(): String {
        val strList = this.title.split("/")
        val len = strList.size
        return if (len > 2) {
            strList[len - 2]
        } else {
            ""
        }
    }

}
