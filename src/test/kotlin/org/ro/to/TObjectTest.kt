package org.ro.to

import kotlinx.serialization.UnstableDefault
import org.ro.handler.TObjectHandler
import org.ro.snapshots.ai1_16_0.SO_0
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@UnstableDefault
class TObjectTest {

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
