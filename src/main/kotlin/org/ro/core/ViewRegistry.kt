package org.ro.core

import pl.treksoft.kvision.utils.Object

/**
 * Keeps a list of closed/minmized/docked views in order to recreate them.
 * When a tab is 'docked' it can be looked up here.
 * And instead of creating a view a second time, it can be taken from here.
 * setVisible(false) ?
 *
 * A unique id is required in order to be able to look it up and setVisible(true) again.
 */
class ViewRegistry {
    private var delegate = mutableMapOf<Any, Any>()

    fun add(key: Object, value: Object) {
        delegate[key] = value
    }

    fun remove(key: Any) {
        // is the key itself removed as well?
        delegate.remove(key)
    }

    fun find(key: Any): Any? {
        return delegate[key]
    }
}