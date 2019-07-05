package org.ro.to

import kotlinx.serialization.Serializable
import org.ro.core.model.ObjectAdapter

@Serializable
data class TObject(val links: List<Link> = emptyList(),
                   val extensions: Extensions,
                   val title: String = "",
                   val domainType: String = "",
                   val instanceId: String? = null,
        //TODO authors@ro.org : Shouldn't members be modeled as elements of Array/[] instead of Map/{} ?
                   val members: Map<String, Member> = emptyMap()
) : TransferObject {

    fun getProperties(): MutableList<Member> {
        val result = mutableListOf<Member>()
        for (m in members) {
            if (m.value.memberType == MemberType.PROPERTY.type) {
                result.add(m.value)
            }
        }
        return result
    }

    fun getProperty(key: String): Member? {
        for (m in members) {
            if (m.key == key) {
                return m.value
            }
        }
        return null
    }

    /**
     * Post-Constructor fun using dynamic nature of class.
     */
    fun addMembersAsProperties() {
        val members: MutableList<Member> = getProperties()
        console.log("[TObject.addMembersAsProperties] $members")
        for (m in members) {
            if (m.memberType == MemberType.PROPERTY.type) {
                addAsProperty(m)
            }
        }
    }

    fun addAsProperty(member: Member) {
        val value = "//TODO members of type property can be either null|String|Link"
        if (value == "[object Object]") {
            val link = Link(value)
            //here the magic of recursive OA's take place
            val attribute = ObjectAdapter(link as TObject) //, link.title, "Link")
            val key: String = member.id
            val dynObj = this.asDynamic()
            dynObj[key] = attribute
        }
    }

    fun selfLink(): Link? {
        val answer = links.find {
            it.rel == RelType.SELF.type
        }
        return answer
    }

}
