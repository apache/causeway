package org.ro.core.model


import org.ro.handler.TObjectHandler
import org.ro.to.FR_OBJECT_BAZ
import org.ro.to.Link
import org.ro.to.Member
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue


class ObjectAdapterTest {

    @Test
    fun testObjectBAZ() {
        // given
        val jsonStr = FR_OBJECT_BAZ.str
        val adaptee = TObjectHandler().parse(jsonStr)
        assertNotNull(adaptee)    //1

        // when
        val oa = ObjectAdapter(adaptee)

        // then
        val expectedTitle = "domain-app-demo/persist-all/item-3:  Object: Baz"
        val actualTitle: String = oa.adaptee.title
        assertEquals(expectedTitle, actualTitle)  //2

        assertTrue(oa.adaptee.hasOwnProperty("domainType"))    //3
        assertTrue(oa.adaptee.hasOwnProperty("instanceId"))       //4

        //Expectations: 
        // 1: adaptee( TObject.kt) has members (memberList?) mapped onto (dynamic) ObjectAdapter properties
        assertTrue(oa.adaptee.hasOwnProperty("members"))   //5 only internal (Object) attributes are 'adapted'
        val members = oa.adaptee.members
        val memberMap = members as LinkedHashMap<String, Member>
        assertNotNull(memberMap)              //6
        assertEquals(8, memberMap.size)    //7

        // 3: adaptee( TObject.kt) has links (linkList?) mapped onto (dynamic) ObjectAdapter properties
        assertTrue(oa.adaptee.hasOwnProperty("links"))   //8 only internal (Object) attributes are 'adapted'
        val links = oa.adaptee.links
        val linkList = links as ArrayList<Link>?
        assertNotNull(linkList)          //9
        assertEquals(4, linkList.size)  //10
    }

}