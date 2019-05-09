package org.ro.core.model

import kotlinx.serialization.Serializable
import org.ro.to.TObject

@Serializable
class ObjectAdapter(val delegate: TObject) {
    //TODO have getters peek into delegate
    val resultClass = "resultClass"
    val fixtureScript = "fixtureScript"
    val resultKey: String
        get() {
            return delegate.title
        }
    val result: String
        get() {
          return delegate.domainType 
        }
    

} 