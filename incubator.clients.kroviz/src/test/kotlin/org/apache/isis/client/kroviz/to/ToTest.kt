package org.apache.isis.client.kroviz.to

abstract class ToTest {
    fun Service.getMemberList(): List<Member> {
        val list = mutableListOf<Member>()
        for (m in members) {
            list.add(m.value)
        }
        return list
    }
}
