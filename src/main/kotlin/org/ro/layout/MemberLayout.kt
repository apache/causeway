package org.ro.layout

import kotlinx.serialization.json.JsonObject
import org.ro.to.Link

/**
 * abstract superclass of PropertyLayout, ActionLayout
 */
open class MemberLayout(jsonObj: JsonObject? = null) : AbstractLayout() {
    var named: String? = null
    private var describedAs: String? = null
    var metadataError: String? = null
    //    var link: String? = null
    protected var linkObject: Link? = null
    var id: String? = null
    var hidden: Boolean? = null
    var namedEscaped: String? = null
    var promptStyle: JsonObject? = null

    init {
        val link = jsonObj!!["link"].jsonObject
        linkObject = Link(link)
        describedAs = jsonObj["describedAs"].toString()
    }

    fun getLink(): Link? {
        return linkObject
    }

}