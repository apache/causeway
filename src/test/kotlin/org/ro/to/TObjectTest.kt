package org.ro.to

import kotlinx.serialization.UnstableDefault
import org.ro.core.Utils
import org.ro.handler.TObjectHandler
import org.ro.snapshots.ai1_16_0.SO_0
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@UnstableDefault
class TObjectTest {

    @Test
    fun testPropertiesChanged() {
        console.log("[TOT.testPropertiesChanged]")
        //given
        val jsonStr = SO_0.str
        // when
        val tObject = TObjectHandler().parse(jsonStr) as TObject
        val properties = tObject.getProperties()
        // then
        val mutable = properties.filter { it.isReadWrite() }
        assertEquals(1, mutable.size)

        //when
        console.log(mutable.first())
        console.log(mutable.first().value)
        mutable.first().value!!.content = "l on the hill"
        //then
        val putBody = Utils.propertiesAsBody(tObject)
        assertTrue(putBody.contains("notes") )
        assertTrue(putBody.contains("value") )
        assertTrue(putBody.contains("l on the hill") )
        // should contain as well: 3*{, 3*}, 2*:, 6*"
        console.log(putBody)
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

        //val matchingPredicate = { it.value == 0 }
        val namedMembers = properties.filter { it.id == "name" }
        assertEquals(1, namedMembers.size)

        val nameMember = namedMembers.first()
        val content = nameMember.value!!.content as String
        assertEquals("Foo", content)
    }

}
