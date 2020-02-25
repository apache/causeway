package org.ro.ui

import org.ro.to.DomainType

class PumlBuilder {

    fun with(domainType: DomainType): String {
        val cn = domainType.canonicalName
        val cls = cn.split(".").last()
        val pkg = cn.replace(".$cls", "")
        var pumlCode = "\"" +
                "@startuml\\n" +
                "package $pkg {\\n" +
                "class $cls\\n"
        domainType.members.forEach { m ->
            val memberName = m.name()
            when {
                m.isProperty() -> pumlCode += "$cls : $memberName\\n"
                else -> {
                    pumlCode += "$cls : $memberName()\\n"
                }
            }
        }
        pumlCode += "}\\n@enduml\""
        console.log("[PumlBuilder.with]")
        console.log(pumlCode)
        return pumlCode
    }

}
