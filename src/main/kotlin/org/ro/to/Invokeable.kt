package org.ro.to

import kotlinx.serialization.json.JsonObject
import org.ro.core.event.ILogEventObserver
import org.ro.core.event.RoXmlHttpRequest

open class Invokeable(jsonObj: JsonObject? = null) : LinkedTO(jsonObj) {
    open val GET = "GET"
    open val PUT = "PUT"
    open val POST = "POST"
    open val DELETE = "DELETE"
    
    internal var href: String = ""
    internal var method: String = ""

    fun invoke(obs: ILogEventObserver? = null) {
        RoXmlHttpRequest().invoke(this, obs)
    }
    
}