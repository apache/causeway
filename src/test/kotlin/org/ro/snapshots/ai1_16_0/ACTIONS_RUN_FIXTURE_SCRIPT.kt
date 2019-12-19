package org.ro.snapshots.ai1_16_0

object ACTIONS_RUN_FIXTURE_SCRIPT : Response() {
    override val url = "http://localhost:8080/restful/services/isisApplib.FixtureScriptsDefault/actions/runFixtureScript"
    override val str = """{
    "id": "runFixtureScript",
    "memberType": "action",
    "links": [
        {
            "rel": "self",
            "href": "http://localhost:8080/restful/services/isisApplib.FixtureScriptsDefault/actions/runFixtureScript",
            "method": "GET",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object-action\""
        },
        {
            "rel": "up",
            "href": "http://localhost:8080/restful/services/isisApplib.FixtureScriptsDefault",
            "method": "GET",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
            "title": "Prototyping"
        },
        {
            "rel": "urn:org.restfulobjects:rels/invokeaction=\"runFixtureScript\"",
            "href": "http://localhost:8080/restful/services/isisApplib.FixtureScriptsDefault/actions/runFixtureScript/invoke",
            "method": "POST",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object-action\"",
            "arguments": {
                "script": {
                    "value": null
                },
                "parameters": {
                    "value": null
                }
            }
        },
        {
            "rel": "describedby",
            "href": "http://localhost:8080/restful/domain-types/isisApplib.FixtureScriptsDefault/actions/runFixtureScript",
            "method": "GET",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/action-description\""
        }
    ],
    "extensions": {
        "actionType": "prototype",
        "actionSemantics": "nonIdempotent"
    },
    "parameters": {
        "script": {
            "num": 0,
            "id": "script",
            "name": "Script",
            "description": "",
            "choices": [
                {
                    "rel": "urn:org.restfulobjects:rels/value",
                    "href": "http://localhost:8080/restful/objects/domainapp.application.fixture.scenarios.DomainAppDemo/PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPG1lbWVudG8-PHBhdGg-PC9wYXRoPjwvbWVtZW50bz4=",
                    "method": "GET",
                    "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
                    "title": "Domain App Demo"
                }
            ],
            "default": {
                "rel": "urn:org.restfulobjects:rels/value",
                "href": "http://localhost:8080/restful/objects/domainapp.application.fixture.scenarios.DomainAppDemo/PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPG1lbWVudG8-PHBhdGg-PC9wYXRoPjwvbWVtZW50bz4=",
                "method": "GET",
                "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
                "title": "Domain App Demo"
            }
        },
        "parameters": {
            "num": 1,
            "id": "parameters",
            "name": "Parameters",
            "description": "Script-specific parameters (if any).  The format depends on the script implementation (eg key=value, CSV, JSON, XML etc)"
        }
    }
}"""
}
