package org.ro.core.model

import kotlinx.serialization.Serializable
import org.ro.to.MemberType
import org.ro.to.TObject

/**
 * Makes properties of delegate available for display in Lists.
 * For regular TObjects these are Member.
 * For FixtureResults these are: result, resultClass etc.
 */
@Serializable
class Exposer(val delegate: TObject) {

    var iconName = ""

    fun dynamise(): dynamic {
        //FIXME where do Result and Member differ?
        val thys = dynamiseMember()
//        iconName = "fa-cube"
        if (delegate.selfLink() != null) {
//            iconName = "fa-star"
            return dynamiseResult()
        }
        return thys
    }

    fun dynamiseMember(): dynamic {
        val thys = this.asDynamic()
        for (m in delegate.members) {
            val member = m.value
            if (member.memberType == MemberType.PROPERTY.type) {
                val realValue = member.value
                if (realValue != null) {
                    thys[member.id] = realValue.content
                }
            }
        }
        thys.iconName = "fa-cube"
        return thys
    }

    // eg. for dataNucleusId
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

    fun dynamiseResult(): dynamic {
        val that = this.asDynamic()
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
        return that
    }

    var result: String
        set(arg: String) {
    //        console.log("[RE.result.set] tabulator requires setter $arg")
        }
        get() {
            var answer = "dummy result"
            val link = delegate.selfLink()
            if (link != null) {
                answer = link.resultTitle()
            }
            return answer
        }

    var resultClass: String
        set(arg: String) {
     //       console.log("[RE.resultClass.set] tabulator requires setter $arg")
        }
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
        set(arg: String) {
   //         console.log("[RE.resultKey.set] tabulator requires setter $arg")
        }
        get() {
            var answer = "dummy resultKey"
            val link = delegate.selfLink()
            if (link != null) {
                answer = link.resultKey()
            }
            return answer
        }

}
