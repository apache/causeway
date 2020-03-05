package org.ro.to

import kotlinx.serialization.UnstableDefault
import org.ro.utils.Utils
import org.ro.handler.TObjectHandler
import org.ro.snapshots.demo2_0_0.ACTIONS_TEXT_INVOKE
import org.ro.snapshots.demo2_0_0.ISIS_SECURITY_ME_SERVICE
import org.ro.snapshots.simpleapp1_16_0.SO_0
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@UnstableDefault
class TObjectTest {

    @Test
    fun testIsisSecurityMe() {
        console.log("[TObjectTest.testIsisSecurityMe]")
        //given
        val jsonStr = ISIS_SECURITY_ME_SERVICE.str
        // when
        val tObject = TObjectHandler().parse(jsonStr) as TObject
        val members = tObject.members
        // then
        assertEquals(27, members.size)
    }

    @Test
    fun testPropertiesChanged() {
        //given
        val jsonStr = SO_0.str
        // when
        val tObject = TObjectHandler().parse(jsonStr) as TObject
        val properties = tObject.getProperties()
        // then
        val mutable = properties.filter { it.isReadWrite() }
        assertEquals(1, mutable.size)

        //when
        mutable.first().value!!.content = "l on the hill"
        //then
        val putBody = Utils.propertiesAsBody(tObject)
        assertTrue(putBody.contains("notes"))
        assertTrue(putBody.contains("value"))
        assertTrue(putBody.contains("l on the hill"))
    }

    @Test
    fun testLinksMembersProperties() {
        //given
        val jsonStr = SO_0.str
        // when
        val to = TObjectHandler().parse(jsonStr) as TObject
        val members = to.members
        val properties = to.getProperties()
        // then
        assertNotNull(to.links)
        assertEquals("Object: Foo", to.links[0].title)
        assertEquals(10, members.size)
        assertEquals(4, properties.size)

        val namedMembers = properties.filter { it.id == "name" }
        assertEquals(1, namedMembers.size)

        val nameMember = namedMembers.first()
        val content = nameMember.value!!.content as String
        assertEquals("Foo", content)
    }

    @Test
    fun testTextDemo() {
        //given
        val jsonStr = ACTIONS_TEXT_INVOKE.str
        // when
        val to = TObjectHandler().parse(jsonStr) as TObject
        val members = to.members
        val properties = to.getProperties()
        // then
        assertNotNull(to.links)
        assertEquals("TextDemo", to.links[0].title)
        assertEquals(6, members.size)
        assertEquals(5, properties.size)

        val filteredProperties = properties.filter { it.id == "description" }
        assertEquals(1, filteredProperties.size)

        val description = filteredProperties.first()
        val content = description.value!!.content as String
        assertTrue(content.startsWith("<div") && content.endsWith("div>"))
    }

}
