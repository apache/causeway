package org.apache.isis.client.kroviz.core.model

import org.apache.isis.client.kroviz.to.DomainTypes
import org.apache.isis.client.kroviz.to.TransferObject
import org.apache.isis.client.kroviz.to.User
import org.apache.isis.client.kroviz.to.Version

class SystemDM(override val title: String) : DisplayModel() {
    var user: User? = null
    var version: Version? = null
    private var domainTypes: DomainTypes? = null

    override fun canBeDisplayed(): Boolean {
        return !isRendered
    }

    override fun addData(obj: TransferObject) {
        when (obj) {
            is User -> user = obj
            is Version -> version = obj
            is DomainTypes -> domainTypes = obj
            else -> {
            }
        }
    }

}
