package org.ro.snapshots.demo2_0_0

import org.ro.snapshots.Response

object DEMO_PROPERTY_DESCRIPTION : Response() {
    override val url = "http://localhost:8080/restful/domain-types/demo.DependentArgsDemoItem/properties/parity"
    override val str = """
{
    "id": "parity",
    "memberType": "property",
    "links": [
        {
            "rel": "self",
            "href": "http://localhost:8080/restful/domain-types/demo.DependentArgsDemoItem/properties/parity",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/property-description\""
        },
        {
            "rel": "up",
            "href": "http://localhost:8080/restful/domain-types/demo.DependentArgsDemoItem",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/return-type",
            "href": "http://localhost:8080/restful/domain-types/demoapp.dom.actions.depargs.Parity",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        }
    ],
    "optional": false,
    "extensions": {
        "friendlyName": "Parity",
        "description": "The parity of this 'DemoItem'."
    }
}
        """
}
