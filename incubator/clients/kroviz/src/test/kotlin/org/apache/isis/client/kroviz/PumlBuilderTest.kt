package org.apache.isis.client.kroviz

import kotlinx.serialization.UnstableDefault
import org.apache.isis.client.kroviz.handler.DomainTypeHandler
import org.apache.isis.client.kroviz.snapshots.simpleapp1_16_0.SO
import org.apache.isis.client.kroviz.to.DomainType
import org.apache.isis.client.kroviz.ui.PumlBuilder
import kotlin.test.Test
import kotlin.test.assertTrue

class PumlBuilderTest {

    val pkg = "domainapp.modules.simple.dom.impl"
    val cls = "SimpleObject"

    @UnstableDefault
    @Test
    fun testSimpleObject() {
        //given
        val jsonStr = SO.str
        val domainType = DomainTypeHandler().parse(jsonStr) as DomainType

        //when
        val actual = PumlBuilder().with(domainType)
        //
        assertTrue(actual.startsWith("\"@startuml"))
        assertTrue(actual.endsWith("@enduml\""))
        assertTrue(actual.contains("package $pkg {\\n"))
        assertTrue(actual.contains("class $cls\\n"))
    }

}
