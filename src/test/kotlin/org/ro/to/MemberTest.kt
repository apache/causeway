package org.ro.to

import kotlinx.serialization.json.JsonObject
import org.ro.URLS
import org.ro.core.model.ObjectAdapter
import org.ro.core.model.ObjectList
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MemberTest() {

    @Test  // http://localhost:8080/restful/domain-types/isisApplib.FixtureResult/properties/className
    fun testParse() {
        val jsonObj = JSON.parse<JsonObject>(URLS.FR_PROPERTY_DESCRIPTION)
        val m = Member(jsonObj)
        val extension: Extensions? = m.getExtension()
        assertEquals("Result class", extension!!.friendlyName)
    }

    @Test  // http://localhost:8080/restful/objects/simple.SimpleObject/0
    fun testTObjectMembers() {
        val jsonObj = JSON.parse<JsonObject>(URLS.SO_0)
        val to: TObject = TObject(jsonObj)
        assertTrue(to.title == "Object: Foo")

        val members = to.getMembers()
        assertEquals(10, members.size)

        val properties = to.getProperties()
        assertEquals(4, properties.size)

        val objectList: ObjectList = ObjectList()
        objectList.initSize(1)
        to.addMembersAsProperties()
        val oa1: ObjectAdapter = ObjectAdapter(to)
        objectList.add(oa1)

        // this is kind of untyped again
        val oa: ObjectAdapter = objectList.last()
        //FIXME dynamic 
        /*
        assertTrue(oa.datanucleusIdLong == 0)
        assertTrue(oa.datanucleusVersionTimestamp == 1514897074953)
        assertTrue(oa.notes == "null")
        */
    }
    
}

