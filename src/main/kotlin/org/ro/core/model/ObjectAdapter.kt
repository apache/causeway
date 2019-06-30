package org.ro.core.model

import kotlinx.serialization.Serializable
import org.ro.to.TObject

@Serializable
class ObjectAdapter(private val delegate: TObject) {

    val result: String
        get() {
            return delegate.domainType
        }

    val resultClass: String
        get() {
            return "resultClass" //TODO where can this be taken from?
        }

    fun get(propertyName: String): Any? {
        val that: dynamic = delegate
        if (that.hasOwnProperty(propertyName)) {
            return that[propertyName]
        } else {
            val delegatedProperty = delegate.getProperty(propertyName)
            if (delegatedProperty != null) {
                return delegatedProperty.value
            }
            return null
        }
    }

    fun match(search: String?): Boolean {
        val result = search?.let {
            resultClass.contains(it, true)
        } ?: true
        return result;
    }

}
