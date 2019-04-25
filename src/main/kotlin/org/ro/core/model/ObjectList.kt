package org.ro.core.model

import org.ro.layout.Layout
import org.ro.to.Extensions
import org.ro.to.Property
import org.ro.to.TObject

class ObjectList : Visible {
    override fun tag() : String { return " "}
    
    var list = mutableListOf<ObjectAdapter>()
    var layout: Layout? = null
    var propertyLabels = mutableMapOf<String, String>()

    fun hasLayout(): Boolean {
        return layout != null
    }

    //FIXME to be called after layout is set
    private fun initPropertyDescription() {
        if (hasLayout()) {
            if (arePropertyLabelsToBeSet()) {
                val pls = layout!!.properties
                for (pl in pls) {
                    val l = pl.link
                    l!!.invoke()
                }
            }
        }
    }

    //TODO public for test only, reduce visibility 
    fun add(oa: ObjectAdapter) {
        if (oa.adaptee is TObject) {
            val tObj = oa.adaptee.unsafeCast<TObject>()
            tObj.addMembersAsProperties()
        }
        list.add(oa)
    }

    fun handleProperty(p: Property) {
        val id = p.id
        val e: Extensions = p.extensions!!
        val friendlyName = e.friendlyName
        propertyLabels.put(id, friendlyName)
        console.log("[ObjectList.handleProperty] $id:$friendlyName")
    }

    fun getPropertyLabel(id: String): String? {
        return propertyLabels.get(id)
    }

    fun arePropertyLabelsToBeSet(): Boolean {
        val labelSize: Int = propertyLabels.size
        var propsSize = 0
        if (layout!!.properties.isNotEmpty()) {
            propsSize = layout!!.properties.size
        }
        return (labelSize < propsSize)
    }
    
}