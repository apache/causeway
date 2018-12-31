package org.ro.to

import kotlinx.serialization.json.JsonObject
import org.ro.core.Utils

/**
 *  Common 'abstract' superclass of Transfer Objects with 'links'.
 *  No Constructor.
 */
open class LinkedTO(jsonObj: JsonObject? = null) : BaseTO() {
    var linkList = mutableListOf<Link>()

    init {
        if (jsonObj != null) {
            val links = jsonObj["links"].jsonArray
            for (l in links) {
                linkList.add(Link(l as JsonObject))
            }
        }
    }

    fun getLayoutLink(): Link? {
        var href: String?
        for (l in linkList) {
            href = l.href
            //TODO can be "object-layout" >= 1.16
            if (href.isNotEmpty()) {
                if (Utils().endsWith(href, "layout")) {
                    return l
                }
            }
        }
        return null
    }

}