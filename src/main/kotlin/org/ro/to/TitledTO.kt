package org.ro.to

import kotlinx.serialization.json.JsonObject

/**
 * 'abstract' superclass of TObject and Service
 */
open class TitledTO(jsonObj: JsonObject? = null) : LinkedTO(jsonObj) {
    private var extensions: Extensions? = null
    internal var title = ""
    internal var memberList = mutableListOf<Invokeable>()

    init {
        if (jsonObj != null) {
            val ext = jsonObj["extensions"].jsonObject
            extensions = Extensions(ext)
            title = jsonObj["title"].toString()
            val members = jsonObj["members"].jsonArray
            for (m in members) {
                val member = Member(m as JsonObject)
                memberList.add(member)
            }
        }
    }

    fun getMembers(): MutableList<Invokeable> {
        return this.memberList
    }
    
}