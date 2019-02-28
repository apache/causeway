package org.ro.core.model


import org.ro.handler.TObjectHandler
import org.ro.to.FR_OBJECT_BAZ
import pl.treksoft.kvision.html.Image
import kotlin.test.Test
import kotlin.test.assertNotNull


class ObjectAdapterTest {

    @Test
    fun testObjectBAZ() {
        // given
        val jsonStr = FR_OBJECT_BAZ.str
        val adaptee = TObjectHandler().parse(jsonStr) 
        assertNotNull(adaptee)

        val title = "test title"
        val type = "Link"
        val icon: Image? = null
        // when
        val oa = ObjectAdapter(adaptee, title, type, icon)

        // then
        val expectedTitle = "domain-app-demo/persist-all/item-3:  Object: Baz"
        /* FIXME dynamic
        val actualTitle: String = oa.title
        assertEquals(expectedTitle, actualTitle)

        assertTrue(oa.hasOwnProperty("domainType"))
        assertTrue(oa.hasOwnProperty("instanceId"))

        //Expectations: 
        // 1: adaptee( TObject.kt) has members (memberList?) mapped onto (dynamic) ObjectAdapter properties
        assertFalse(oa.hasOwnProperty("members"))   // only internal (Object) attributes are 'adapted'
        assertTrue(oa.hasOwnProperty("memberList"))    // objectLists need to be public?
        val memberList = oa.memberList
        assertTrue(adaptee.getMembers() == memberList)

        // 2: icon is instance of ObjectIconRenderer
        val iconClass: Image = oa.getIcon()
        assertNotNull(iconClass)

        // 3: adaptee( TObject.kt) has links (linkList?) mapped onto (dynamic) ObjectAdapter properties
        assertFalse(oa.hasOwnProperty("links"))   // only internal (Object) attributes are 'adapted'
        assertTrue(oa.hasOwnProperty("linkList"))   // objectLists need to be public?
        assertTrue(adaptee.linkList.size == oa.linkList.length)
        */
    }

}