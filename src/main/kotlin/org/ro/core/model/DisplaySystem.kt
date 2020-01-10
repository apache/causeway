package org.ro.core.model

import org.ro.layout.Layout
import org.ro.to.DomainTypes
import org.ro.to.TransferObject
import org.ro.to.User
import org.ro.to.Version

class DisplaySystem(override val title: String) : BaseDisplayable() {
    override var layout: Layout? = null
    var user: User? = null
    var version: Version? = null
    private var domainTypes: DomainTypes? = null

    override fun canBeDisplayed(): Boolean {
        if (layout == null) {
            return false
        }
        return !isRendered
    }

    override fun addData(obj: TransferObject) {
        when(obj) {
            is User -> user = obj
            is Version -> version = obj
            is DomainTypes -> domainTypes = obj
            else -> {}
        }
    }

}
