package org.apache.isis.client.kroviz.core.model.meta

import org.apache.isis.client.kroviz.to.DomainType

class MetaClass(val domainType: DomainType) {
    var name: String
    var pkg: MetaPackage
    val actions = mutableSetOf<MetaAction>()
    val properties = mutableSetOf<MetaProperty>()

    init {
        val cn = domainType.canonicalName
        name = cn.split(".").last()
        val pkgName = cn.replace(".$name", "")
        pkg = MetaPackage(pkgName)

        domainType.members.forEach { m ->
            val memberName = m.name()
            when {
                m.isProperty() -> properties.add(MetaProperty(memberName))
                m.isAction() -> actions.add(MetaAction(memberName))
                else -> {
                    throw Throwable("tertium non datur")
                }
            }
        }
    }

}
