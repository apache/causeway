package org.ro.core.model

import kotlinx.serialization.UnstableDefault
import org.ro.handler.TObjectHandler
import org.ro.to.FR_OBJECT_BAZ
import org.ro.to.Link
import org.ro.to.Member
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@UnstableDefault
class FixtureResultTest {

    @Test
    fun testObjectBAZ() {
        // given
        val jsonStr = FR_OBJECT_BAZ.str

        // when
        val tObj: dynamic = TObjectHandler().parse(jsonStr)

        // then
        assertNotNull(tObj)    //1

        val expectedTitle = "domain-app-demo/persist-all/item-3:  Object: Baz"
        val actualTitle: String = tObj.title
        assertEquals(expectedTitle, actualTitle)  //2

        assertTrue(tObj.hasOwnProperty("domainType"))    //3
        assertTrue(tObj.hasOwnProperty("instanceId"))       //4

        //Expectations:
        // 1:  has members (memberList?) mapped onto (dynamic) Revealator properties
        assertTrue(tObj.hasOwnProperty("members"))   //5 only internal (Object) attributes are 'adapted'
        val members = tObj.members
        val memberMap = members as LinkedHashMap<String, Member>
        assertNotNull(memberMap)              //6
        assertEquals(8, memberMap.size)    //7

        // 3:  has links (linkList?) mapped onto (dynamic) Revealator properties
        assertTrue(tObj.hasOwnProperty("links"))   //8 only internal (Object) attributes are 'adapted'
        val links = tObj.links
        val linkList = links as ArrayList<Link>?
        assertNotNull(linkList)          //9
        assertEquals(4, linkList.size)  //10
    }

}
