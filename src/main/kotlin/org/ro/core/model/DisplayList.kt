package org.ro.core.model

import org.ro.layout.Layout
import org.ro.to.Extensions
import org.ro.to.Property
import org.ro.to.TObject
import pl.treksoft.kvision.utils.observableListOf

class DisplayList(override val title: String) : BaseDisplayable() {
    var data = observableListOf<Exposer>()
    override var layout: Layout? = null
    var propertyLabels = mutableMapOf<String, String>()
    var properties = mutableListOf<Property>()

    override fun canBeDisplayed(): Boolean {
        if (layout == null) {
            return false
        } else {
            val lps = layout!!.properties.size
            return (lps <= propertyLabels.size)
                    && (lps <= properties.size)
                    && !isRendered
        }
    }

    override fun addData(obj: TObject) {
        val exo = Exposer(obj)
        data.add(exo.dynamise())  //if exposer is not dynamised, data access in tables won't work
    }

    // PropertyDescription
    fun addPropertyLabel(p: Property) {
        val id = p.id
        val e: Extensions = p.extensions!!
        val friendlyName = e.friendlyName
        propertyLabels.put(id, friendlyName)
    }

    // Property
    fun addProperty(property: Property) {
        properties.add(property)
    }

    override fun reset() {
        isRendered = false
        data = observableListOf<Exposer>()
    }

}
