package org.ro.org.ro.core.model

class MetaClass(val name:String) {
    var actions = listOf<MetaAction>()
    var properties = listOf<MetaProperty>()
}
