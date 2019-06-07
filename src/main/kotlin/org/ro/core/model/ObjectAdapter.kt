package org.ro.core.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import org.ro.to.TObject

@Serializable
class ObjectAdapter(val delegate: TObject) {
    init {
        //["sample"] = "Beispiel";
    }
    //TODO have getters peek into delegate
    val resultClass = "resultClass"
    val fixtureScript = "fixtureScript"
    val resultKey: String
        get() {
            return delegate.title
        }
    val result: String
        get() {
            return delegate.domainType
        }

    fun sample(): JsonObject {
        //val answer = JsonObject(JSON.stringify(delegate))
        return delegate.asDynamic()
    }

    fun get(propertyName: String): String {
        val that: dynamic = delegate
        if (that.hasOwnProperty(propertyName)) {
            return that.propertyName()
        } else {
            return "null"
        }
    }

    fun match(search: String?): Boolean {
        return search?.let {
            resultClass.contains(it, true) ?: false
        } ?: true
    }
    
} 