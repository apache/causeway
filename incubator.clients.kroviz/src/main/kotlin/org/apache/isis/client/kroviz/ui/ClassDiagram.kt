package org.apache.isis.client.kroviz.ui

import org.apache.isis.client.kroviz.core.model.DiagramDM
import org.apache.isis.client.kroviz.core.model.meta.MetaClass
import org.apache.isis.client.kroviz.core.model.meta.MetaPackage
import org.apache.isis.client.kroviz.to.DomainType

object ClassDiagram {

    fun buildDiagramCode(dd: DiagramDM): String {
        val domainTypeList: Set<DomainType> = dd.classes
        //TODO properties needed to set type
        //val properties: Set<Property> = dd.properties
        val packages = mutableSetOf<MetaPackage>()
        domainTypeList.forEach { dt ->
            val cls = MetaClass(dt)
            val pkgName = cls.pkg.name
            var pkg = packages.find { p -> p.name == pkgName }
            if (pkg == null) {
                pkg = cls.pkg
                pkg.classes.add(cls)
                packages.add(pkg)
            } else {
                pkg.classes.add(cls)
            }
        }
        return PumlBuilder().with(packages)
    }

}
