package org.ro.core

import org.ro.to.Action

class MenuEntry(var title: String?, var id: String?, var action: Action) {
    internal var itemId: String? = null
    private var itemTitle: String? = null

    init {
        itemId = action.getInvokeLink()!!.href
        itemTitle = action.id
    }

}