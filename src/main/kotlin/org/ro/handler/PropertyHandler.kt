package org.ro.handler

import kotlinx.serialization.json.JsonObject
import org.ro.to.Link
import org.ro.to.Property

class PropertyHandler : AbstractHandler(), IResponseHandler {

    override fun canHandle(jsonStr: String): Boolean {
        /*val links =  jsonObj["links"].jsonArray
        if (links.isNotEmpty()) {
            val s = links.size
            var jso: JsonObject
            if (s == 3) {
                jso = links[2].jsonObject
                return isLinkDescribedBy(jso)
            }
            if (s == 5) {
                jso = links[4].jsonObject
                return isLinkDescribedBy(jso)
            }
        }       */
        return false
    }
    
    private fun isLinkDescribedBy(jso:JsonObject): Boolean {
        val ljs = jso["link"].jsonObject
        val link = Link(ljs)
        return (link.rel == Property().DESCRIBED_BY)
    }

    override fun doHandle(jsonStr: String) {
        val p =  Property()
        p.descriptionLink()?.invoke()
    }

}