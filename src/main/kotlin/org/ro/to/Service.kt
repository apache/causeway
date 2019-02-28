package org.ro.to

import kotlinx.serialization.Serializable

@Serializable
data class Service(val links: List<Link> = emptyList(),
                   val extensions: Extensions? = null,
                   val title: String = "",
                   val serviceId: String = "",
                   val members: Map<String, Member> = emptyMap()) {

    fun getMemberList(): List<Member> {
        val list = mutableListOf<Member>()
        for (m in members) {
            list.add(m.value)
        }
        return list
    }

    fun getActionList(): List<Member> {
        val list = mutableListOf<Member>()
        for (m in members) {
            val v = m.value
            if (v.memberType.equals(MemberType.ACTION.type)) {
                list.add(v)
            }
        }
        return list
    }
}