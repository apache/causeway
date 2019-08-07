package org.ro.core.model

import kotlinx.serialization.Serializable
import org.ro.to.MemberType
import org.ro.to.TObject

/**
 * Makes properties of delegate available for display in Lists.
 * For regular TObjects these are all members.
 * For FixtureResults these are: result, resultClass etc.
 */
@Serializable
class Exposer(private val delegate: TObject) {

    fun dynamise(): dynamic {
        val that = this.asDynamic()
        //FixtureResult
        /*
        // result
        var result = "dummy result"
        val link = delegate.selfLink()
        if (link != null) {
            result = link.resultTitle()
        }
        that["result"] = result

        // resultClass
        var resultClass = ""
        val member = delegate.getProperty("className")
        if (member != null) {
            val value = member.value
            if (value != null)
                resultClass = value.content.toString()
        }
        that["resultClass"] = resultClass

        // resultKey
        var resultKey = "dummy resultKey"
        if (link != null) {
            resultKey = link.resultKey()
        }
        that["resultKey"] = resultKey
                 */
        // members
        for (m in delegate.members) {
            if (m.value.memberType == MemberType.PROPERTY.type) {
                that[m.key] = m.value
            }
        }
        return that
    }

    var iconName = "fa-cube"  //TODO fixed value for FixtureResult only

    //FIXME TObject members should already be exposed by default
    //TODO only FixtureResults need this special handling

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
