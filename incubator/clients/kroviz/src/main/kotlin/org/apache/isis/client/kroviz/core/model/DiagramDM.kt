package org.apache.isis.client.kroviz.core.model

import org.apache.isis.client.kroviz.to.DomainType
import org.apache.isis.client.kroviz.to.Property
import org.apache.isis.client.kroviz.to.TransferObject

class DiagramDM(override val title: String) : DisplayModel() {

    val classes = mutableSetOf<DomainType>()
    val properties = mutableSetOf<Property>()
    var numberOfClasses = -1
    private var numberOfProperties = 0

    fun incNumberOfProperties(inc: Int) {
        numberOfProperties += inc
    }

    fun decNumberOfClasses() {
        numberOfClasses--
    }

    override fun canBeDisplayed(): Boolean {
        if (isRendered) return false
        return (numberOfClasses == classes.size)
                //TODO && numberOfProperties == properties.size

    }

    override fun addData(obj: TransferObject) {
        when (obj) {
            is DomainType -> classes.add(obj)
            is Property -> properties.add(obj)
            else -> {
            }
        }
    }

}
