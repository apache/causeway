package org.ro.core.model

import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import org.ro.to.Link
import org.ro.to.Member
import org.ro.to.TObject
import org.ro.urls.FR_OBJECT_BAZ
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
        val tObj = Json.parse(TObject.serializer(), jsonStr)
        val dynObj = tObj.asDynamic()

        // then
        assertNotNull(dynObj)    //1

        val expectedTitle = "domain-app-demo/persist-all/item-3:  Object: Baz"
        val actualTitle: String = dynObj.title
        assertEquals(expectedTitle, actualTitle)  //2

        assertTrue(dynObj.hasOwnProperty("domainType"))    //3
        assertTrue(dynObj.hasOwnProperty("instanceId"))       //4

        //Expectations:
        // 1:  has members (memberList?) mapped onto (dynamic) MemberExposer properties
        assertTrue(dynObj.hasOwnProperty("members"))   //5 only internal (Object) attributes are 'adapted'
        val members = dynObj.members
        val memberMap = members as LinkedHashMap<String, Member>
        assertNotNull(memberMap)              //6
        assertEquals(8, memberMap.size)    //7

        // 3:  has links (linkList?) mapped onto (dynamic) MemberExposer properties
        assertTrue(dynObj.hasOwnProperty("links"))   //8 only internal (Object) attributes are 'adapted'
        val links = tObj.links
        val linkList = links as ArrayList<Link>?
        assertNotNull(linkList)          //9
        assertEquals(4, linkList.size)  //10
    }

}
