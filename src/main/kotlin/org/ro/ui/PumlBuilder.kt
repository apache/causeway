package org.ro.ui

import org.ro.to.DomainType

class PumlBuilder {

    fun with(domaintype: DomainType) : String {
        val pkg = "domainapp.modules.simple.dom.impl"
        val cls = "SimpleObject"
        val prp = "name String"
        val mth =  "rebuildMetamodel"
        val defaultPumlCode = "\"" +
                "@startuml\\n" +
                "package $pkg {\\n" +
                "class $cls\\n" +
                "$cls : $prp\\n" +
                "$cls : $mth()\\n" +
                "}\\n" +
                "@enduml\""
        console.log("[PumlBuilder.with]")
        console.log(defaultPumlCode)
        return defaultPumlCode
    }
}
