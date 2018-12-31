package org.ro.to

import kotlinx.serialization.json.JsonObject
import org.ro.core.Menu
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class MenuTest {

    @Test
    fun testUnique() {
        //given:
        val jsonObj1 = JSON.parse<JsonObject>(jsonStr1)
        val s1 = Service(jsonObj1)
        val m1 = s1.getMembers()
        val jsonObj2 = JSON.parse<JsonObject>(jsonStr2)
        val s2 = Service(jsonObj2)
        val m2 = s2.getMembers()
        //when
        val menu = Menu(2)
        menu.init(s1, m1)
        menu.init(s2, m2)
        //then
        val size: Int = menu.uniqueMenuTitles().size
        assertTrue(1 == size)
    }

    @Test
    fun testParse() {
        val jsonObj = JSON.parse<JsonObject>(jsonStr)
        val service = Service(jsonObj)
        val members = service.getMembers()
        val menu = Menu(1)
        menu.init(service, members)
        assertNotNull(menu)
    }

    // http://localhost:8080/restful/services/simple.SimpleObjectMenu
    private val jsonStr = """{
        "links": [
        {
            "rel": "self",
            "href": "http://localhost:8080/restful/services/simple.SimpleObjectMenu",
            "method": "GET",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
            "title": "Simple Objects"
        },
        {
            "rel": "describedby",
            "href": "http://localhost:8080/restful/domain-types/simple.SimpleObjectMenu",
            "method": "GET",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.apache.isis.restfulobjects:rels/layout",
            "href": "http://localhost:8080/restful/domain-types/simple.SimpleObjectMenu/layout",
            "method": "GET",
            "type": "application/xmlprofile=\"urn:org.restfulobjects:repr-types/layout-bs3\""
        },
        {
            "rel": "up",
            "href": "http://localhost:8080/restful/services",
            "method": "GET",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/list\""
        }
        ],
        "extensions": {
        "oid": "simple.SimpleObjectMenu:1",
        "isService": true,
        "isPersistent": true,
        "menuBar": "PRIMARY"
    },
        "title": "Simple Objects",
        "serviceId": "simple.SimpleObjectMenu",
        "members": {
        "listAll": {
        "id": "listAll",
        "memberType": "action",
        "links": [
        {
            "rel": "urn:org.restfulobjects:rels/detailsaction=\"listAll\"",
            "href": "http://localhost:8080/restful/services/simple.SimpleObjectMenu/actions/listAll",
            "method": "GET",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object-action\""
        }
        ]
    },
        "findByName": {
        "id": "findByName",
        "memberType": "action",
        "links": [
        {
            "rel": "urn:org.restfulobjects:rels/detailsaction=\"findByName\"",
            "href": "http://localhost:8080/restful/services/simple.SimpleObjectMenu/actions/findByName",
            "method": "GET",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object-action\""
        }
        ]
    },
        "create": {
        "id": "create",
        "memberType": "action",
        "links": [
        {
            "rel": "urn:org.restfulobjects:rels/detailsaction=\"create\"",
            "href": "http://localhost:8080/restful/services/simple.SimpleObjectMenu/actions/create",
            "method": "GET",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object-action\""
        }
        ]
    }
    }
    }"""

    // http://localhost:8080/restful/services/isisApplib.LayoutServiceMenu
    private val jsonStr1 = """{
        "links": [
        {
            "rel": "self",
            "href": "http://localhost:8080/restful/services/isisApplib.LayoutServiceMenu",
            "method": "GET",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
            "title": "Prototyping"
        },
        {
            "rel": "describedby",
            "href": "http://localhost:8080/restful/domain-types/isisApplib.LayoutServiceMenu",
            "method": "GET",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.apache.isis.restfulobjects:rels/layout",
            "href": "http://localhost:8080/restful/domain-types/isisApplib.LayoutServiceMenu/layout",
            "method": "GET",
            "type": "application/xmlprofile=\"urn:org.restfulobjects:repr-types/layout-bs3\""
        },
        {
            "rel": "up",
            "href": "http://localhost:8080/restful/services",
            "method": "GET",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/list\""
        }
        ],
        "extensions": {
        "oid": "isisApplib.LayoutServiceMenu:1",
        "isService": true,
        "isPersistent": true,
        "menuBar": "SECONDARY"
    },
        "title": "Prototyping",
        "serviceId": "isisApplib.LayoutServiceMenu",
        "members": {
        "downloadLayouts": {
        "id": "downloadLayouts",
        "memberType": "action",
        "links": [
        {
            "rel": "urn:org.restfulobjects:rels/detailsaction=\"downloadLayouts\"",
            "href": "http://localhost:8080/restful/services/isisApplib.LayoutServiceMenu/actions/downloadLayouts",
            "method": "GET",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object-action\""
        }
        ]
    }
    }
    }"""

    // http://localhost:8080/restful/services/isisApplib.MetaModelServicesMenu
    private val jsonStr2 = """{
        "links": [
        {
            "rel": "self",
            "href": "http://localhost:8080/restful/services/isisApplib.MetaModelServicesMenu",
            "method": "GET",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
            "title": "Prototyping"
        },
        {
            "rel": "describedby",
            "href": "http://localhost:8080/restful/domain-types/isisApplib.MetaModelServicesMenu",
            "method": "GET",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.apache.isis.restfulobjects:rels/layout",
            "href": "http://localhost:8080/restful/domain-types/isisApplib.MetaModelServicesMenu/layout",
            "method": "GET",
            "type": "application/xmlprofile=\"urn:org.restfulobjects:repr-types/layout-bs3\""
        },
        {
            "rel": "up",
            "href": "http://localhost:8080/restful/services",
            "method": "GET",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/list\""
        }
        ],
        "extensions": {
        "oid": "isisApplib.MetaModelServicesMenu:1",
        "isService": true,
        "isPersistent": true,
        "menuBar": "SECONDARY"
    },
        "title": "Prototyping",
        "serviceId": "isisApplib.MetaModelServicesMenu",
        "members": {
        "downloadMetaModel": {
        "id": "downloadMetaModel",
        "memberType": "action",
        "links": [
        {
            "rel": "urn:org.restfulobjects:rels/detailsaction=\"downloadMetaModel\"",
            "href": "http://localhost:8080/restful/services/isisApplib.MetaModelServicesMenu/actions/downloadMetaModel",
            "method": "GET",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object-action\""
        }
        ]
    }
    }
    }"""

}