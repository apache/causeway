package org.ro.core.model.meta

class MetaPackage(val name: String) {
    val classes = mutableSetOf<MetaClass>()
}
