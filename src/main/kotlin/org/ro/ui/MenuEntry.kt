package org.ro.ui

import org.ro.to.Member

class MenuEntry(val title: String, val id: String, val action: Member) {
    var itemId: String
    private var itemTitle: String

    init {
        itemId = action.getInvokeLink()!!.href
        itemTitle = action.id
    }

}
