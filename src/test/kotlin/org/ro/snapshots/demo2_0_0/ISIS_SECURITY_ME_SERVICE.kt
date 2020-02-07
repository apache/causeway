package org.ro.snapshots.demo2_0_0

import org.ro.snapshots.Response

object ISIS_SECURITY_ME_SERVICE : Response() {
    override val url = "http://localhost:8080/restful/objects/isissecurity.MeService/1/actions/me/invoke"
    override val str = """{
    "links": [
        {
            "rel": "self",
            "href": "http://localhost:8080/restful/objects/isissecurity.ApplicationUser/0",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object\"",
            "title": "sven"
        },
        {
            "rel": "describedby",
            "href": "http://localhost:8080/restful/domain-types/isissecurity.ApplicationUser",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.apache.isis.restfulobjects:rels/object-layout",
            "href": "http://localhost:8080/restful/objects/isissecurity.ApplicationUser/0/object-layout",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-layout-bs3\""
        },
        {
            "rel": "urn:org.apache.isis.restfulobjects:rels/object-icon",
            "href": "http://localhost:8080/restful/objects/isissecurity.ApplicationUser/0/image",
            "method": "GET",
            "type": "image/png"
        },
        {
            "rel": "urn:org.restfulobjects:rels/update",
            "href": "http://localhost:8080/restful/objects/isissecurity.ApplicationUser/0",
            "method": "PUT",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object\"",
            "arguments": {}
        }
    ],
    "extensions": {
        "oid": "isissecurity.ApplicationUser:0",
        "isService": false,
        "isPersistent": true
    },
    "title": "sven",
    "domainType": "isissecurity.ApplicationUser",
    "instanceId": "0",
    "members": {
        "emailAddress": {
            "id": "emailAddress",
            "memberType": "property",
            "links": [
                {
                    "rel": "urn:org.restfulobjects:rels/details;property=\"emailAddress\"",
                    "href": "http://localhost:8080/restful/objects/isissecurity.ApplicationUser/0/properties/emailAddress",
                    "method": "GET",
                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-property\""
                }
            ],
            "value": null,
            "extensions": {
                "x-isis-format": "string"
            },
            "disabledReason": "Always disabled"
        },
        "phoneNumber": {
            "id": "phoneNumber",
            "memberType": "property",
            "links": [
                {
                    "rel": "urn:org.restfulobjects:rels/details;property=\"phoneNumber\"",
                    "href": "http://localhost:8080/restful/objects/isissecurity.ApplicationUser/0/properties/phoneNumber",
                    "method": "GET",
                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-property\""
                }
            ],
            "value": null,
            "extensions": {
                "x-isis-format": "string"
            },
            "disabledReason": "Always disabled"
        },
        "faxNumber": {
            "id": "faxNumber",
            "memberType": "property",
            "links": [
                {
                    "rel": "urn:org.restfulobjects:rels/details;property=\"faxNumber\"",
                    "href": "http://localhost:8080/restful/objects/isissecurity.ApplicationUser/0/properties/faxNumber",
                    "method": "GET",
                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-property\""
                }
            ],
            "value": null,
            "extensions": {
                "x-isis-format": "string"
            },
            "disabledReason": "Always disabled"
        },
        "name": {
            "id": "name",
            "memberType": "property",
            "links": [
                {
                    "rel": "urn:org.restfulobjects:rels/details;property=\"name\"",
                    "href": "http://localhost:8080/restful/objects/isissecurity.ApplicationUser/0/properties/name",
                    "method": "GET",
                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-property\""
                }
            ],
            "value": "sven",
            "extensions": {
                "x-isis-format": "string"
            },
            "disabledReason": "Always disabled"
        },
        "username": {
            "id": "username",
            "memberType": "property",
            "links": [
                {
                    "rel": "urn:org.restfulobjects:rels/details;property=\"username\"",
                    "href": "http://localhost:8080/restful/objects/isissecurity.ApplicationUser/0/properties/username",
                    "method": "GET",
                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-property\""
                }
            ],
            "value": "sven",
            "extensions": {
                "x-isis-format": "string"
            },
            "disabledReason": "Always disabled"
        },
        "accountType": {
            "id": "accountType",
            "memberType": "property",
            "links": [
                {
                    "rel": "urn:org.restfulobjects:rels/details;property=\"accountType\"",
                    "href": "http://localhost:8080/restful/objects/isissecurity.ApplicationUser/0/properties/accountType",
                    "method": "GET",
                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-property\""
                }
            ],
            "value": "Local",
            "format": "string",
            "extensions": {
                "x-isis-format": "string"
            },
            "disabledReason": "Always disabled"
        },
        "hasPassword": {
            "id": "hasPassword",
            "memberType": "property",
            "links": [
                {
                    "rel": "urn:org.restfulobjects:rels/details;property=\"hasPassword\"",
                    "href": "http://localhost:8080/restful/objects/isissecurity.ApplicationUser/0/properties/hasPassword",
                    "method": "GET",
                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-property\""
                }
            ],
            "value": true,
            "extensions": {
                "x-isis-format": "boolean"
            },
            "disabledReason": "Always disabled"
        },
        "status": {
            "id": "status",
            "memberType": "property",
            "links": [
                {
                    "rel": "urn:org.restfulobjects:rels/details;property=\"status\"",
                    "href": "http://localhost:8080/restful/objects/isissecurity.ApplicationUser/0/properties/status",
                    "method": "GET",
                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-property\""
                }
            ],
            "value": "Enabled",
            "format": "string",
            "extensions": {
                "x-isis-format": "string"
            },
            "disabledReason": "Always disabled"
        },
        "atPath": {
            "id": "atPath",
            "memberType": "property",
            "links": [
                {
                    "rel": "urn:org.restfulobjects:rels/details;property=\"atPath\"",
                    "href": "http://localhost:8080/restful/objects/isissecurity.ApplicationUser/0/properties/atPath",
                    "method": "GET",
                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-property\""
                }
            ],
            "value": "/",
            "extensions": {
                "x-isis-format": "string"
            },
            "disabledReason": "Always disabled"
        },
        "roles": {
            "id": "roles",
            "memberType": "collection",
            "links": [
                {
                    "rel": "urn:org.restfulobjects:rels/details;collection=\"roles\"",
                    "href": "http://localhost:8080/restful/objects/isissecurity.ApplicationUser/0/collections/roles",
                    "method": "GET",
                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-collection\""
                }
            ],
            "disabledReason": "Always disabled"
        },
        "permissions": {
            "id": "permissions",
            "memberType": "collection",
            "links": [
                {
                    "rel": "urn:org.restfulobjects:rels/details;collection=\"permissions\"",
                    "href": "http://localhost:8080/restful/objects/isissecurity.ApplicationUser/0/collections/permissions",
                    "method": "GET",
                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-collection\""
                }
            ],
            "disabledReason": "Contributed collection"
        },
        "delete": {
            "id": "delete",
            "memberType": "action",
            "links": [
                {
                    "rel": "urn:org.restfulobjects:rels/details;action=\"delete\"",
                    "href": "http://localhost:8080/restful/objects/isissecurity.ApplicationUser/0/actions/delete",
                    "method": "GET",
                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
            ],
            "disabledReason": "Cannot delete the admin user"
        },
        "updateAccountType": {
            "id": "updateAccountType",
            "memberType": "action",
            "links": [
                {
                    "rel": "urn:org.restfulobjects:rels/details;action=\"updateAccountType\"",
                    "href": "http://localhost:8080/restful/objects/isissecurity.ApplicationUser/0/actions/updateAccountType",
                    "method": "GET",
                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
            ],
            "disabledReason": "Cannot change account type for admin user"
        },
        "unlock": {
            "id": "unlock",
            "memberType": "action",
            "links": [
                {
                    "rel": "urn:org.restfulobjects:rels/details;action=\"unlock\"",
                    "href": "http://localhost:8080/restful/objects/isissecurity.ApplicationUser/0/actions/unlock",
                    "method": "GET",
                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
            ],
            "disabledReason": "Status is already set to ENABLE"
        },
        "lock": {
            "id": "lock",
            "memberType": "action",
            "links": [
                {
                    "rel": "urn:org.restfulobjects:rels/details;action=\"lock\"",
                    "href": "http://localhost:8080/restful/objects/isissecurity.ApplicationUser/0/actions/lock",
                    "method": "GET",
                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
            ],
            "disabledReason": "Cannot disable the 'sven' user."
        },
        "updateAtPath": {
            "id": "updateAtPath",
            "memberType": "action",
            "links": [
                {
                    "rel": "urn:org.restfulobjects:rels/details;action=\"updateAtPath\"",
                    "href": "http://localhost:8080/restful/objects/isissecurity.ApplicationUser/0/actions/updateAtPath",
                    "method": "GET",
                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
            ]
        },
        "updateEmailAddress": {
            "id": "updateEmailAddress",
            "memberType": "action",
            "links": [
                {
                    "rel": "urn:org.restfulobjects:rels/details;action=\"updateEmailAddress\"",
                    "href": "http://localhost:8080/restful/objects/isissecurity.ApplicationUser/0/actions/updateEmailAddress",
                    "method": "GET",
                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
            ]
        },
        "updateFaxNumber": {
            "id": "updateFaxNumber",
            "memberType": "action",
            "links": [
                {
                    "rel": "urn:org.restfulobjects:rels/details;action=\"updateFaxNumber\"",
                    "href": "http://localhost:8080/restful/objects/isissecurity.ApplicationUser/0/actions/updateFaxNumber",
                    "method": "GET",
                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
            ]
        },
        "updatePassword": {
            "id": "updatePassword",
            "memberType": "action",
            "links": [
                {
                    "rel": "urn:org.restfulobjects:rels/details;action=\"updatePassword\"",
                    "href": "http://localhost:8080/restful/objects/isissecurity.ApplicationUser/0/actions/updatePassword",
                    "method": "GET",
                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
            ]
        },
        "resetPassword": {
            "id": "resetPassword",
            "memberType": "action",
            "links": [
                {
                    "rel": "urn:org.restfulobjects:rels/details;action=\"resetPassword\"",
                    "href": "http://localhost:8080/restful/objects/isissecurity.ApplicationUser/0/actions/resetPassword",
                    "method": "GET",
                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
            ]
        },
        "updateName": {
            "id": "updateName",
            "memberType": "action",
            "links": [
                {
                    "rel": "urn:org.restfulobjects:rels/details;action=\"updateName\"",
                    "href": "http://localhost:8080/restful/objects/isissecurity.ApplicationUser/0/actions/updateName",
                    "method": "GET",
                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
            ]
        },
        "updatePhoneNumber": {
            "id": "updatePhoneNumber",
            "memberType": "action",
            "links": [
                {
                    "rel": "urn:org.restfulobjects:rels/details;action=\"updatePhoneNumber\"",
                    "href": "http://localhost:8080/restful/objects/isissecurity.ApplicationUser/0/actions/updatePhoneNumber",
                    "method": "GET",
                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
            ]
        },
        "addRole": {
            "id": "addRole",
            "memberType": "action",
            "links": [
                {
                    "rel": "urn:org.restfulobjects:rels/details;action=\"addRole\"",
                    "href": "http://localhost:8080/restful/objects/isissecurity.ApplicationUser/0/actions/addRole",
                    "method": "GET",
                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
            ]
        },
        "removeRole": {
            "id": "removeRole",
            "memberType": "action",
            "links": [
                {
                    "rel": "urn:org.restfulobjects:rels/details;action=\"removeRole\"",
                    "href": "http://localhost:8080/restful/objects/isissecurity.ApplicationUser/0/actions/removeRole",
                    "method": "GET",
                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
            ]
        },
        "updateUsername": {
            "id": "updateUsername",
            "memberType": "action",
            "links": [
                {
                    "rel": "urn:org.restfulobjects:rels/details;action=\"updateUsername\"",
                    "href": "http://localhost:8080/restful/objects/isissecurity.ApplicationUser/0/actions/updateUsername",
                    "method": "GET",
                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
            ]
        },
        "clearHints": {
            "id": "clearHints",
            "memberType": "action",
            "links": [
                {
                    "rel": "urn:org.restfulobjects:rels/details;action=\"clearHints\"",
                    "href": "http://localhost:8080/restful/objects/isissecurity.ApplicationUser/0/actions/clearHints",
                    "method": "GET",
                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
            ]
        },
        "filterPermissions": {
            "id": "filterPermissions",
            "memberType": "action",
            "links": [
                {
                    "rel": "urn:org.restfulobjects:rels/details;action=\"filterPermissions\"",
                    "href": "http://localhost:8080/restful/objects/isissecurity.ApplicationUser/0/actions/filterPermissions",
                    "method": "GET",
                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
            ]
        }
    }
}
"""
}
