package org.ro.core

import org.ro.to.Action
import org.ro.to.Member
import org.ro.to.Service

class Menu(private var limit: Int) {
    private var count: Int = 0
    var menuItems: MutableList<MenuEntry> = mutableListOf()

    fun init(service: Service, invokableList: List<Member>): Boolean {
        for (i in invokableList) {
            val title: String? = service.title
            val id: String? = service.serviceId
            val action = i as Action
            val me = MenuEntry(title, id, action)
            menuItems.add(me)
        }
        count += 1
        return (this.count >= limit)
    }

    fun uniqueMenuTitles(): MutableList<String> {
        val titles = mutableListOf<String>()
        for (me in menuItems) {
            titles.add(me.title!!)

        }
        return collectUnique(titles)
    }

    private fun collectUnique(titles: MutableList<String>): MutableList<String> {
        return titles.distinct().toMutableList()
    }

    fun findEntriesByTitle(title: String): MutableList<MenuEntry> {
        val result = mutableListOf<MenuEntry>()
        for (me in menuItems) {
            if (me.title == title) {
                result.add(me)
            }
        }
        return result
    }

    fun findAction(url: String): Action? {
        for (me in menuItems) {
            if (me.itemId == url) {
                return me.action
            }
        }
        return null
    }

}