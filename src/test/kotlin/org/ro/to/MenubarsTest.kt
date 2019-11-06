package org.ro.to

import kotlinx.serialization.UnstableDefault
import org.ro.handler.ResultListHandler
import org.ro.urls.RESTFUL_MENUBARS

@UnstableDefault
class MenubarsTest {

    //@Test
    fun testParse() {
        val list = ResultListHandler().parse(RESTFUL_MENUBARS.str) as ResultList
        //TODO implements userName: String, roles [], links [], extensions
    }
}
