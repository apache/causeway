package org.ro.to

import kotlinx.serialization.Serializable

@Serializable
data class TObject(val links: List<Link> = emptyList(),
                   val extensions: Extensions,
                   val title: String = "",
                   val domainType: String = "",
                   val instanceId: String? = null,
                   val members: Map<String, Member> = emptyMap()
) : TransferObject {

    fun getProperties(): MutableList<Member> {
        return getMembersOfType(MemberType.PROPERTY.type)
    }

    fun getActions(): MutableList<Member> {
        return getMembersOfType(MemberType.ACTION.type)
    }

    private fun getMembersOfType(type: String): MutableList<Member> {
        val result = mutableListOf<Member>()
        for (m in members) {
            if (m.value.memberType == type) {
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
