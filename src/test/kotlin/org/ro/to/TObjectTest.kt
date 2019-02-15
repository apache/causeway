package org.ro.to

import kotlinx.serialization.ImplicitReflectionSerializer
import org.ro.core.model.ObjectAdapter
import org.ro.core.model.ObjectList
import org.ro.handler.TObjectHandler
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@ImplicitReflectionSerializer
class TObjectTest {

    @Test
    fun testParse() {
        val jsonStr = SO_0.str
        val to = TObjectHandler().parse(jsonStr)
        assertNotNull(to)
        assertNotNull(to.getLayoutLink())
    }

    @Test  // http://localhost:8080/restful/objects/simple.SimpleObject/0
    fun testTObjectMembers() {
        //FIXME authors@ro.org members should be modeled as elements of an Array [] 
        //Expected '[, kind: [object Object]'
        // some kind of custom serializer is required to handle it as is
        val jsonStr = SO_0.str
        val to = TObjectHandler().parse(jsonStr)
        assertEquals("Object: Foo", to.links[0].title)

        val members = to.members
        assertEquals(10, members.size)
        val properties = to.getProperties()
        assertEquals(4, properties.size)

        val objectList = ObjectList()
        objectList.initSize(1)
        to.addMembersAsProperties()
        val oa1 = ObjectAdapter(to)
        objectList.add(oa1)

        // this is kind of untyped again
        val oa: ObjectAdapter = objectList.last()
        /* FIXME dynamic
        assertNotNull(oa)

        assertTrue(oa.datanucleusIdLong == 0)
        assertTrue(oa.datanucleusVersionTimestamp == 1514897074953)
        assertTrue(oa.notes == "null") */
    }


}