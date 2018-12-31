package org.ro.to

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.boolean
import org.ro.core.Utils

class Extensions(jsonObj: JsonObject? = null) : BaseTO() {
    val PRIMARY: String = "primary"
    val SECONDARY: String = "secondary"
    val IDEMPOTENT: String = "idempotent"
    val NON_IDEMPOTENT: String = "nonIdempotent"
    val NON_IDEMPOTENT_ARE_YOU_SURE: String = "nonIdempotentAreYouSure"

    private var oid: String? = null
    internal var isService  = false // is this default sensible?
    private var isPersistent: Boolean? = null
    private var menuBar: String? = null // TODO use constants [PRIMARY, , etc.]
    private var actionSemantics: String? = null //enum? nonIdempotent, idempotent, nonIdempotentAreYouSure, etc.
    internal var actionType= ""
    internal var xIsisFormat: String? = null
    internal var friendlyName = ""
    private var collectionSemantics: String? = null

    init {
        if (jsonObj != null) {
            val json: JsonObject = fixXIsisFormat(jsonObj)
            oid = json["oid"].toString()
            isService = json["isService"].boolean
            isPersistent = json["isPersistent"].boolean
            menuBar = json["menuBar"].toString()
            actionSemantics = json["actionSemantics"].toString()
            actionType = json["actionType"].toString()
            xIsisFormat = json["xIsisFormat"].toString()
            friendlyName = json["friendlyName"].toString()
            collectionSemantics = json["collectionSemantics"].toString()
        }
    }

    //Workaround for https://issues.apache.org/jira/browse/ISIS-1850 would break RO Spec 1.0
    private fun fixXIsisFormat(json: JsonObject): JsonObject {
        val search = "\"x-isis-format\":"
        val replace = "\"xIsisFormat\":"
        return Utils().replace(json, search, replace)
    }

}