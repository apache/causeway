package org.ro.core.model

import kotlinx.serialization.Serializable
import org.ro.to.TObject

@Serializable
class ObjectAdapter(private val delegate: TObject) {

    var iconName = "fa-cube"  //TODO fixed value for FixtureResult only

    var result: String
        set(arg: String) {}  // tabulator requires setter
        get() {
            var answer = "dummy result"
            val link = delegate.selfLink()
            if (link != null) {
                answer = link.resultTitle()
            }
            return answer
        }

    var resultClass: String
        set(arg: String) {} // tabulator requires setter
        get() {
            var answer = ""
            val member = delegate.getProperty("className")
            if (member != null) {
                val value = member.value
                if (value != null)
                    answer = value.content.toString()
            }
            return answer
        }

    var resultKey: String
        set(arg: String) {} // tabulator requires setter
        get() {
            var answer = "dummy resultKey"
            val link = delegate.selfLink()
            if (link != null) {
                answer = link.resultKey()
            }
            return answer
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

}
