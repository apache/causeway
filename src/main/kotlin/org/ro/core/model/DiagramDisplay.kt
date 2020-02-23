package org.ro.core.model

import org.ro.layout.Layout
import org.ro.to.DomainType
import org.ro.to.Property
import org.ro.to.TransferObject
import org.ro.ui.PumlBuilder
import pl.treksoft.kvision.state.observableListOf

class DiagramDisplay(override val title: String) : BaseDisplayable() {

    override var layout: Layout? = null

    private val classes = observableListOf<DomainType>()
    private val properties = observableListOf<Property>()
    var numberOfClasses = -1
    private var numberOfProperties = 0

    fun addProperties(inc: Int) {
        numberOfProperties += inc
    }

    override fun canBeDisplayed(): Boolean {
        console.log("[DiagramDisplay.canBeDisplayed]")
        console.log(this)
        return (numberOfClasses == classes.size
                && numberOfProperties == properties.size)
    }

    override fun addData(obj: TransferObject) {
        console.log("[DiagramDisplay.addData] ${obj::class}")
        console.log(obj)
        when (obj) {
            is DomainType -> classes.add(obj)
            is Property -> properties.add(obj)
            else -> {
            }
        }
    }

    fun buildDiagramCode(): String {
        var pumlCode: String = PumlBuilder().with(classes.first())
        return pumlCode
    }

}
