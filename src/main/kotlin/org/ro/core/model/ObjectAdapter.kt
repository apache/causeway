package org.ro.core.model

import org.ro.core.Utils
import org.ro.view.ImageRepository
import pl.treksoft.kvision.html.Image
import pl.treksoft.kvision.utils.Object

/**
 * Wrapps the adapted object and adds an operation that renders the adaptee as a clickable link in the UI.
 * @see: https://en.wikipedia.org/wiki/Adapter_pattern
 */
//FIXME dynamic 
class ObjectAdapter(var adaptee: Adaptable,
                    private var label: String? = null,
                    private var typeSpec: String? = null,
                    private var icon: Image? = null) : Visible {

    init {
        if (typeSpec == null) {
            typeSpec = "String"
        }
        if (icon == null) {
            icon = ImageRepository.ObjectIcon
        }
        if (label == null) {
            label = "label not set"
        }
        initPropertyAccessors()
    }
    
    override fun toString(): String {
        return "[$label -> $typeSpec]"
    }

    // add properties of the adaptee to the adapter dynamically
    fun initPropertyAccessors() {
        //first pass: add properties
       // val s: String = JSON.stringify(adaptee)
        //val o: Object = JSON.parse(s)
//        fromObject(o)
        //second pass: set values from adaptee
  //      fromAdaptee(o)
    }

    private fun fromObject(obj: Object) {
//FIXME dynamic?
/*
for (prop in obj) {
    this[prop] = obj[prop]
}
*/
    }

    private fun fromAdaptee(obj: Object) {
//FIXME dynamic?
/*
var value: Object
for (prop in obj) {
    value = adaptee[prop]
    this[prop] = value
}
*/
    }

    override fun tag(): String {
        var tag: String = ""
//FIXME
/*
if (this.hasOwnProperty("name")) {
    tag = this.name
} else if (this.hasOwnProperty("className")) {
    tag = this.className
} else {
    tag = "noNameNorClassname"
}
*/
        tag = Utils().deCamel(tag)
        return tag
    }

}
