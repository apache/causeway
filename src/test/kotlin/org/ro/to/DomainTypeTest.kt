package org.ro.to

import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import org.ro.snapshots.demo2_0_0.DEMO_FILE_NODE
import org.ro.snapshots.simpleapp1_16_0.SO
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@UnstableDefault
class DomainTypeTest {

    @Test
    fun testParseSimpleObject() {
        // given
        val jsonStr = SO.str
        // when
        val domainType = Json.parse(DomainType.serializer(), jsonStr)
        // then
        val linkList = domainType.links
        assertEquals(2, linkList.size)

        assertEquals("domainapp.modules.simple.dom.impl.SimpleObject", domainType.canonicalName)

        val members = domainType.members
        assertEquals(9, members.size)

        val typeActions = domainType.typeActions
        assertEquals(2, typeActions.size)

        assertNotNull(domainType.extensions)
    }

    @Test
    fun testParseFileNode() {
        // given
        val jsonStr = DEMO_FILE_NODE.str
        // when
        val domainType = Json.parse(DomainType.serializer(), jsonStr)
        // then
        val linkList = domainType.links
        assertEquals(2, linkList.size)

        assertEquals("demoapp.dom.tree.FileNode", domainType.canonicalName)

        val members = domainType.members
        assertEquals(8, members.size)

        val typeActions = domainType.typeActions
        assertEquals(2, typeActions.size)

        assertNotNull(domainType.extensions)
    }

}
