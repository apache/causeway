package org.ro.to

import kotlinx.serialization.ImplicitReflectionSerializer
import org.ro.core.event.ILogEventObserver
import org.ro.core.event.RoXmlHttpRequest

@ImplicitReflectionSerializer
open class Invokeable(val href: String, val method: String = Method.GET.operation) {

    fun invoke(obs: ILogEventObserver? = null) {
        RoXmlHttpRequest().invoke(this, obs)
    }

}