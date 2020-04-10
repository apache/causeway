package org.apache.isis.client.kroviz.ui.kv

import pl.treksoft.kvision.core.Component
import pl.treksoft.kvision.utils.isIE11

internal val roManagerBootstrapInit = RoManagerBootstrap.init()

/**
 * Internal singleton object which initializes and configures KVision Bootstrap module.
 */
internal object RoManagerBootstrap {
    init {
        pl.treksoft.kvision.require("bootstrap/dist/js/bootstrap.bundle.min.js")
        pl.treksoft.kvision.require("awesome-bootstrap-checkbox")
    }

    private val elementResizeEvent = pl.treksoft.kvision.require("element-resize-event")

    @Suppress("UnsafeCastFromDynamic")
    internal fun setResizeEvent(component: Component, callback: () -> Unit) {
        if (!isIE11()) {
            component.getElement()?.let {
                elementResizeEvent(it, callback)
            }
        }
    }

    @Suppress("UnsafeCastFromDynamic")
    internal fun clearResizeEvent(component: Component) {
        if (!isIE11()) {
            if (component.getElement()?.asDynamic()?.__resizeTrigger__?.contentDocument != null) {
                component.getElement()?.let {
                    elementResizeEvent.unbind(it)
                }
            }
        }
    }

    internal fun init() {}
}
