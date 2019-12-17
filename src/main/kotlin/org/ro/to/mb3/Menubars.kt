package org.ro.to.mb3

import org.w3c.dom.Document
import org.w3c.dom.asList

class Menubars(menuBars: Document) {
    var primary: Menu
    var secondary: Menu
    var tertiary: Menu

    init {
        val root = menuBars.firstChild
        val kids = root!!.childNodes
        val mbNodes = kids.asList()
        val mbList = mbNodes.filter { it.nodeName.startsWith("mb3") }
        primary = Menu(mbList.get(0))
        secondary = Menu(mbList.get(1))
        tertiary = Menu(mbList.get(2))
    }

}
