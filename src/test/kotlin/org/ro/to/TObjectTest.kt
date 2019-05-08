package org.ro.to

import org.ro.core.model.ObjectList
import org.ro.handler.TObjectHandler
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class TObjectTest {

    @Test
    fun testParse() {
        val jsonStr = SO_0.str
        val to = TObjectHandler().parse(jsonStr) as TObject
        assertNotNull(to)
        assertNotNull(to.getLayoutLink())
    }

    @Test  // http://localhost:8080/restful/objects/simple.SimpleObject/0
    fun testTObjectMembers() {
        //TODO authors@ro.org members should be modeled as elements of an Array [] 
        val jsonStr = SO_0.str
        val to = TObjectHandler().parse(jsonStr) as TObject
        assertEquals("Object: Foo", to.links[0].title)

        val members = to.members
        assertEquals(10, members.size)
        val properties = to.getProperties()
        assertEquals(4, properties.size)

        val objectList = ObjectList()
        // objectList.initSize(1)
        to.addMembersAsProperties()
//        val oa1 = ObjectAdapter(to)
        objectList.list.add(to)

        // this is kind of untyped again
        // val oa: ObjectAdapter = objectList.last()
        /* FIXME dynamic
        assertNotNull(oa)

        assertTrue(oa.datanucleusIdLong == 0)
        assertTrue(oa.datanucleusVersionTimestamp == 1514897074953)
        assertTrue(oa.notes == "null") */
    }


}