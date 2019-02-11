package org.ro.core.model

import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.json.JSON
import org.ro.to.FR_OBJECT_BAZ
import org.ro.to.TObject
import pl.treksoft.kvision.html.Image
import kotlin.test.assertNotNull

@ImplicitReflectionSerializer
class ObjectAdapterTest {

    // FIXME members has a list of named elements @Test
    fun testObjectBAZ() {
        // given
        val adaptee = JSON.parse(TObject.serializer(), FR_OBJECT_BAZ.str)
        assertNotNull(adaptee)

        val title = "test title"
        val type = "Link"
        val icon: Image? = null
        // when
        val oa = ObjectAdapter(adaptee, title, type, icon)

        // then
        val expectedTitle = "domain-app-demo/persist-all/item-3:  Object: Baz"
        //FIXME
        /*
        val actualTitle: String = oa.title
        assertEquals(expectedTitle, actualTitle)

        assertTrue(oa.hasOwnProperty("domainType"))
        assertTrue(oa.hasOwnProperty("instanceId"))

        //Expectations: 
        // 1: adaptee( TObject) has members (memberList?) mapped onto (dynamic) ObjectAdapter properties
        assertFalse(oa.hasOwnProperty("members"))   // only internal (Object) attributes are 'adapted'
        assertTrue(oa.hasOwnProperty("memberList"))    // objectLists need to be public?
        val memberList = oa.memberList
        assertTrue(adaptee.getMembers() == memberList)

        // 2: icon is instance of ObjectIconRenderer
        val iconClass: Image = oa.getIcon()
        assertNotNull(iconClass)

        // 3: adaptee( TObject) has links (linkList?) mapped onto (dynamic) ObjectAdapter properties
        assertFalse(oa.hasOwnProperty("links"))   // only internal (Object) attributes are 'adapted'
        assertTrue(oa.hasOwnProperty("linkList"))   // objectLists need to be public?
        assertTrue(adaptee.linkList.size == oa.linkList.length)
        */
    }

}