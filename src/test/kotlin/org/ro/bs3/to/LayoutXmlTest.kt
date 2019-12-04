package org.ro.bs3.to

import org.ro.bs3.parser.LayoutXmlHandler
import org.ro.urls.RESTFUL_MENUBARS

class LayoutXmlTest {

//    @Test
    fun testParseXmlLayout() {
        //given
        val xmlStr = RESTFUL_MENUBARS.str
//        val jsonStr = FR_OBJECT_LAYOUT
        //when
        val bs3 = LayoutXmlHandler.parse(xmlStr)
        console.log("[LayoutTest.testParseXmlLayout]")
        console.log(bs3)
//        val  layout = bs3 as Layout
    }

}
