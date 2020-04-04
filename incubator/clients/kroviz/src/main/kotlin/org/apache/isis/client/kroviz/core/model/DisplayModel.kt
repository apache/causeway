package org.apache.isis.client.kroviz.core.model

import org.apache.isis.client.kroviz.to.TObject
import org.apache.isis.client.kroviz.to.TransferObject

abstract class DisplayModel {

    var isRendered = false

    abstract val title: String

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
