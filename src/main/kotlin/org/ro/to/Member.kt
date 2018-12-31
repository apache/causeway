package org.ro.to

import kotlinx.serialization.json.JsonObject

open class Member(jsonObj: JsonObject? = null) : Invokeable(jsonObj) {
    val ACTION = "action"
    val PROPERTY = "property"

    var id: String = ""
    var memberType: String? = null
    internal var value: Any? = null
    private var valueObject: Any? = null
    private var format: String? = null
    internal var extensions: JsonObject? = null
    private var extensionObject: Extensions? = null
    private var disabledReason: String? = null
    private var optional: JsonObject? = null

    init {
        if (jsonObj != null) {
            val link: Link = linkList[0]
            href = link.href
            method = link.method
            extensionObject = Extensions(extensions)
            //TODO use format and/or extensions.xIsisFormat on order to type
            valueObject = constructByValue()
        }
    }

    fun getValue(): Any? {
        return valueObject
    }

    fun getExtension(): Extensions? {
        return extensionObject
    }

    private fun constructByValue(): Any {
        if (isNumber()) return value as Number
        if (isString()) return value as String
        if (format == "object") return value as Link
        return this.value as Any
    }

    private fun isString(): Boolean {
        return (format == "string") || (extensionObject!!.xIsisFormat == "string")
    }

    private fun isNumber(): Boolean {
        return (format == "int") || (format == "utc-millisec")
    }

}
