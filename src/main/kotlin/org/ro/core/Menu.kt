package org.ro.core

import org.ro.to.Member
import org.ro.to.MemberType
import org.ro.to.Service

object Menu {
    //TODO chekck if limit can be avoided by the use of observeableList
    var limit: Int = 0
    var list = mutableListOf<MenuEntry>()

    fun isFull(): Boolean {
        return list.size >= limit
    }

    fun add(service: Service) {
        val title = service.title
        val id = service.serviceId
        for (action in service.getActionList()) {
            val me = MenuEntry(title, id, action)
            list.add(me)
        }
    }

    fun filterUniqueMenuTitles(): MutableList<String> {
        val titles = mutableListOf<String>()
        for (me in list) {
            titles.add(me.title)
        }
        return titles.distinct().toMutableList()
    }

    fun filterEntriesByTitle(title: String): MutableList<MenuEntry> {
        val result = mutableListOf<MenuEntry>()
        for (me in list) {
            if (me.title == title) {
                result.add(me)
            }
        }
        return result
    }

    fun Service.getActionList(): List<Member> {
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