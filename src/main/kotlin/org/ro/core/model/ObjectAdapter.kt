package org.ro.core.model

import org.ro.core.Utils
import pl.treksoft.kvision.utils.Object

/**
 * Wrapps the adapted object and adds an operation that renders the adaptee as a clickable link in the UI.
 * @see: https://en.wikipedia.org/wiki/Adapter_pattern
 */
//FIXME dynamic 
class ObjectAdapter(var adaptee: dynamic,
                    var label: String = "label not set",
                    private var typeSpec: String = "String",
                    private var iconName: String = "fa-box") : Visible {

    init {
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
        tag = Utils.deCamel(tag)
        return tag
    }

}
