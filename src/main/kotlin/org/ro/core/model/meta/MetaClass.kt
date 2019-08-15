package org.ro.core.model.meta

class MetaClass(val name: String) {
    var actions = listOf<MetaAction>()
    var properties = listOf<MetaProperty>()
}
