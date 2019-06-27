package org.ro.core.model

import kotlinx.serialization.Serializable
import org.ro.to.TObject

@Serializable
class ObjectAdapter(val delegate: TObject) {
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

    fun get(propertyName: String): Any? {
        val that: dynamic = delegate
        if (that.hasOwnProperty(propertyName)) {
            return that[propertyName]
        } else {
            val delegatedProperty =  delegate.getProperty(propertyName)
            if (delegatedProperty != null) {
                return delegatedProperty.value
            }
            return null
        }
    }

    fun match(search: String?): Boolean {
        return search?.let {
            resultClass.contains(it, true) ?: false
        } ?: true
    }

}
