package org.apache.isis.client.kroviz.snapshots.demo2_0_0

import org.apache.isis.client.kroviz.snapshots.Response

object ACTIONS_TEXT_INVOKE : Response() {
    override val url = "http://localhost:8080/restful/objects/demo.FeaturedTypesMenu/1/actions/text/invoke"
    override val str = """{
    "links": [
        {
            "rel": "self",
            "href": "http://localhost:8080/restful/objects/demo.Text/AR-LCAAAAAAAAACFkLEOgkAMhneeorlJB0Xj4nBASIybi9EHOKExF3s9wx0Kby8ERKIkdvrTv_36pzKpDMEDC6ctR2K9XAlAzmyu-RqJ82m_2ApwXnGuyDJGokYnkjiQOzQ2DqAp6XzRTMcKOgGzjHR2A4NzGfbeePBQktekGeMUzFv3u4G7K-ZWbKBtu-UE7QMYY4-ocstUNzmKXg6BLqUHRWSfDjxWHhwSZn4ADquTMQc3_QZPpf1z6pcayLD75QsMtJyWiwEAAA==",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object\"",
            "title": "TextDemo"
        },
        {
            "rel": "describedby",
            "href": "http://localhost:8080/restful/domain-types/demo.Text",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.apache.isis.restfulobjects:rels/object-layout",
            "href": "http://localhost:8080/restful/objects/demo.Text/AR-LCAAAAAAAAACFkLEOgkAMhneeorlJB0Xj4nBASIybi9EHOKExF3s9wx0Kby8ERKIkdvrTv_36pzKpDMEDC6ctR2K9XAlAzmyu-RqJ82m_2ApwXnGuyDJGokYnkjiQOzQ2DqAp6XzRTMcKOgGzjHR2A4NzGfbeePBQktekGeMUzFv3u4G7K-ZWbKBtu-UE7QMYY4-ocstUNzmKXg6BLqUHRWSfDjxWHhwSZn4ADquTMQc3_QZPpf1z6pcayLD75QsMtJyWiwEAAA==/object-layout",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-layout-bs3\""
        },
        {
            "rel": "urn:org.apache.isis.restfulobjects:rels/object-icon",
            "href": "http://localhost:8080/restful/objects/demo.Text/AR-LCAAAAAAAAACFkLEOgkAMhneeorlJB0Xj4nBASIybi9EHOKExF3s9wx0Kby8ERKIkdvrTv_36pzKpDMEDC6ctR2K9XAlAzmyu-RqJ82m_2ApwXnGuyDJGokYnkjiQOzQ2DqAp6XzRTMcKOgGzjHR2A4NzGfbeePBQktekGeMUzFv3u4G7K-ZWbKBtu-UE7QMYY4-ocstUNzmKXg6BLqUHRWSfDjxWHhwSZn4ADquTMQc3_QZPpf1z6pcayLD75QsMtJyWiwEAAA==/image",
            "method": "GET",
            "type": "image/png"
        },
        {
            "rel": "urn:org.restfulobjects:rels/update",
            "href": "http://localhost:8080/restful/objects/demo.Text/AR-LCAAAAAAAAACFkLEOgkAMhneeorlJB0Xj4nBASIybi9EHOKExF3s9wx0Kby8ERKIkdvrTv_36pzKpDMEDC6ctR2K9XAlAzmyu-RqJ82m_2ApwXnGuyDJGokYnkjiQOzQ2DqAp6XzRTMcKOgGzjHR2A4NzGfbeePBQktekGeMUzFv3u4G7K-ZWbKBtu-UE7QMYY4-ocstUNzmKXg6BLqUHRWSfDjxWHhwSZn4ADquTMQc3_QZPpf1z6pcayLD75QsMtJyWiwEAAA==",
            "method": "PUT",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object\"",
            "arguments": {}
        }
    ],
    "extensions": {
        "oid": "demo.Text:AR-LCAAAAAAAAACFkLEOgkAMhneeorlJB0Xj4nBASIybi9EHOKExF3s9wx0Kby8ERKIkdvrTv_36pzKpDMEDC6ctR2K9XAlAzmyu-RqJ82m_2ApwXnGuyDJGokYnkjiQOzQ2DqAp6XzRTMcKOgGzjHR2A4NzGfbeePBQktekGeMUzFv3u4G7K-ZWbKBtu-UE7QMYY4-ocstUNzmKXg6BLqUHRWSfDjxWHhwSZn4ADquTMQc3_QZPpf1z6pcayLD75QsMtJyWiwEAAA==",
        "isService": false,
        "isPersistent": true
    },
    "title": "TextDemo",
    "domainType": "demo.Text",
    "instanceId": "AR-LCAAAAAAAAACFkLEOgkAMhneeorlJB0Xj4nBASIybi9EHOKExF3s9wx0Kby8ERKIkdvrTv_36pzKpDMEDC6ctR2K9XAlAzmyu-RqJ82m_2ApwXnGuyDJGokYnkjiQOzQ2DqAp6XzRTMcKOgGzjHR2A4NzGfbeePBQktekGeMUzFv3u4G7K-ZWbKBtu-UE7QMYY4-ocstUNzmKXg6BLqUHRWSfDjxWHhwSZn4ADquTMQc3_QZPpf1z6pcayLD75QsMtJyWiwEAAA==",
    "members": {
        "description": {
            "id": "description",
            "memberType": "property",
            "links": [
                {
                    "rel": "urn:org.restfulobjects:rels/details;property=\"description\"",
                    "href": "http://localhost:8080/restful/objects/demo.Text/AR-LCAAAAAAAAACFkLEOgkAMhneeorlJB0Xj4nBASIybi9EHOKExF3s9wx0Kby8ERKIkdvrTv_36pzKpDMEDC6ctR2K9XAlAzmyu-RqJ82m_2ApwXnGuyDJGokYnkjiQOzQ2DqAp6XzRTMcKOgGzjHR2A4NzGfbeePBQktekGeMUzFv3u4G7K-ZWbKBtu-UE7QMYY4-ocstUNzmKXg6BLqUHRWSfDjxWHhwSZn4ADquTMQc3_QZPpf1z6pcayLD75QsMtJyWiwEAAA==/properties/description",
                    "method": "GET",
                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-property\""
                }
            ],
            "value": "<div class=\"paragraph\">\n<p>(since 1.)</p>\n</div>\n<div class=\"paragraph\">\n<p>The framework supports text values as:</p>\n</div>\n<div class=\"ulist\">\n<ul>\n<li>\n<p>Single-line</p>\n</li>\n<li>\n<p>Multi-line</p>\n</li>\n</ul>\n</div>\n<div class=\"paragraph\">\n<p>For multi-line rendering use <code>@PropertyLayout(multiLine=&#8230;&#8203;)</code>:</p>\n</div>\n<div class=\"listingblock\">\n<div class=\"content\">\n<pre class=\"highlight\"><code class=\"language-java\" data-lang=\"java\">public class TextDemo {\n\n    @Property\n    @Getter @Setter private String string; // rendered as single line field\n\n    @Property\n    @PropertyLayout(multiLine=3)\n    @Getter @Setter private String stringMultiline; // rendered as multi-line field (3 lines)\n\n}</code></pre>\n</div>\n</div>\n<div class=\"paragraph\">\n<p>See the text demo <a href=\"https://github.com/apache/isis/tree/master/examples/demo/src/main/java/demoapp/dom/types/text\">sources</a>.</p>\n</div>",
            "format": "string",
            "extensions": {
                "x-isis-format": "string"
            },
            "disabledReason": "Always disabled"
        },
        "string": {
            "id": "string",
            "memberType": "property",
            "links": [
                {
                    "rel": "urn:org.restfulobjects:rels/details;property=\"string\"",
                    "href": "http://localhost:8080/restful/objects/demo.Text/AR-LCAAAAAAAAACFkLEOgkAMhneeorlJB0Xj4nBASIybi9EHOKExF3s9wx0Kby8ERKIkdvrTv_36pzKpDMEDC6ctR2K9XAlAzmyu-RqJ82m_2ApwXnGuyDJGokYnkjiQOzQ2DqAp6XzRTMcKOgGzjHR2A4NzGfbeePBQktekGeMUzFv3u4G7K-ZWbKBtu-UE7QMYY4-ocstUNzmKXg6BLqUHRWSfDjxWHhwSZn4ADquTMQc3_QZPpf1z6pcayLD75QsMtJyWiwEAAA==/properties/string",
                    "method": "GET",
                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-property\""
                }
            ],
            "value": "a string (click me)",
            "extensions": {
                "x-isis-format": "string"
            }
        },
        "stringMultiline": {
            "id": "stringMultiline",
            "memberType": "property",
            "links": [
                {
                    "rel": "urn:org.restfulobjects:rels/details;property=\"stringMultiline\"",
                    "href": "http://localhost:8080/restful/objects/demo.Text/AR-LCAAAAAAAAACFkLEOgkAMhneeorlJB0Xj4nBASIybi9EHOKExF3s9wx0Kby8ERKIkdvrTv_36pzKpDMEDC6ctR2K9XAlAzmyu-RqJ82m_2ApwXnGuyDJGokYnkjiQOzQ2DqAp6XzRTMcKOgGzjHR2A4NzGfbeePBQktekGeMUzFv3u4G7K-ZWbKBtu-UE7QMYY4-ocstUNzmKXg6BLqUHRWSfDjxWHhwSZn4ADquTMQc3_QZPpf1z6pcayLD75QsMtJyWiwEAAA==/properties/stringMultiline",
                    "method": "GET",
                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-property\""
                }
            ],
            "value": "A multiline string\nspanning\n3 lines. (click me)",
            "extensions": {
                "x-isis-format": "string"
            }
        },
        "stringMultilineReadonly": {
            "id": "stringMultilineReadonly",
            "memberType": "property",
            "links": [
                {
                    "rel": "urn:org.restfulobjects:rels/details;property=\"stringMultilineReadonly\"",
                    "href": "http://localhost:8080/restful/objects/demo.Text/AR-LCAAAAAAAAACFkLEOgkAMhneeorlJB0Xj4nBASIybi9EHOKExF3s9wx0Kby8ERKIkdvrTv_36pzKpDMEDC6ctR2K9XAlAzmyu-RqJ82m_2ApwXnGuyDJGokYnkjiQOzQ2DqAp6XzRTMcKOgGzjHR2A4NzGfbeePBQktekGeMUzFv3u4G7K-ZWbKBtu-UE7QMYY4-ocstUNzmKXg6BLqUHRWSfDjxWHhwSZn4ADquTMQc3_QZPpf1z6pcayLD75QsMtJyWiwEAAA==/properties/stringMultilineReadonly",
                    "method": "GET",
                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-property\""
                }
            ],
            "value": "A readonly string\nspanning\n3 lines. (but allows text select)",
            "extensions": {
                "x-isis-format": "string"
            },
            "disabledReason": "Always disabled"
        },
        "stringReadonly": {
            "id": "stringReadonly",
            "memberType": "property",
            "links": [
                {
                    "rel": "urn:org.restfulobjects:rels/details;property=\"stringReadonly\"",
                    "href": "http://localhost:8080/restful/objects/demo.Text/AR-LCAAAAAAAAACFkLEOgkAMhneeorlJB0Xj4nBASIybi9EHOKExF3s9wx0Kby8ERKIkdvrTv_36pzKpDMEDC6ctR2K9XAlAzmyu-RqJ82m_2ApwXnGuyDJGokYnkjiQOzQ2DqAp6XzRTMcKOgGzjHR2A4NzGfbeePBQktekGeMUzFv3u4G7K-ZWbKBtu-UE7QMYY4-ocstUNzmKXg6BLqUHRWSfDjxWHhwSZn4ADquTMQc3_QZPpf1z6pcayLD75QsMtJyWiwEAAA==/properties/stringReadonly",
                    "method": "GET",
                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-property\""
                }
            ],
            "value": "a readonly string (but allows text select)",
            "extensions": {
                "x-isis-format": "string"
            },
            "disabledReason": "Always disabled"
        },
        "clearHints": {
            "id": "clearHints",
            "memberType": "action",
            "links": [
                {
                    "rel": "urn:org.restfulobjects:rels/details;action=\"clearHints\"",
                    "href": "http://localhost:8080/restful/objects/demo.Text/AR-LCAAAAAAAAACFkLEOgkAMhneeorlJB0Xj4nBASIybi9EHOKExF3s9wx0Kby8ERKIkdvrTv_36pzKpDMEDC6ctR2K9XAlAzmyu-RqJ82m_2ApwXnGuyDJGokYnkjiQOzQ2DqAp6XzRTMcKOgGzjHR2A4NzGfbeePBQktekGeMUzFv3u4G7K-ZWbKBtu-UE7QMYY4-ocstUNzmKXg6BLqUHRWSfDjxWHhwSZn4ADquTMQc3_QZPpf1z6pcayLD75QsMtJyWiwEAAA==/actions/clearHints",
                    "method": "GET",
                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
            ]
        }
    }
}
"""
}
