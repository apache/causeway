package org.ro.to

import kotlinx.serialization.Serializable

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

    fun getActions(): MutableList<Member> {
        val result = mutableListOf<Member>()
        for (m in members) {
            if (m.value.memberType == MemberType.ACTION.type) {
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

}
