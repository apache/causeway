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
class MemberExposer(val delegate: TObject) : Exposer {

    var iconName = "fa-star"

    fun dynamise(): dynamic {
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

}
