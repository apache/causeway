package org.apache.isis.client.kroviz.to

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
        members.forEach {
            if (it.value.memberType == type) {
                result.add(it.value)
            }
        }
        return result
    }

    fun getProperty(key: String): Member? {
        members.forEach {
            if (it.key == key) {
                return it.value
            }
        }
        return null
    }

}
