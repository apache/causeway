package org.ro.to

import kotlinx.serialization.json.JsonObject
import org.ro.core.Utils

class Action(jsonObj: JsonObject? = null) : Member(jsonObj) {
    var parameterList: MutableList<Parameter> = mutableListOf()
    var link: Link? = null

    init {
        memberType = ACTION
        if (jsonObj != null) {
            val json = fixDefault(jsonObj)
            val linkJS = json["link"].jsonObject
            link = Link(linkJS)
            val parameters = json["parameters"].jsonArray
            for (o in parameters) {
                val p = Parameter(o as JsonObject)
                parameterList.add(p)
            }
        }
    }

    fun getInvokeLink(): Link? {
        for (l in linkList) {
            if (l.rel.indexOf(this.id) > 0) {
                return l
            }
        }
        return null
    }

    fun findParameterByName(name: String): Parameter? {
        for (p in parameterList) {
            if (p.id == name) return p
        }
        return null
    }

    //Workaround for https://issues.apache.org/jira/browse/ISIS-1850 would break RO Spec 1.0
    private fun fixDefault(input: JsonObject): JsonObject {
        val search: String = "\"default\":"
        val replace: String = "\"defaultChoice\":"
        return Utils().replace(input, search, replace)
    }

}