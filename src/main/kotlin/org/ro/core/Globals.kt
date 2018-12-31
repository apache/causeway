package org.ro.core

import org.ro.handler.Dispatcher
import org.ro.view.RoView

/**
 * Single Point of Contact between view components and all other classes.
 *
 * - keeps track of session (url, uid, pw)
 * - single point of entry for: view, dispatcher
 *
 * @See https://en.wikipedia.org/wiki/Facade_pattern
 * @See https://en.wikipedia.org/wiki/Singleton_pattern
 */
object Globals {
    var session = Session
    var dispatcher = Dispatcher

    var view: RoView? = null
        get() {
            if (view == null) {
                view = RoView()
            }
            return view
        }
    
    /**
     * Should only be called from within this class,
     * but ActionScript does not support private constructors.
     */
    /*fun Globals(view: RoView? = null) {
        if (instance == null) {
            this.view = view
            instance = this
        }
    } */
    
}