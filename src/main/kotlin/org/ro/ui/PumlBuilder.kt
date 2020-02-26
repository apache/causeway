package org.ro.ui

import org.ro.core.model.meta.MetaClass
import org.ro.core.model.meta.MetaPackage
import org.ro.to.DomainType

class PumlBuilder {

    private val Q = "\""
    private val NL = "\\n"

    fun with(pkgList: Set<MetaPackage>): String {
        var code = "$Q@startuml$NL"
        pkgList.forEach { p ->
            code += amendByPackage(p)
        }
        code += "@enduml$Q"
        return code
    }

    fun with(pkg: MetaPackage): String {
        var code = "$Q@startuml$NL"
        code += amendByPackage(pkg)
        code += "@enduml$Q"
        return code
    }

    private fun amendByPackage(pkg: MetaPackage): String {
        val packageName = pkg.name
        var code = "package $packageName {$NL"
        pkg.classes.forEach { cls ->
            code += amendByClass(cls)
        }
        code += "}$NL"
        return code
    }

    private fun amendByClass(cls: MetaClass): String {
        val className = cls.name
        var code = "class $className$NL"
        cls.properties.forEach { p ->
            code += "$className : ${p.name}$NL"
        }
        cls.actions.forEach { a ->
            code += "$className : ${a.name}()$NL"
        }
        return code
    }

    fun with(domainType: DomainType): String {
        val cn = domainType.canonicalName
        val cls = cn.split(".").last()
        val pkg = cn.replace(".$cls", "")
        var pumlCode = "$Q@startuml$NL package $pkg {$NL" +
                "class $cls$NL"
        domainType.members.forEach { m ->
            val memberName = m.name()
            when {
                m.isProperty() -> pumlCode += "$cls : $memberName$NL"
                else -> {
                    pumlCode += "$cls : $memberName()$NL"
                }
            }
        }
        pumlCode += "}$NL@enduml$Q"
        console.log("[PumlBuilder.with]")
        console.log(pumlCode)
        return pumlCode
    }

}
