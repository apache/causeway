package org.ro.core.model

import org.ro.layout.Layout
import org.ro.to.Extensions
import org.ro.to.Property
import org.ro.to.TObject
import org.ro.to.TransferObject
import pl.treksoft.kvision.state.observableListOf

class DisplayList(override val title: String) : BaseDisplayable() {
    var data = observableListOf<Exposer>()
    override var layout: Layout? = null
    var propertyDescriptionList = mutableMapOf<String, String>()
    var propertyList = mutableListOf<Property>()

    override fun canBeDisplayed(): Boolean {
        when {
            isRendered -> return false
            layout == null -> return false
            else -> {
                val lps = layout!!.propertyList.size
                val pds = propertyDescriptionList.size
              //  val ps =  propertyList.size
                val descriptionsComplete = lps <= pds
              //  val propertiesComplete = lps <= ps
                console.log("[DL.canBeDisplayed] layout.properties: $lps")
                console.log("[DL.canBeDisplayed] propertyDescriptions: $pds")
             //   console.log("[DL.canBeDisplayed] properties: $ps")
                return descriptionsComplete //&& propertiesComplete
            }
        }
    }

    override fun addData(obj: TransferObject) {
        val exo = Exposer(obj as TObject)
        data.add(exo.dynamise())  //if exposer is not dynamised, data access in tables won't work
    }

    fun addPropertyDescription(p: Property) {
        val id = p.id
        val e: Extensions = p.extensions!!
        val friendlyName = e.friendlyName
        propertyDescriptionList.put(id, friendlyName)
        layout?.addPropertyDescription(p)
    }

    fun addProperty(property: Property) {
        propertyList.add(property)
    }

    override fun reset() {
        isRendered = false
        data = observableListOf<Exposer>()
    }

}
