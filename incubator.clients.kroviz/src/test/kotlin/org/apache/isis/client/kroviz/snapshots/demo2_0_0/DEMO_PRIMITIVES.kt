package org.apache.isis.client.kroviz.snapshots.demo2_0_0

import org.apache.isis.client.kroviz.snapshots.Response

object DEMO_PRIMITIVES : Response(){
    override val url = "http://localhost:8080/restful/objects/demo.FeaturedTypesMenu/1/actions/primitives/invoke"
    override val str = """{
    "links": [
        {
            "rel": "self",
            "href": "http://localhost:8080/restful/objects/demo.Primitives/AR-LCAAAAAAAAABlkstugzAQRff5CsTeBD_ARgJHqgJSpeyafoDbuJQK7AoIav6-POJ26nrhxfGZeyWG_PDVtcGk-6GxpghxFIeBNq_20pi6CJ_PFRJhMIzKXFRrjS7Cmx7Cg9zlR91ZuQvmk3-oSZ2UqR-sbbUy8k21g873Pt7kz77pmrGZdLVYzvWop577q5bjfAFxZV7_bdQSEw6qF-KFrQxhIkAY8Nzo07vtR0kJT0HeBr3ADaJFhZnQdfOPZtS17iXBjDNBUway3ZuX7jD6mYElf4dc1MmaWmaEUMpJTFORMM4TEYOy1fCaVob-j8E-MOeiqtaq-UNFLCaC0KSk4rdme_MXv0IcsRKxBK4eyC7gaK8v7bzUiGc8zSimTKSE4oSXNAY9d80rulMWZeW8HQaqnJ_vt9_4G2ScniUGAwAA",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object\"",
            "title": "Primitives Demo"
        },
        {
            "rel": "describedby",
            "href": "http://localhost:8080/restful/domain-types/demo.Primitives",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.apache.isis.restfulobjects:rels/object-layout",
            "href": "http://localhost:8080/restful/objects/demo.Primitives/AR-LCAAAAAAAAABlkstugzAQRff5CsTeBD_ARgJHqgJSpeyafoDbuJQK7AoIav6-POJ26nrhxfGZeyWG_PDVtcGk-6GxpghxFIeBNq_20pi6CJ_PFRJhMIzKXFRrjS7Cmx7Cg9zlR91ZuQvmk3-oSZ2UqR-sbbUy8k21g873Pt7kz77pmrGZdLVYzvWop577q5bjfAFxZV7_bdQSEw6qF-KFrQxhIkAY8Nzo07vtR0kJT0HeBr3ADaJFhZnQdfOPZtS17iXBjDNBUway3ZuX7jD6mYElf4dc1MmaWmaEUMpJTFORMM4TEYOy1fCaVob-j8E-MOeiqtaq-UNFLCaC0KSk4rdme_MXv0IcsRKxBK4eyC7gaK8v7bzUiGc8zSimTKSE4oSXNAY9d80rulMWZeW8HQaqnJ_vt9_4G2ScniUGAwAA/object-layout",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-layout-bs3\""
        },
        {
            "rel": "urn:org.apache.isis.restfulobjects:rels/object-icon",
            "href": "http://localhost:8080/restful/objects/demo.Primitives/AR-LCAAAAAAAAABlkstugzAQRff5CsTeBD_ARgJHqgJSpeyafoDbuJQK7AoIav6-POJ26nrhxfGZeyWG_PDVtcGk-6GxpghxFIeBNq_20pi6CJ_PFRJhMIzKXFRrjS7Cmx7Cg9zlR91ZuQvmk3-oSZ2UqR-sbbUy8k21g873Pt7kz77pmrGZdLVYzvWop577q5bjfAFxZV7_bdQSEw6qF-KFrQxhIkAY8Nzo07vtR0kJT0HeBr3ADaJFhZnQdfOPZtS17iXBjDNBUway3ZuX7jD6mYElf4dc1MmaWmaEUMpJTFORMM4TEYOy1fCaVob-j8E-MOeiqtaq-UNFLCaC0KSk4rdme_MXv0IcsRKxBK4eyC7gaK8v7bzUiGc8zSimTKSE4oSXNAY9d80rulMWZeW8HQaqnJ_vt9_4G2ScniUGAwAA/image",
            "method": "GET",
            "type": "image/png"
        },
        {
            "rel": "urn:org.restfulobjects:rels/update",
            "href": "http://localhost:8080/restful/objects/demo.Primitives/AR-LCAAAAAAAAABlkstugzAQRff5CsTeBD_ARgJHqgJSpeyafoDbuJQK7AoIav6-POJ26nrhxfGZeyWG_PDVtcGk-6GxpghxFIeBNq_20pi6CJ_PFRJhMIzKXFRrjS7Cmx7Cg9zlR91ZuQvmk3-oSZ2UqR-sbbUy8k21g873Pt7kz77pmrGZdLVYzvWop577q5bjfAFxZV7_bdQSEw6qF-KFrQxhIkAY8Nzo07vtR0kJT0HeBr3ADaJFhZnQdfOPZtS17iXBjDNBUway3ZuX7jD6mYElf4dc1MmaWmaEUMpJTFORMM4TEYOy1fCaVob-j8E-MOeiqtaq-UNFLCaC0KSk4rdme_MXv0IcsRKxBK4eyC7gaK8v7bzUiGc8zSimTKSE4oSXNAY9d80rulMWZeW8HQaqnJ_vt9_4G2ScniUGAwAA",
            "method": "PUT",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object\"",
            "arguments": {}
        }
    ],
    "extensions": {
        "oid": "demo.Primitives:AR-LCAAAAAAAAABlkstugzAQRff5CsTeBD_ARgJHqgJSpeyafoDbuJQK7AoIav6-POJ26nrhxfGZeyWG_PDVtcGk-6GxpghxFIeBNq_20pi6CJ_PFRJhMIzKXFRrjS7Cmx7Cg9zlR91ZuQvmk3-oSZ2UqR-sbbUy8k21g873Pt7kz77pmrGZdLVYzvWop577q5bjfAFxZV7_bdQSEw6qF-KFrQxhIkAY8Nzo07vtR0kJT0HeBr3ADaJFhZnQdfOPZtS17iXBjDNBUway3ZuX7jD6mYElf4dc1MmaWmaEUMpJTFORMM4TEYOy1fCaVob-j8E-MOeiqtaq-UNFLCaC0KSk4rdme_MXv0IcsRKxBK4eyC7gaK8v7bzUiGc8zSimTKSE4oSXNAY9d80rulMWZeW8HQaqnJ_vt9_4G2ScniUGAwAA",
        "isService": false,
        "isPersistent": true
    },
    "title": "Primitives Demo",
    "domainType": "demo.Primitives",
    "instanceId": "AR-LCAAAAAAAAABlkstugzAQRff5CsTeBD_ARgJHqgJSpeyafoDbuJQK7AoIav6-POJ26nrhxfGZeyWG_PDVtcGk-6GxpghxFIeBNq_20pi6CJ_PFRJhMIzKXFRrjS7Cmx7Cg9zlR91ZuQvmk3-oSZ2UqR-sbbUy8k21g873Pt7kz77pmrGZdLVYzvWop577q5bjfAFxZV7_bdQSEw6qF-KFrQxhIkAY8Nzo07vtR0kJT0HeBr3ADaJFhZnQdfOPZtS17iXBjDNBUway3ZuX7jD6mYElf4dc1MmaWmaEUMpJTFORMM4TEYOy1fCaVob-j8E-MOeiqtaq-UNFLCaC0KSk4rdme_MXv0IcsRKxBK4eyC7gaK8v7bzUiGc8zSimTKSE4oSXNAY9d80rulMWZeW8HQaqnJ_vt9_4G2ScniUGAwAA",
    "members": {
        "description": {
            "id": "description",
            "memberType": "property",
            "links": [
                {
                    "rel": "urn:org.restfulobjects:rels/details;property=\"description\"",
                    "href": "http://localhost:8080/restful/objects/demo.Primitives/AR-LCAAAAAAAAABlkstugzAQRff5CsTeBD_ARgJHqgJSpeyafoDbuJQK7AoIav6-POJ26nrhxfGZeyWG_PDVtcGk-6GxpghxFIeBNq_20pi6CJ_PFRJhMIzKXFRrjS7Cmx7Cg9zlR91ZuQvmk3-oSZ2UqR-sbbUy8k21g873Pt7kz77pmrGZdLVYzvWop577q5bjfAFxZV7_bdQSEw6qF-KFrQxhIkAY8Nzo07vtR0kJT0HeBr3ADaJFhZnQdfOPZtS17iXBjDNBUway3ZuX7jD6mYElf4dc1MmaWmaEUMpJTFORMM4TEYOy1fCaVob-j8E-MOeiqtaq-UNFLCaC0KSk4rdme_MXv0IcsRKxBK4eyC7gaK8v7bzUiGc8zSimTKSE4oSXNAY9d80rulMWZeW8HQaqnJ_vt9_4G2ScniUGAwAA/properties/description",
                    "method": "GET",
                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-property\""
                }
            ],
            "value": "<div class=\"paragraph\">\n<p>(since 1.x)</p>\n</div>\n<div class=\"paragraph\">\n<p>The framework supports following types in their primitive and boxed form:</p>\n</div>\n<div class=\"ulist\">\n<ul>\n<li>\n<p>java.lang.<strong>Boolean</strong></p>\n</li>\n<li>\n<p>java.lang.<strong>Byte</strong></p>\n</li>\n<li>\n<p>java.lang.<strong>Short</strong></p>\n</li>\n<li>\n<p>java.lang.<strong>Integer</strong></p>\n</li>\n<li>\n<p>java.lang.<strong>Long</strong></p>\n</li>\n<li>\n<p>java.lang.<strong>Float</strong></p>\n</li>\n<li>\n<p>java.lang.<strong>Double</strong></p>\n</li>\n</ul>\n</div>\n<div class=\"paragraph\">\n<p>Also note how <strong>null</strong> and <strong>void</strong> are handled regarding action results or properties.</p>\n</div>\n<div class=\"listingblock\">\n<div class=\"content\">\n<pre class=\"highlight\"><code class=\"language-java\" data-lang=\"java\">//TODO floating point value types are broken, see https://issues.apache.org/jira/browse/ISIS-2168</code></pre>\n</div>\n</div>\n<div class=\"paragraph\">\n<p>See the primitives demo\n<a href=\"https://github.com/apache/isis/tree/master/examples/demo/src/main/java/demoapp/dom/types/primitive\">sources</a>.</p>\n</div>",
            "format": "string",
            "extensions": {
                "x-isis-format": "string"
            },
            "disabledReason": "Always disabled"
        },
        "javaLangBoolean": {
            "id": "javaLangBoolean",
            "memberType": "property",
            "links": [
                {
                    "rel": "urn:org.restfulobjects:rels/details;property=\"javaLangBoolean\"",
                    "href": "http://localhost:8080/restful/objects/demo.Primitives/AR-LCAAAAAAAAABlkstugzAQRff5CsTeBD_ARgJHqgJSpeyafoDbuJQK7AoIav6-POJ26nrhxfGZeyWG_PDVtcGk-6GxpghxFIeBNq_20pi6CJ_PFRJhMIzKXFRrjS7Cmx7Cg9zlR91ZuQvmk3-oSZ2UqR-sbbUy8k21g873Pt7kz77pmrGZdLVYzvWop577q5bjfAFxZV7_bdQSEw6qF-KFrQxhIkAY8Nzo07vtR0kJT0HeBr3ADaJFhZnQdfOPZtS17iXBjDNBUway3ZuX7jD6mYElf4dc1MmaWmaEUMpJTFORMM4TEYOy1fCaVob-j8E-MOeiqtaq-UNFLCaC0KSk4rdme_MXv0IcsRKxBK4eyC7gaK8v7bzUiGc8zSimTKSE4oSXNAY9d80rulMWZeW8HQaqnJ_vt9_4G2ScniUGAwAA/properties/javaLangBoolean",
                    "method": "GET",
                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-property\""
                }
            ],
            "value": false,
            "extensions": {
                "x-isis-format": "boolean"
            }
        },
        "javaLangByte": {
            "id": "javaLangByte",
            "memberType": "property",
            "links": [
                {
                    "rel": "urn:org.restfulobjects:rels/details;property=\"javaLangByte\"",
                    "href": "http://localhost:8080/restful/objects/demo.Primitives/AR-LCAAAAAAAAABlkstugzAQRff5CsTeBD_ARgJHqgJSpeyafoDbuJQK7AoIav6-POJ26nrhxfGZeyWG_PDVtcGk-6GxpghxFIeBNq_20pi6CJ_PFRJhMIzKXFRrjS7Cmx7Cg9zlR91ZuQvmk3-oSZ2UqR-sbbUy8k21g873Pt7kz77pmrGZdLVYzvWop577q5bjfAFxZV7_bdQSEw6qF-KFrQxhIkAY8Nzo07vtR0kJT0HeBr3ADaJFhZnQdfOPZtS17iXBjDNBUway3ZuX7jD6mYElf4dc1MmaWmaEUMpJTFORMM4TEYOy1fCaVob-j8E-MOeiqtaq-UNFLCaC0KSk4rdme_MXv0IcsRKxBK4eyC7gaK8v7bzUiGc8zSimTKSE4oSXNAY9d80rulMWZeW8HQaqnJ_vt9_4G2ScniUGAwAA/properties/javaLangByte",
                    "method": "GET",
                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-property\""
                }
            ],
            "value": 127,
            "format": "int",
            "extensions": {
                "x-isis-format": "byte"
            }
        },
        "javaLangDouble": {
            "id": "javaLangDouble",
            "memberType": "property",
            "links": [
                {
                    "rel": "urn:org.restfulobjects:rels/details;property=\"javaLangDouble\"",
                    "href": "http://localhost:8080/restful/objects/demo.Primitives/AR-LCAAAAAAAAABlkstugzAQRff5CsTeBD_ARgJHqgJSpeyafoDbuJQK7AoIav6-POJ26nrhxfGZeyWG_PDVtcGk-6GxpghxFIeBNq_20pi6CJ_PFRJhMIzKXFRrjS7Cmx7Cg9zlR91ZuQvmk3-oSZ2UqR-sbbUy8k21g873Pt7kz77pmrGZdLVYzvWop577q5bjfAFxZV7_bdQSEw6qF-KFrQxhIkAY8Nzo07vtR0kJT0HeBr3ADaJFhZnQdfOPZtS17iXBjDNBUway3ZuX7jD6mYElf4dc1MmaWmaEUMpJTFORMM4TEYOy1fCaVob-j8E-MOeiqtaq-UNFLCaC0KSk4rdme_MXv0IcsRKxBK4eyC7gaK8v7bzUiGc8zSimTKSE4oSXNAY9d80rulMWZeW8HQaqnJ_vt9_4G2ScniUGAwAA/properties/javaLangDouble",
                    "method": "GET",
                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-property\""
                }
            ],
            "value": 1.7976931348623157e+308,
            "format": "decimal",
            "extensions": {
                "x-isis-format": "double"
            }
        },
        "javaLangFloat": {
            "id": "javaLangFloat",
            "memberType": "property",
            "links": [
                {
                    "rel": "urn:org.restfulobjects:rels/details;property=\"javaLangFloat\"",
                    "href": "http://localhost:8080/restful/objects/demo.Primitives/AR-LCAAAAAAAAABlkstugzAQRff5CsTeBD_ARgJHqgJSpeyafoDbuJQK7AoIav6-POJ26nrhxfGZeyWG_PDVtcGk-6GxpghxFIeBNq_20pi6CJ_PFRJhMIzKXFRrjS7Cmx7Cg9zlR91ZuQvmk3-oSZ2UqR-sbbUy8k21g873Pt7kz77pmrGZdLVYzvWop577q5bjfAFxZV7_bdQSEw6qF-KFrQxhIkAY8Nzo07vtR0kJT0HeBr3ADaJFhZnQdfOPZtS17iXBjDNBUway3ZuX7jD6mYElf4dc1MmaWmaEUMpJTFORMM4TEYOy1fCaVob-j8E-MOeiqtaq-UNFLCaC0KSk4rdme_MXv0IcsRKxBK4eyC7gaK8v7bzUiGc8zSimTKSE4oSXNAY9d80rulMWZeW8HQaqnJ_vt9_4G2ScniUGAwAA/properties/javaLangFloat",
                    "method": "GET",
                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-property\""
                }
            ],
            "value": 3.4028235e+38,
            "format": "decimal",
            "extensions": {
                "x-isis-format": "float"
            }
        },
        "javaLangInteger": {
            "id": "javaLangInteger",
            "memberType": "property",
            "links": [
                {
                    "rel": "urn:org.restfulobjects:rels/details;property=\"javaLangInteger\"",
                    "href": "http://localhost:8080/restful/objects/demo.Primitives/AR-LCAAAAAAAAABlkstugzAQRff5CsTeBD_ARgJHqgJSpeyafoDbuJQK7AoIav6-POJ26nrhxfGZeyWG_PDVtcGk-6GxpghxFIeBNq_20pi6CJ_PFRJhMIzKXFRrjS7Cmx7Cg9zlR91ZuQvmk3-oSZ2UqR-sbbUy8k21g873Pt7kz77pmrGZdLVYzvWop577q5bjfAFxZV7_bdQSEw6qF-KFrQxhIkAY8Nzo07vtR0kJT0HeBr3ADaJFhZnQdfOPZtS17iXBjDNBUway3ZuX7jD6mYElf4dc1MmaWmaEUMpJTFORMM4TEYOy1fCaVob-j8E-MOeiqtaq-UNFLCaC0KSk4rdme_MXv0IcsRKxBK4eyC7gaK8v7bzUiGc8zSimTKSE4oSXNAY9d80rulMWZeW8HQaqnJ_vt9_4G2ScniUGAwAA/properties/javaLangInteger",
                    "method": "GET",
                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-property\""
                }
            ],
            "value": 2147483647,
            "format": "int",
            "extensions": {
                "x-isis-format": "int"
            }
        },
        "javaLangLong": {
            "id": "javaLangLong",
            "memberType": "property",
            "links": [
                {
                    "rel": "urn:org.restfulobjects:rels/details;property=\"javaLangLong\"",
                    "href": "http://localhost:8080/restful/objects/demo.Primitives/AR-LCAAAAAAAAABlkstugzAQRff5CsTeBD_ARgJHqgJSpeyafoDbuJQK7AoIav6-POJ26nrhxfGZeyWG_PDVtcGk-6GxpghxFIeBNq_20pi6CJ_PFRJhMIzKXFRrjS7Cmx7Cg9zlR91ZuQvmk3-oSZ2UqR-sbbUy8k21g873Pt7kz77pmrGZdLVYzvWop577q5bjfAFxZV7_bdQSEw6qF-KFrQxhIkAY8Nzo07vtR0kJT0HeBr3ADaJFhZnQdfOPZtS17iXBjDNBUway3ZuX7jD6mYElf4dc1MmaWmaEUMpJTFORMM4TEYOy1fCaVob-j8E-MOeiqtaq-UNFLCaC0KSk4rdme_MXv0IcsRKxBK4eyC7gaK8v7bzUiGc8zSimTKSE4oSXNAY9d80rulMWZeW8HQaqnJ_vt9_4G2ScniUGAwAA/properties/javaLangLong",
                    "method": "GET",
                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-property\""
                }
            ],
            "value": 9223372036854776000,
            "format": "int",
            "extensions": {
                "x-isis-format": "long"
            }
        },
        "javaLangShort": {
            "id": "javaLangShort",
            "memberType": "property",
            "links": [
                {
                    "rel": "urn:org.restfulobjects:rels/details;property=\"javaLangShort\"",
                    "href": "http://localhost:8080/restful/objects/demo.Primitives/AR-LCAAAAAAAAABlkstugzAQRff5CsTeBD_ARgJHqgJSpeyafoDbuJQK7AoIav6-POJ26nrhxfGZeyWG_PDVtcGk-6GxpghxFIeBNq_20pi6CJ_PFRJhMIzKXFRrjS7Cmx7Cg9zlR91ZuQvmk3-oSZ2UqR-sbbUy8k21g873Pt7kz77pmrGZdLVYzvWop577q5bjfAFxZV7_bdQSEw6qF-KFrQxhIkAY8Nzo07vtR0kJT0HeBr3ADaJFhZnQdfOPZtS17iXBjDNBUway3ZuX7jD6mYElf4dc1MmaWmaEUMpJTFORMM4TEYOy1fCaVob-j8E-MOeiqtaq-UNFLCaC0KSk4rdme_MXv0IcsRKxBK4eyC7gaK8v7bzUiGc8zSimTKSE4oSXNAY9d80rulMWZeW8HQaqnJ_vt9_4G2ScniUGAwAA/properties/javaLangShort",
                    "method": "GET",
                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-property\""
                }
            ],
            "value": 32767,
            "format": "int",
            "extensions": {
                "x-isis-format": "short"
            }
        },
        "nullObject": {
            "id": "nullObject",
            "memberType": "property",
            "links": [
                {
                    "rel": "urn:org.restfulobjects:rels/details;property=\"nullObject\"",
                    "href": "http://localhost:8080/restful/objects/demo.Primitives/AR-LCAAAAAAAAABlkstugzAQRff5CsTeBD_ARgJHqgJSpeyafoDbuJQK7AoIav6-POJ26nrhxfGZeyWG_PDVtcGk-6GxpghxFIeBNq_20pi6CJ_PFRJhMIzKXFRrjS7Cmx7Cg9zlR91ZuQvmk3-oSZ2UqR-sbbUy8k21g873Pt7kz77pmrGZdLVYzvWop577q5bjfAFxZV7_bdQSEw6qF-KFrQxhIkAY8Nzo07vtR0kJT0HeBr3ADaJFhZnQdfOPZtS17iXBjDNBUway3ZuX7jD6mYElf4dc1MmaWmaEUMpJTFORMM4TEYOy1fCaVob-j8E-MOeiqtaq-UNFLCaC0KSk4rdme_MXv0IcsRKxBK4eyC7gaK8v7bzUiGc8zSimTKSE4oSXNAY9d80rulMWZeW8HQaqnJ_vt9_4G2ScniUGAwAA/properties/nullObject",
                    "method": "GET",
                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-property\""
                }
            ],
            "value": null,
            "disabledReason": "Always disabled"
        },
        "primitiveByte": {
            "id": "primitiveByte",
            "memberType": "property",
            "links": [
                {
                    "rel": "urn:org.restfulobjects:rels/details;property=\"primitiveByte\"",
                    "href": "http://localhost:8080/restful/objects/demo.Primitives/AR-LCAAAAAAAAABlkstugzAQRff5CsTeBD_ARgJHqgJSpeyafoDbuJQK7AoIav6-POJ26nrhxfGZeyWG_PDVtcGk-6GxpghxFIeBNq_20pi6CJ_PFRJhMIzKXFRrjS7Cmx7Cg9zlR91ZuQvmk3-oSZ2UqR-sbbUy8k21g873Pt7kz77pmrGZdLVYzvWop577q5bjfAFxZV7_bdQSEw6qF-KFrQxhIkAY8Nzo07vtR0kJT0HeBr3ADaJFhZnQdfOPZtS17iXBjDNBUway3ZuX7jD6mYElf4dc1MmaWmaEUMpJTFORMM4TEYOy1fCaVob-j8E-MOeiqtaq-UNFLCaC0KSk4rdme_MXv0IcsRKxBK4eyC7gaK8v7bzUiGc8zSimTKSE4oSXNAY9d80rulMWZeW8HQaqnJ_vt9_4G2ScniUGAwAA/properties/primitiveByte",
                    "method": "GET",
                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-property\""
                }
            ],
            "value": -128,
            "format": "int",
            "extensions": {
                "x-isis-format": "byte"
            },
            "disabledReason": "Always disabled"
        },
        "primitiveDouble": {
            "id": "primitiveDouble",
            "memberType": "property",
            "links": [
                {
                    "rel": "urn:org.restfulobjects:rels/details;property=\"primitiveDouble\"",
                    "href": "http://localhost:8080/restful/objects/demo.Primitives/AR-LCAAAAAAAAABlkstugzAQRff5CsTeBD_ARgJHqgJSpeyafoDbuJQK7AoIav6-POJ26nrhxfGZeyWG_PDVtcGk-6GxpghxFIeBNq_20pi6CJ_PFRJhMIzKXFRrjS7Cmx7Cg9zlR91ZuQvmk3-oSZ2UqR-sbbUy8k21g873Pt7kz77pmrGZdLVYzvWop577q5bjfAFxZV7_bdQSEw6qF-KFrQxhIkAY8Nzo07vtR0kJT0HeBr3ADaJFhZnQdfOPZtS17iXBjDNBUway3ZuX7jD6mYElf4dc1MmaWmaEUMpJTFORMM4TEYOy1fCaVob-j8E-MOeiqtaq-UNFLCaC0KSk4rdme_MXv0IcsRKxBK4eyC7gaK8v7bzUiGc8zSimTKSE4oSXNAY9d80rulMWZeW8HQaqnJ_vt9_4G2ScniUGAwAA/properties/primitiveDouble",
                    "method": "GET",
                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-property\""
                }
            ],
            "value": 5e-324,
            "format": "decimal",
            "extensions": {
                "x-isis-format": "double"
            },
            "disabledReason": "Always disabled"
        },
        "primitiveFalse": {
            "id": "primitiveFalse",
            "memberType": "property",
            "links": [
                {
                    "rel": "urn:org.restfulobjects:rels/details;property=\"primitiveFalse\"",
                    "href": "http://localhost:8080/restful/objects/demo.Primitives/AR-LCAAAAAAAAABlkstugzAQRff5CsTeBD_ARgJHqgJSpeyafoDbuJQK7AoIav6-POJ26nrhxfGZeyWG_PDVtcGk-6GxpghxFIeBNq_20pi6CJ_PFRJhMIzKXFRrjS7Cmx7Cg9zlR91ZuQvmk3-oSZ2UqR-sbbUy8k21g873Pt7kz77pmrGZdLVYzvWop577q5bjfAFxZV7_bdQSEw6qF-KFrQxhIkAY8Nzo07vtR0kJT0HeBr3ADaJFhZnQdfOPZtS17iXBjDNBUway3ZuX7jD6mYElf4dc1MmaWmaEUMpJTFORMM4TEYOy1fCaVob-j8E-MOeiqtaq-UNFLCaC0KSk4rdme_MXv0IcsRKxBK4eyC7gaK8v7bzUiGc8zSimTKSE4oSXNAY9d80rulMWZeW8HQaqnJ_vt9_4G2ScniUGAwAA/properties/primitiveFalse",
                    "method": "GET",
                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-property\""
                }
            ],
            "value": false,
            "extensions": {
                "x-isis-format": "boolean"
            },
            "disabledReason": "Always disabled"
        },
        "primitiveFloat": {
            "id": "primitiveFloat",
            "memberType": "property",
            "links": [
                {
                    "rel": "urn:org.restfulobjects:rels/details;property=\"primitiveFloat\"",
                    "href": "http://localhost:8080/restful/objects/demo.Primitives/AR-LCAAAAAAAAABlkstugzAQRff5CsTeBD_ARgJHqgJSpeyafoDbuJQK7AoIav6-POJ26nrhxfGZeyWG_PDVtcGk-6GxpghxFIeBNq_20pi6CJ_PFRJhMIzKXFRrjS7Cmx7Cg9zlR91ZuQvmk3-oSZ2UqR-sbbUy8k21g873Pt7kz77pmrGZdLVYzvWop577q5bjfAFxZV7_bdQSEw6qF-KFrQxhIkAY8Nzo07vtR0kJT0HeBr3ADaJFhZnQdfOPZtS17iXBjDNBUway3ZuX7jD6mYElf4dc1MmaWmaEUMpJTFORMM4TEYOy1fCaVob-j8E-MOeiqtaq-UNFLCaC0KSk4rdme_MXv0IcsRKxBK4eyC7gaK8v7bzUiGc8zSimTKSE4oSXNAY9d80rulMWZeW8HQaqnJ_vt9_4G2ScniUGAwAA/properties/primitiveFloat",
                    "method": "GET",
                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-property\""
                }
            ],
            "value": 1.4e-45,
            "format": "decimal",
            "extensions": {
                "x-isis-format": "float"
            },
            "disabledReason": "Always disabled"
        },
        "primitiveInteger": {
            "id": "primitiveInteger",
            "memberType": "property",
            "links": [
                {
                    "rel": "urn:org.restfulobjects:rels/details;property=\"primitiveInteger\"",
                    "href": "http://localhost:8080/restful/objects/demo.Primitives/AR-LCAAAAAAAAABlkstugzAQRff5CsTeBD_ARgJHqgJSpeyafoDbuJQK7AoIav6-POJ26nrhxfGZeyWG_PDVtcGk-6GxpghxFIeBNq_20pi6CJ_PFRJhMIzKXFRrjS7Cmx7Cg9zlR91ZuQvmk3-oSZ2UqR-sbbUy8k21g873Pt7kz77pmrGZdLVYzvWop577q5bjfAFxZV7_bdQSEw6qF-KFrQxhIkAY8Nzo07vtR0kJT0HeBr3ADaJFhZnQdfOPZtS17iXBjDNBUway3ZuX7jD6mYElf4dc1MmaWmaEUMpJTFORMM4TEYOy1fCaVob-j8E-MOeiqtaq-UNFLCaC0KSk4rdme_MXv0IcsRKxBK4eyC7gaK8v7bzUiGc8zSimTKSE4oSXNAY9d80rulMWZeW8HQaqnJ_vt9_4G2ScniUGAwAA/properties/primitiveInteger",
                    "method": "GET",
                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-property\""
                }
            ],
            "value": -2147483648,
            "format": "int",
            "extensions": {
                "x-isis-format": "int"
            },
            "disabledReason": "Always disabled"
        },
        "primitiveLong": {
            "id": "primitiveLong",
            "memberType": "property",
            "links": [
                {
                    "rel": "urn:org.restfulobjects:rels/details;property=\"primitiveLong\"",
                    "href": "http://localhost:8080/restful/objects/demo.Primitives/AR-LCAAAAAAAAABlkstugzAQRff5CsTeBD_ARgJHqgJSpeyafoDbuJQK7AoIav6-POJ26nrhxfGZeyWG_PDVtcGk-6GxpghxFIeBNq_20pi6CJ_PFRJhMIzKXFRrjS7Cmx7Cg9zlR91ZuQvmk3-oSZ2UqR-sbbUy8k21g873Pt7kz77pmrGZdLVYzvWop577q5bjfAFxZV7_bdQSEw6qF-KFrQxhIkAY8Nzo07vtR0kJT0HeBr3ADaJFhZnQdfOPZtS17iXBjDNBUway3ZuX7jD6mYElf4dc1MmaWmaEUMpJTFORMM4TEYOy1fCaVob-j8E-MOeiqtaq-UNFLCaC0KSk4rdme_MXv0IcsRKxBK4eyC7gaK8v7bzUiGc8zSimTKSE4oSXNAY9d80rulMWZeW8HQaqnJ_vt9_4G2ScniUGAwAA/properties/primitiveLong",
                    "method": "GET",
                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-property\""
                }
            ],
            "value": -9223372036854776000,
            "format": "int",
            "extensions": {
                "x-isis-format": "long"
            },
            "disabledReason": "Always disabled"
        },
        "primitiveShort": {
            "id": "primitiveShort",
            "memberType": "property",
            "links": [
                {
                    "rel": "urn:org.restfulobjects:rels/details;property=\"primitiveShort\"",
                    "href": "http://localhost:8080/restful/objects/demo.Primitives/AR-LCAAAAAAAAABlkstugzAQRff5CsTeBD_ARgJHqgJSpeyafoDbuJQK7AoIav6-POJ26nrhxfGZeyWG_PDVtcGk-6GxpghxFIeBNq_20pi6CJ_PFRJhMIzKXFRrjS7Cmx7Cg9zlR91ZuQvmk3-oSZ2UqR-sbbUy8k21g873Pt7kz77pmrGZdLVYzvWop577q5bjfAFxZV7_bdQSEw6qF-KFrQxhIkAY8Nzo07vtR0kJT0HeBr3ADaJFhZnQdfOPZtS17iXBjDNBUway3ZuX7jD6mYElf4dc1MmaWmaEUMpJTFORMM4TEYOy1fCaVob-j8E-MOeiqtaq-UNFLCaC0KSk4rdme_MXv0IcsRKxBK4eyC7gaK8v7bzUiGc8zSimTKSE4oSXNAY9d80rulMWZeW8HQaqnJ_vt9_4G2ScniUGAwAA/properties/primitiveShort",
                    "method": "GET",
                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-property\""
                }
            ],
            "value": -32768,
            "format": "int",
            "extensions": {
                "x-isis-format": "short"
            },
            "disabledReason": "Always disabled"
        },
        "primitiveTrue": {
            "id": "primitiveTrue",
            "memberType": "property",
            "links": [
                {
                    "rel": "urn:org.restfulobjects:rels/details;property=\"primitiveTrue\"",
                    "href": "http://localhost:8080/restful/objects/demo.Primitives/AR-LCAAAAAAAAABlkstugzAQRff5CsTeBD_ARgJHqgJSpeyafoDbuJQK7AoIav6-POJ26nrhxfGZeyWG_PDVtcGk-6GxpghxFIeBNq_20pi6CJ_PFRJhMIzKXFRrjS7Cmx7Cg9zlR91ZuQvmk3-oSZ2UqR-sbbUy8k21g873Pt7kz77pmrGZdLVYzvWop577q5bjfAFxZV7_bdQSEw6qF-KFrQxhIkAY8Nzo07vtR0kJT0HeBr3ADaJFhZnQdfOPZtS17iXBjDNBUway3ZuX7jD6mYElf4dc1MmaWmaEUMpJTFORMM4TEYOy1fCaVob-j8E-MOeiqtaq-UNFLCaC0KSk4rdme_MXv0IcsRKxBK4eyC7gaK8v7bzUiGc8zSimTKSE4oSXNAY9d80rulMWZeW8HQaqnJ_vt9_4G2ScniUGAwAA/properties/primitiveTrue",
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
        "calculateBoolean": {
            "id": "calculateBoolean",
            "memberType": "action",
            "links": [
                {
                    "rel": "urn:org.restfulobjects:rels/details;action=\"calculateBoolean\"",
                    "href": "http://localhost:8080/restful/objects/demo.Primitives/AR-LCAAAAAAAAABlkstugzAQRff5CsTeBD_ARgJHqgJSpeyafoDbuJQK7AoIav6-POJ26nrhxfGZeyWG_PDVtcGk-6GxpghxFIeBNq_20pi6CJ_PFRJhMIzKXFRrjS7Cmx7Cg9zlR91ZuQvmk3-oSZ2UqR-sbbUy8k21g873Pt7kz77pmrGZdLVYzvWop577q5bjfAFxZV7_bdQSEw6qF-KFrQxhIkAY8Nzo07vtR0kJT0HeBr3ADaJFhZnQdfOPZtS17iXBjDNBUway3ZuX7jD6mYElf4dc1MmaWmaEUMpJTFORMM4TEYOy1fCaVob-j8E-MOeiqtaq-UNFLCaC0KSk4rdme_MXv0IcsRKxBK4eyC7gaK8v7bzUiGc8zSimTKSE4oSXNAY9d80rulMWZeW8HQaqnJ_vt9_4G2ScniUGAwAA/actions/calculateBoolean",
                    "method": "GET",
                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
            ]
        },
        "calculateBooleans": {
            "id": "calculateBooleans",
            "memberType": "action",
            "links": [
                {
                    "rel": "urn:org.restfulobjects:rels/details;action=\"calculateBooleans\"",
                    "href": "http://localhost:8080/restful/objects/demo.Primitives/AR-LCAAAAAAAAABlkstugzAQRff5CsTeBD_ARgJHqgJSpeyafoDbuJQK7AoIav6-POJ26nrhxfGZeyWG_PDVtcGk-6GxpghxFIeBNq_20pi6CJ_PFRJhMIzKXFRrjS7Cmx7Cg9zlR91ZuQvmk3-oSZ2UqR-sbbUy8k21g873Pt7kz77pmrGZdLVYzvWop577q5bjfAFxZV7_bdQSEw6qF-KFrQxhIkAY8Nzo07vtR0kJT0HeBr3ADaJFhZnQdfOPZtS17iXBjDNBUway3ZuX7jD6mYElf4dc1MmaWmaEUMpJTFORMM4TEYOy1fCaVob-j8E-MOeiqtaq-UNFLCaC0KSk4rdme_MXv0IcsRKxBK4eyC7gaK8v7bzUiGc8zSimTKSE4oSXNAY9d80rulMWZeW8HQaqnJ_vt9_4G2ScniUGAwAA/actions/calculateBooleans",
                    "method": "GET",
                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
            ]
        },
        "calculateByte": {
            "id": "calculateByte",
            "memberType": "action",
            "links": [
                {
                    "rel": "urn:org.restfulobjects:rels/details;action=\"calculateByte\"",
                    "href": "http://localhost:8080/restful/objects/demo.Primitives/AR-LCAAAAAAAAABlkstugzAQRff5CsTeBD_ARgJHqgJSpeyafoDbuJQK7AoIav6-POJ26nrhxfGZeyWG_PDVtcGk-6GxpghxFIeBNq_20pi6CJ_PFRJhMIzKXFRrjS7Cmx7Cg9zlR91ZuQvmk3-oSZ2UqR-sbbUy8k21g873Pt7kz77pmrGZdLVYzvWop577q5bjfAFxZV7_bdQSEw6qF-KFrQxhIkAY8Nzo07vtR0kJT0HeBr3ADaJFhZnQdfOPZtS17iXBjDNBUway3ZuX7jD6mYElf4dc1MmaWmaEUMpJTFORMM4TEYOy1fCaVob-j8E-MOeiqtaq-UNFLCaC0KSk4rdme_MXv0IcsRKxBK4eyC7gaK8v7bzUiGc8zSimTKSE4oSXNAY9d80rulMWZeW8HQaqnJ_vt9_4G2ScniUGAwAA/actions/calculateByte",
                    "method": "GET",
                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
            ]
        },
        "calculateBytes": {
            "id": "calculateBytes",
            "memberType": "action",
            "links": [
                {
                    "rel": "urn:org.restfulobjects:rels/details;action=\"calculateBytes\"",
                    "href": "http://localhost:8080/restful/objects/demo.Primitives/AR-LCAAAAAAAAABlkstugzAQRff5CsTeBD_ARgJHqgJSpeyafoDbuJQK7AoIav6-POJ26nrhxfGZeyWG_PDVtcGk-6GxpghxFIeBNq_20pi6CJ_PFRJhMIzKXFRrjS7Cmx7Cg9zlR91ZuQvmk3-oSZ2UqR-sbbUy8k21g873Pt7kz77pmrGZdLVYzvWop577q5bjfAFxZV7_bdQSEw6qF-KFrQxhIkAY8Nzo07vtR0kJT0HeBr3ADaJFhZnQdfOPZtS17iXBjDNBUway3ZuX7jD6mYElf4dc1MmaWmaEUMpJTFORMM4TEYOy1fCaVob-j8E-MOeiqtaq-UNFLCaC0KSk4rdme_MXv0IcsRKxBK4eyC7gaK8v7bzUiGc8zSimTKSE4oSXNAY9d80rulMWZeW8HQaqnJ_vt9_4G2ScniUGAwAA/actions/calculateBytes",
                    "method": "GET",
                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
            ]
        },
        "calculateDouble": {
            "id": "calculateDouble",
            "memberType": "action",
            "links": [
                {
                    "rel": "urn:org.restfulobjects:rels/details;action=\"calculateDouble\"",
                    "href": "http://localhost:8080/restful/objects/demo.Primitives/AR-LCAAAAAAAAABlkstugzAQRff5CsTeBD_ARgJHqgJSpeyafoDbuJQK7AoIav6-POJ26nrhxfGZeyWG_PDVtcGk-6GxpghxFIeBNq_20pi6CJ_PFRJhMIzKXFRrjS7Cmx7Cg9zlR91ZuQvmk3-oSZ2UqR-sbbUy8k21g873Pt7kz77pmrGZdLVYzvWop577q5bjfAFxZV7_bdQSEw6qF-KFrQxhIkAY8Nzo07vtR0kJT0HeBr3ADaJFhZnQdfOPZtS17iXBjDNBUway3ZuX7jD6mYElf4dc1MmaWmaEUMpJTFORMM4TEYOy1fCaVob-j8E-MOeiqtaq-UNFLCaC0KSk4rdme_MXv0IcsRKxBK4eyC7gaK8v7bzUiGc8zSimTKSE4oSXNAY9d80rulMWZeW8HQaqnJ_vt9_4G2ScniUGAwAA/actions/calculateDouble",
                    "method": "GET",
                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
            ]
        },
        "calculateDoubles": {
            "id": "calculateDoubles",
            "memberType": "action",
            "links": [
                {
                    "rel": "urn:org.restfulobjects:rels/details;action=\"calculateDoubles\"",
                    "href": "http://localhost:8080/restful/objects/demo.Primitives/AR-LCAAAAAAAAABlkstugzAQRff5CsTeBD_ARgJHqgJSpeyafoDbuJQK7AoIav6-POJ26nrhxfGZeyWG_PDVtcGk-6GxpghxFIeBNq_20pi6CJ_PFRJhMIzKXFRrjS7Cmx7Cg9zlR91ZuQvmk3-oSZ2UqR-sbbUy8k21g873Pt7kz77pmrGZdLVYzvWop577q5bjfAFxZV7_bdQSEw6qF-KFrQxhIkAY8Nzo07vtR0kJT0HeBr3ADaJFhZnQdfOPZtS17iXBjDNBUway3ZuX7jD6mYElf4dc1MmaWmaEUMpJTFORMM4TEYOy1fCaVob-j8E-MOeiqtaq-UNFLCaC0KSk4rdme_MXv0IcsRKxBK4eyC7gaK8v7bzUiGc8zSimTKSE4oSXNAY9d80rulMWZeW8HQaqnJ_vt9_4G2ScniUGAwAA/actions/calculateDoubles",
                    "method": "GET",
                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
            ]
        },
        "calculateFloat": {
            "id": "calculateFloat",
            "memberType": "action",
            "links": [
                {
                    "rel": "urn:org.restfulobjects:rels/details;action=\"calculateFloat\"",
                    "href": "http://localhost:8080/restful/objects/demo.Primitives/AR-LCAAAAAAAAABlkstugzAQRff5CsTeBD_ARgJHqgJSpeyafoDbuJQK7AoIav6-POJ26nrhxfGZeyWG_PDVtcGk-6GxpghxFIeBNq_20pi6CJ_PFRJhMIzKXFRrjS7Cmx7Cg9zlR91ZuQvmk3-oSZ2UqR-sbbUy8k21g873Pt7kz77pmrGZdLVYzvWop577q5bjfAFxZV7_bdQSEw6qF-KFrQxhIkAY8Nzo07vtR0kJT0HeBr3ADaJFhZnQdfOPZtS17iXBjDNBUway3ZuX7jD6mYElf4dc1MmaWmaEUMpJTFORMM4TEYOy1fCaVob-j8E-MOeiqtaq-UNFLCaC0KSk4rdme_MXv0IcsRKxBK4eyC7gaK8v7bzUiGc8zSimTKSE4oSXNAY9d80rulMWZeW8HQaqnJ_vt9_4G2ScniUGAwAA/actions/calculateFloat",
                    "method": "GET",
                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
            ]
        },
        "calculateFloats": {
            "id": "calculateFloats",
            "memberType": "action",
            "links": [
                {
                    "rel": "urn:org.restfulobjects:rels/details;action=\"calculateFloats\"",
                    "href": "http://localhost:8080/restful/objects/demo.Primitives/AR-LCAAAAAAAAABlkstugzAQRff5CsTeBD_ARgJHqgJSpeyafoDbuJQK7AoIav6-POJ26nrhxfGZeyWG_PDVtcGk-6GxpghxFIeBNq_20pi6CJ_PFRJhMIzKXFRrjS7Cmx7Cg9zlR91ZuQvmk3-oSZ2UqR-sbbUy8k21g873Pt7kz77pmrGZdLVYzvWop577q5bjfAFxZV7_bdQSEw6qF-KFrQxhIkAY8Nzo07vtR0kJT0HeBr3ADaJFhZnQdfOPZtS17iXBjDNBUway3ZuX7jD6mYElf4dc1MmaWmaEUMpJTFORMM4TEYOy1fCaVob-j8E-MOeiqtaq-UNFLCaC0KSk4rdme_MXv0IcsRKxBK4eyC7gaK8v7bzUiGc8zSimTKSE4oSXNAY9d80rulMWZeW8HQaqnJ_vt9_4G2ScniUGAwAA/actions/calculateFloats",
                    "method": "GET",
                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
            ]
        },
        "calculateInteger": {
            "id": "calculateInteger",
            "memberType": "action",
            "links": [
                {
                    "rel": "urn:org.restfulobjects:rels/details;action=\"calculateInteger\"",
                    "href": "http://localhost:8080/restful/objects/demo.Primitives/AR-LCAAAAAAAAABlkstugzAQRff5CsTeBD_ARgJHqgJSpeyafoDbuJQK7AoIav6-POJ26nrhxfGZeyWG_PDVtcGk-6GxpghxFIeBNq_20pi6CJ_PFRJhMIzKXFRrjS7Cmx7Cg9zlR91ZuQvmk3-oSZ2UqR-sbbUy8k21g873Pt7kz77pmrGZdLVYzvWop577q5bjfAFxZV7_bdQSEw6qF-KFrQxhIkAY8Nzo07vtR0kJT0HeBr3ADaJFhZnQdfOPZtS17iXBjDNBUway3ZuX7jD6mYElf4dc1MmaWmaEUMpJTFORMM4TEYOy1fCaVob-j8E-MOeiqtaq-UNFLCaC0KSk4rdme_MXv0IcsRKxBK4eyC7gaK8v7bzUiGc8zSimTKSE4oSXNAY9d80rulMWZeW8HQaqnJ_vt9_4G2ScniUGAwAA/actions/calculateInteger",
                    "method": "GET",
                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
            ]
        },
        "calculateIntegers": {
            "id": "calculateIntegers",
            "memberType": "action",
            "links": [
                {
                    "rel": "urn:org.restfulobjects:rels/details;action=\"calculateIntegers\"",
                    "href": "http://localhost:8080/restful/objects/demo.Primitives/AR-LCAAAAAAAAABlkstugzAQRff5CsTeBD_ARgJHqgJSpeyafoDbuJQK7AoIav6-POJ26nrhxfGZeyWG_PDVtcGk-6GxpghxFIeBNq_20pi6CJ_PFRJhMIzKXFRrjS7Cmx7Cg9zlR91ZuQvmk3-oSZ2UqR-sbbUy8k21g873Pt7kz77pmrGZdLVYzvWop577q5bjfAFxZV7_bdQSEw6qF-KFrQxhIkAY8Nzo07vtR0kJT0HeBr3ADaJFhZnQdfOPZtS17iXBjDNBUway3ZuX7jD6mYElf4dc1MmaWmaEUMpJTFORMM4TEYOy1fCaVob-j8E-MOeiqtaq-UNFLCaC0KSk4rdme_MXv0IcsRKxBK4eyC7gaK8v7bzUiGc8zSimTKSE4oSXNAY9d80rulMWZeW8HQaqnJ_vt9_4G2ScniUGAwAA/actions/calculateIntegers",
                    "method": "GET",
                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
            ]
        },
        "calculateLong": {
            "id": "calculateLong",
            "memberType": "action",
            "links": [
                {
                    "rel": "urn:org.restfulobjects:rels/details;action=\"calculateLong\"",
                    "href": "http://localhost:8080/restful/objects/demo.Primitives/AR-LCAAAAAAAAABlkstugzAQRff5CsTeBD_ARgJHqgJSpeyafoDbuJQK7AoIav6-POJ26nrhxfGZeyWG_PDVtcGk-6GxpghxFIeBNq_20pi6CJ_PFRJhMIzKXFRrjS7Cmx7Cg9zlR91ZuQvmk3-oSZ2UqR-sbbUy8k21g873Pt7kz77pmrGZdLVYzvWop577q5bjfAFxZV7_bdQSEw6qF-KFrQxhIkAY8Nzo07vtR0kJT0HeBr3ADaJFhZnQdfOPZtS17iXBjDNBUway3ZuX7jD6mYElf4dc1MmaWmaEUMpJTFORMM4TEYOy1fCaVob-j8E-MOeiqtaq-UNFLCaC0KSk4rdme_MXv0IcsRKxBK4eyC7gaK8v7bzUiGc8zSimTKSE4oSXNAY9d80rulMWZeW8HQaqnJ_vt9_4G2ScniUGAwAA/actions/calculateLong",
                    "method": "GET",
                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
            ]
        },
        "calculateLongs": {
            "id": "calculateLongs",
            "memberType": "action",
            "links": [
                {
                    "rel": "urn:org.restfulobjects:rels/details;action=\"calculateLongs\"",
                    "href": "http://localhost:8080/restful/objects/demo.Primitives/AR-LCAAAAAAAAABlkstugzAQRff5CsTeBD_ARgJHqgJSpeyafoDbuJQK7AoIav6-POJ26nrhxfGZeyWG_PDVtcGk-6GxpghxFIeBNq_20pi6CJ_PFRJhMIzKXFRrjS7Cmx7Cg9zlR91ZuQvmk3-oSZ2UqR-sbbUy8k21g873Pt7kz77pmrGZdLVYzvWop577q5bjfAFxZV7_bdQSEw6qF-KFrQxhIkAY8Nzo07vtR0kJT0HeBr3ADaJFhZnQdfOPZtS17iXBjDNBUway3ZuX7jD6mYElf4dc1MmaWmaEUMpJTFORMM4TEYOy1fCaVob-j8E-MOeiqtaq-UNFLCaC0KSk4rdme_MXv0IcsRKxBK4eyC7gaK8v7bzUiGc8zSimTKSE4oSXNAY9d80rulMWZeW8HQaqnJ_vt9_4G2ScniUGAwAA/actions/calculateLongs",
                    "method": "GET",
                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
            ]
        },
        "calculateNull": {
            "id": "calculateNull",
            "memberType": "action",
            "links": [
                {
                    "rel": "urn:org.restfulobjects:rels/details;action=\"calculateNull\"",
                    "href": "http://localhost:8080/restful/objects/demo.Primitives/AR-LCAAAAAAAAABlkstugzAQRff5CsTeBD_ARgJHqgJSpeyafoDbuJQK7AoIav6-POJ26nrhxfGZeyWG_PDVtcGk-6GxpghxFIeBNq_20pi6CJ_PFRJhMIzKXFRrjS7Cmx7Cg9zlR91ZuQvmk3-oSZ2UqR-sbbUy8k21g873Pt7kz77pmrGZdLVYzvWop577q5bjfAFxZV7_bdQSEw6qF-KFrQxhIkAY8Nzo07vtR0kJT0HeBr3ADaJFhZnQdfOPZtS17iXBjDNBUway3ZuX7jD6mYElf4dc1MmaWmaEUMpJTFORMM4TEYOy1fCaVob-j8E-MOeiqtaq-UNFLCaC0KSk4rdme_MXv0IcsRKxBK4eyC7gaK8v7bzUiGc8zSimTKSE4oSXNAY9d80rulMWZeW8HQaqnJ_vt9_4G2ScniUGAwAA/actions/calculateNull",
                    "method": "GET",
                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
            ]
        },
        "calculateNullCollection": {
            "id": "calculateNullCollection",
            "memberType": "action",
            "links": [
                {
                    "rel": "urn:org.restfulobjects:rels/details;action=\"calculateNullCollection\"",
                    "href": "http://localhost:8080/restful/objects/demo.Primitives/AR-LCAAAAAAAAABlkstugzAQRff5CsTeBD_ARgJHqgJSpeyafoDbuJQK7AoIav6-POJ26nrhxfGZeyWG_PDVtcGk-6GxpghxFIeBNq_20pi6CJ_PFRJhMIzKXFRrjS7Cmx7Cg9zlR91ZuQvmk3-oSZ2UqR-sbbUy8k21g873Pt7kz77pmrGZdLVYzvWop577q5bjfAFxZV7_bdQSEw6qF-KFrQxhIkAY8Nzo07vtR0kJT0HeBr3ADaJFhZnQdfOPZtS17iXBjDNBUway3ZuX7jD6mYElf4dc1MmaWmaEUMpJTFORMM4TEYOy1fCaVob-j8E-MOeiqtaq-UNFLCaC0KSk4rdme_MXv0IcsRKxBK4eyC7gaK8v7bzUiGc8zSimTKSE4oSXNAY9d80rulMWZeW8HQaqnJ_vt9_4G2ScniUGAwAA/actions/calculateNullCollection",
                    "method": "GET",
                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
            ]
        },
        "calculateShort": {
            "id": "calculateShort",
            "memberType": "action",
            "links": [
                {
                    "rel": "urn:org.restfulobjects:rels/details;action=\"calculateShort\"",
                    "href": "http://localhost:8080/restful/objects/demo.Primitives/AR-LCAAAAAAAAABlkstugzAQRff5CsTeBD_ARgJHqgJSpeyafoDbuJQK7AoIav6-POJ26nrhxfGZeyWG_PDVtcGk-6GxpghxFIeBNq_20pi6CJ_PFRJhMIzKXFRrjS7Cmx7Cg9zlR91ZuQvmk3-oSZ2UqR-sbbUy8k21g873Pt7kz77pmrGZdLVYzvWop577q5bjfAFxZV7_bdQSEw6qF-KFrQxhIkAY8Nzo07vtR0kJT0HeBr3ADaJFhZnQdfOPZtS17iXBjDNBUway3ZuX7jD6mYElf4dc1MmaWmaEUMpJTFORMM4TEYOy1fCaVob-j8E-MOeiqtaq-UNFLCaC0KSk4rdme_MXv0IcsRKxBK4eyC7gaK8v7bzUiGc8zSimTKSE4oSXNAY9d80rulMWZeW8HQaqnJ_vt9_4G2ScniUGAwAA/actions/calculateShort",
                    "method": "GET",
                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
            ]
        },
        "calculateShorts": {
            "id": "calculateShorts",
            "memberType": "action",
            "links": [
                {
                    "rel": "urn:org.restfulobjects:rels/details;action=\"calculateShorts\"",
                    "href": "http://localhost:8080/restful/objects/demo.Primitives/AR-LCAAAAAAAAABlkstugzAQRff5CsTeBD_ARgJHqgJSpeyafoDbuJQK7AoIav6-POJ26nrhxfGZeyWG_PDVtcGk-6GxpghxFIeBNq_20pi6CJ_PFRJhMIzKXFRrjS7Cmx7Cg9zlR91ZuQvmk3-oSZ2UqR-sbbUy8k21g873Pt7kz77pmrGZdLVYzvWop577q5bjfAFxZV7_bdQSEw6qF-KFrQxhIkAY8Nzo07vtR0kJT0HeBr3ADaJFhZnQdfOPZtS17iXBjDNBUway3ZuX7jD6mYElf4dc1MmaWmaEUMpJTFORMM4TEYOy1fCaVob-j8E-MOeiqtaq-UNFLCaC0KSk4rdme_MXv0IcsRKxBK4eyC7gaK8v7bzUiGc8zSimTKSE4oSXNAY9d80rulMWZeW8HQaqnJ_vt9_4G2ScniUGAwAA/actions/calculateShorts",
                    "method": "GET",
                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
            ]
        },
        "calculateVoid": {
            "id": "calculateVoid",
            "memberType": "action",
            "links": [
                {
                    "rel": "urn:org.restfulobjects:rels/details;action=\"calculateVoid\"",
                    "href": "http://localhost:8080/restful/objects/demo.Primitives/AR-LCAAAAAAAAABlkstugzAQRff5CsTeBD_ARgJHqgJSpeyafoDbuJQK7AoIav6-POJ26nrhxfGZeyWG_PDVtcGk-6GxpghxFIeBNq_20pi6CJ_PFRJhMIzKXFRrjS7Cmx7Cg9zlR91ZuQvmk3-oSZ2UqR-sbbUy8k21g873Pt7kz77pmrGZdLVYzvWop577q5bjfAFxZV7_bdQSEw6qF-KFrQxhIkAY8Nzo07vtR0kJT0HeBr3ADaJFhZnQdfOPZtS17iXBjDNBUway3ZuX7jD6mYElf4dc1MmaWmaEUMpJTFORMM4TEYOy1fCaVob-j8E-MOeiqtaq-UNFLCaC0KSk4rdme_MXv0IcsRKxBK4eyC7gaK8v7bzUiGc8zSimTKSE4oSXNAY9d80rulMWZeW8HQaqnJ_vt9_4G2ScniUGAwAA/actions/calculateVoid",
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
                    "href": "http://localhost:8080/restful/objects/demo.Primitives/AR-LCAAAAAAAAABlkstugzAQRff5CsTeBD_ARgJHqgJSpeyafoDbuJQK7AoIav6-POJ26nrhxfGZeyWG_PDVtcGk-6GxpghxFIeBNq_20pi6CJ_PFRJhMIzKXFRrjS7Cmx7Cg9zlR91ZuQvmk3-oSZ2UqR-sbbUy8k21g873Pt7kz77pmrGZdLVYzvWop577q5bjfAFxZV7_bdQSEw6qF-KFrQxhIkAY8Nzo07vtR0kJT0HeBr3ADaJFhZnQdfOPZtS17iXBjDNBUway3ZuX7jD6mYElf4dc1MmaWmaEUMpJTFORMM4TEYOy1fCaVob-j8E-MOeiqtaq-UNFLCaC0KSk4rdme_MXv0IcsRKxBK4eyC7gaK8v7bzUiGc8zSimTKSE4oSXNAY9d80rulMWZeW8HQaqnJ_vt9_4G2ScniUGAwAA/actions/clearHints",
                    "method": "GET",
                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
            ]
        }
    }
}
"""
}
