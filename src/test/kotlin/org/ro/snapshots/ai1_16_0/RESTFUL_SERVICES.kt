package org.ro.snapshots.ai1_16_0
// ResultListList.kt
object RESTFUL_SERVICES : Response() {
    override val url = "http://localhost:8080/restful/services"
    override val str = """
        {
        "value": [
            {
                "rel": "urn:org.restfulobjects:rels/serviceserviceId=\"simple.SimpleObjectMenu\"",
                "href": "http://localhost:8080/restful/services/simple.SimpleObjectMenu",
                "method": "GET",
                "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
                "title": "Simple Objects"
            },
            {
                "rel": "urn:org.restfulobjects:rels/serviceserviceId=\"isisApplib.FixtureScriptsDefault\"",
                "href": "http://localhost:8080/restful/services/isisApplib.FixtureScriptsDefault",
                "method": "GET",
                "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
                "title": "Prototyping"
            },
            {
                "rel": "urn:org.restfulobjects:rels/serviceserviceId=\"isisApplib.LayoutServiceMenu\"",
                "href": "http://localhost:8080/restful/services/isisApplib.LayoutServiceMenu",
                "method": "GET",
                "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
                "title": "Prototyping"
            },
            {
                "rel": "urn:org.restfulobjects:rels/serviceserviceId=\"isisApplib.MetaModelServicesMenu\"",
                "href": "http://localhost:8080/restful/services/isisApplib.MetaModelServicesMenu",
                "method": "GET",
                "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
                "title": "Prototyping"
            },
            {
                "rel": "urn:org.restfulobjects:rels/serviceserviceId=\"isisApplib.SwaggerServiceMenu\"",
                "href": "http://localhost:8080/restful/services/isisApplib.SwaggerServiceMenu",
                "method": "GET",
                "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
                "title": "Prototyping"
            },
            {
                "rel": "urn:org.restfulobjects:rels/serviceserviceId=\"isisApplib.TranslationServicePoMenu\"",
                "href": "http://localhost:8080/restful/services/isisApplib.TranslationServicePoMenu",
                "method": "GET",
                "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
                "title": "Prototyping"
            },
            {
                "rel": "urn:org.restfulobjects:rels/serviceserviceId=\"isisApplib.HsqlDbManagerMenu\"",
                "href": "http://localhost:8080/restful/services/isisApplib.HsqlDbManagerMenu",
                "method": "GET",
                "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
                "title": "Prototyping"
            },
            {
                "rel": "urn:org.restfulobjects:rels/serviceserviceId=\"isisApplib.ConfigurationServiceMenu\"",
                "href": "http://localhost:8080/restful/services/isisApplib.ConfigurationServiceMenu",
                "method": "GET",
                "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
                "title": "Configuration Service Menu"
            }
        ],
        "extensions": {},
        "links": [
            {
                "rel": "self",
                "href": "http://localhost:8080/restful/services",
                "method": "GET",
                "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/list\""
            },
            {
                "rel": "up",
                "href": "http://localhost:8080/restful/",
                "method": "GET",
                "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/homepage\""
            }
        ]
    }"""
}
