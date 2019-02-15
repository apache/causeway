package org.ro.core

import org.ro.to.Member

class MenuEntry(var title: String?, var id: String?, var action: Member) {
    internal var itemId: String? = null
    private var itemTitle: String? = null

    init {
        itemId = action.getInvokeLink()!!.href
        itemTitle = action.id
    }

}