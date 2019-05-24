package org.ro.to

import org.ro.core.model.ObjectAdapter
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
        assertNotNull(to.links)
    }

    @Test
    fun testMembers() {
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
        val oa1 = ObjectAdapter(to)
        objectList.list.add(oa1)

        val oa: ObjectAdapter = objectList.last()!!
        console.log("[TOT.testMembers] $oa")
        //FIXME dynamic
//        assertEquals("0", oa.get("datanucleusIdLong"))
//        assertEquals("1514897074953", oa.get("datanucleusVersionTimestamp"))
//        assertEquals("null", oa.get("notes"))
    }
    
}