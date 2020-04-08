package org.apache.isis.client.kroviz.core.model.meta

class MetaPackage(val name: String) {
    val classes = mutableSetOf<MetaClass>()
}
