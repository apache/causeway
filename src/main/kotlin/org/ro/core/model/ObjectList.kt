package org.ro.core.model

import org.ro.layout.Layout
import org.ro.to.Extensions
import org.ro.to.Property

class ObjectList : Visible {
    override fun tag(): String {
        return " "
    }

    var list = mutableListOf<ObjectAdapter>()
    var layout: Layout? = null
    var propertyLabels = mutableMapOf<String, String>()

    fun hasLayout(): Boolean {
        val it = layout != null
        if (it) {
//            initPropertyDescription()
        }
        return it
    }

    private fun initPropertyDescription() {
        if (arePropertyLabelsToBeSet()) {
            val pls = layout!!.properties
            for (pl in pls) {
                val l = pl.link
                l!!.invoke()
            }
        }
    }

    fun addProperty(p: Property) {
        val id = p.id
        val e: Extensions = p.extensions!!
        val friendlyName = e.friendlyName
        propertyLabels.put(id, friendlyName)
        console.log("[ObjectList.handleProperty] $id:$friendlyName")
    }

    fun getPropertyLabel(id: String): String? {
        return propertyLabels.get(id)
    }

    private fun arePropertyLabelsToBeSet(): Boolean {
        val labelSize: Int = propertyLabels.size
        var propsSize = 0
        if (layout!!.properties.isNotEmpty()) {
            propsSize = layout!!.properties.size
        }
        return (labelSize < propsSize)
    }

}