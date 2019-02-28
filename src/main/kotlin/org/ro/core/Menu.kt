package org.ro.core

import org.ro.to.Service

object Menu {
    //TODO are services required here at all?
    var menuItems = mutableListOf<MenuEntry>()

    fun add(service: Service) {
        val title = service.title
        val id = service.serviceId
        for (action in service.getActionList()) {
            val me = MenuEntry(title, id, action)
            menuItems.add(me)
        }
    }

    fun uniqueMenuTitles(): MutableList<String> {
        val titles = mutableListOf<String>()
        for (me in menuItems) {
            titles.add(me.title)
        }
        return titles.distinct().toMutableList()
    }
/*    
    fun findEntriesByTitle(title: String): MutableList<MenuEntry> {
        val result = mutableListOf<MenuEntry>()
        for (me in menuItems) {
            if (me.title == title) {
                result.add(me)
            }
        }
        return result
    }

    fun findAction(url: String): Member? {
        for (me in menuItems) {
            if (me.itemId == url) {
                return me.action
            }
        }
        return null
    }
         */
}